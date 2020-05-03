(ns rooij.system.core
  (:require
   [clojure.walk :refer [postwalk]]
   [rooij.config]
   [com.rpl.specter :as sp :refer [ALL MAP-VALS MAP-KEYS]]
   [meta-merge.core :refer [meta-merge]]
   [taoensso.timbre :as timbre]
   [rooij.methods :refer [assert-schema-key]]
   [rooij.state :as state]
   [rooij.util :refer [derive-all-composites derive-all-hierarchies map-kv ->context]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(it/derive-hierarchy
 {:rooij/scene [:rooij/system]
  :rooij/entity [:rooij/system]
  :rooij/component [:rooij/system]
  :rooij/handler [:rooij/system]
  :rooij/keyboard [:rooij/system]
  :rooij/reactor [:rooij/system]
  :rooij/ticker [:rooij/system]})

(defmulti init-key
  "The init-key for rooij system components. This is used internally by rooij
  and should not be called directly."
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defn init
  "Starts an rooij system (scene or entity). This is used internally by rooij
  and should not be called directly."
  [config key]
  (ig/build config [key] init-key assert-schema-key ig/resolve-key))

(defn prep
  "Prepares the config system with a composite derive on all keys. This is used
  internally by rooij and should not be called directly."
  [config]
  (derive-all-composites config)
  (derive-all-hierarchies config)
  (ig/prep config))

(defn get-init-key [derived-k]
  (if-let [f (get-method ig/init-key (#'ig/normalize-key derived-k))]
    f
    (fn [_ opts] opts)))

(def process-refs-keys
  (memoize
   (fn [ns]
     {:key (keyword ns "key")})))

(defn- has-handler? [handler-key component]
  ((set (sp/select [:component/handlers MAP-VALS :handler/key] component))
   handler-key))

(defn- filter-handler-components [handler-key components]
  (mapv :component/key
        (filter #(has-handler? handler-key %) components)))

(defn- components->nested-routes [handler-keys components]
  (for [handler-key handler-keys]
    {handler-key (filter-handler-components handler-key components)}))

(defn- routes [entity]
  (let [components (sp/select [:entity/components MAP-VALS] entity)
        handler-keys (sp/select [ALL :component/handlers MAP-KEYS] components)]
    (->> components
         (components->nested-routes handler-keys)
         (apply merge))))

(defn add-routes [entity] []
  (assoc entity :entity/routes (routes entity)))

(defn- entity-component-state [{:entity/keys [components]}]
  (sp/transform [MAP-VALS] :component/state components))

(defn preprocess-entity [context entity-key entity-opts]
  (-> entity-opts
      (->> (meta-merge (:entity/ref entity-opts)))
      (dissoc :entity/ref)
      (merge context)
      (assoc :entity/key entity-key)))

(defn preprocess-entities [scene-key entities]
  (map-kv #(preprocess-entity (->context scene-key %1) %1 %2) entities))

(defn postprocess-entity [entity]
  (-> entity
      add-routes
      (assoc :entity/state (entity-component-state entity))
      (->> ((:entity/init entity) (:entity/key entity)))))

(defn preprocess-component [context component-key component-opts]
  (-> component-opts
      (->> (meta-merge (:component/ref component-opts)))
      (dissoc :component/ref)
      (merge context)
      (assoc :component/key component-key)
      (as-> $ (assoc $ :component/state ((:component/init $) component-key $)))))

(defn preprocess-components [scene-key entity-key components]
  (map-kv #(preprocess-component (->context scene-key entity-key %1) %1 %2) components))

(defn preprocess-handler [context handler-key handler-opts]
  (-> handler-opts
      (->> (meta-merge (:handler/ref handler-opts)))
      (dissoc :handler/ref)
      (merge context)
      (assoc :handler/key handler-key)
      (as-> $ (assoc $ :handler/fn ((:handler/init $) handler-key $)))))

(defn preprocess-ticker [context ticker-key ticker-opts]
  (-> ticker-opts
      (->> (meta-merge (:ticker/ref ticker-opts)))
      (dissoc :ticker/ref)
      (merge context)
      (assoc :ticker/key ticker-key)
      (as-> $ (assoc $ :ticker/fn ((:ticker/init $) ticker-key $)))))

(defn preprocess-reactor [context reactor-key reactor-opts]
  (-> reactor-opts
      (->> (meta-merge (:reactor/ref reactor-opts)))
      (dissoc :reactor/ref)
      (merge context)
      (assoc :reactor/key reactor-key)
      (as-> $ (assoc $ :reactor/fn ((:reactor/init $) reactor-key $)))))

(defn preprocess-middleware [context middleware-key middleware-opts]
  (-> middleware-opts
      (->> (meta-merge (:middleware/ref middleware-opts)))
      (dissoc :middleware/ref)
      (merge context)
      (assoc :middleware/key middleware-key)
      (as-> $ (assoc $ :middleware/fn ((:middleware/init $) middleware-key $)))))

(defn preprocess-handlers [scene-key entity-key component-key handlers]
  (map-kv #(preprocess-handler (->context scene-key entity-key component-key) %1 %2) handlers))

(defn preprocess-tickers [scene-key entity-key component-key ticker]
  (map-kv #(preprocess-ticker (->context scene-key entity-key component-key) %1 %2) ticker))

(defn preprocess-reactors [scene-key entity-key component-key reactor]
  (map-kv #(preprocess-reactor (->context scene-key entity-key component-key) %1 %2) reactor))

(defn preprocess-middlewares [scene-key entity-key component-key middleware]
  (map-kv #(preprocess-middleware (->context scene-key entity-key component-key) %1 %2) middleware))

(defn process-refs-component [{:context/keys [scene-key entity-key component-key] :as opts}]
  (-> opts
      (update :component/handlers (partial preprocess-handlers scene-key entity-key component-key))
      (update :component/tickers (partial preprocess-tickers scene-key entity-key component-key))
      (update :component/reactors (partial preprocess-reactors scene-key entity-key component-key))
      (update :component/middlewares (partial preprocess-middlewares scene-key entity-key component-key))))

(defn process-refs-entity [{:context/keys [scene-key entity-key] :as opts}]
  (update opts :entity/components (partial preprocess-components scene-key entity-key)))

(defn process-refs [{scene-key :scene/key :as opts}]
  (-> opts
      (update :scene/entities (partial preprocess-entities scene-key))
      (->> (sp/transform [:scene/entities MAP-VALS] process-refs-entity))
      (->> (sp/transform [:scene/entities MAP-VALS :entity/components MAP-VALS] process-refs-component))
      (->> (sp/transform [:scene/entities MAP-VALS] postprocess-entity))))

(defn get-halt-key [derived-k entity-opts]
  (if-let [f (get-method ig/halt-key! (#'ig/normalize-key derived-k))]
    (or (f derived-k entity-opts)
        (fn [_] nil))
    (fn [_] nil)))

(defn start []
  (timbre/info ::start)
  (rooij.config/reset-config! (prep @rooij.config/config))
  (-> @rooij.config/config
      (init [:rooij/system])
      (->> (reset! state/system))))

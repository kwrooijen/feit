(ns essen.core
  (:require
   [essen.util :refer [spy]]
   [clojure.spec.alpha :as s]
   [essen.keyboard]
   [essen.state :refer [input-messages messages game state]]
   [essen.system]
   [essen.system.component]
   [essen.system.entity]
   [essen.system.handler]
   [essen.system.middleware]
   [essen.system.reactor]
   [essen.system.scene]
   [essen.system.ticker]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [integrant.core :as ig]
   [spec-signature.core :refer-macros [sdef]]))

(defmethod ig/init-key :essen/const [_ opts] opts)

(defn setup [{:keys [:essen/config :essen.module/render] :as game-config}]
  (reset! game game-config)
  ((:essen/setup render) config))

(defn emit-keydown! [event]
  (doseq [[_ scene] @input-messages]
    (swap! scene
           conj {:input-message/type :key/down
                 :input-message/key (essen.keyboard/get-key event)})))

(defn emit-keyup! [event]
(doseq [[_ scene] @input-messages]
  (swap! scene
         conj {:input-message/type :key/up
               :input-message/key (essen.keyboard/get-key event)})))

(defn emit!
;; TODO implement (scene) global emit.
  ([entity route content] nil)
  ([scene entity route content]
   (swap! (get @messages scene)
          conj {:message/entity entity
                :message/route route
                :message/content content})))


;; TODO Work out details on child-ref. Maybe create an interface to create new
;; types of references. Possibly use multimethods to resolve them
;; (add-child-ref replacement)
;;
(defn child-ref? [ref]
  (and (ig/ref? ref) (:child (meta ref))))

(defn child-refs [config keys]
  (->> (ig/dependent-keys config keys)
       (select-keys config)
       (#'ig/depth-search child-ref?)
       (mapv :key)))

(defn find-derived-key
  "Return the first key in `config` that is derived from `k`."
  [config k]
  (->> (ig/find-derived config k)
       (ffirst)))

(def parent
  (comp first parents))

(defn child-key [config ref]
  (conj (find-derived-key config (parent ref)) ref))

(defn child-value [config ref]
  (it/find-derived-value config (parent ref)))

(defn add-child-ref [config ref]
  (assoc config
         (child-key config ref)
         (child-value config ref)))

(defn add-child-refs
  ([config] (add-child-refs config (keys config)))
  ([config keys]
   (reduce add-child-ref config (child-refs config keys))))

(defn child-ref [key]
  (-> (it.keyword/make-child key)
      (ig/ref)
      (with-meta {:child true})))

(defn start-scene [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom []))
  (-> (:essen/config @game)
      (add-child-refs [:scene/start])
      (ig/prep [scene-key])
      (ig/build [scene-key] essen.system/init-key (fn []) ig/resolve-key)
      (it/find-derived-value scene-key)
      (atom)
      (->> (swap! state assoc-in [:essen/scenes scene-key])))
  ((-> @game :essen.module/render :essen/stage-start) (:essen/config @game) scene-key))

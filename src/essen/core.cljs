(ns essen.core
  (:require
   [integrant-tools.core :as it]
   [essen.ticker :as ticker]
   [essen.scene :as scene]
   [essen.component :as component]
   [essen.handler :as handler]
   [essen.middleware :as middleware]
   [essen.reactor :as reactor]
   [clojure.spec.alpha :as s]
   [essen.keyboard]
   [essen.entity :as entity]
   [essen.state :refer [input-messages messages game state]]
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

(defn- essen-init-key [k opts]
  (cond
    (ig/derived-from? k :essen/scene)
    (scene/init-process k opts)

    (ig/derived-from? k :essen/entity)
    (entity/init-process k opts)

    (ig/derived-from? k :essen/component)
    (component/init-process k opts)

    (ig/derived-from? k :essen/handler)
    (handler/init-process k opts)

    (ig/derived-from? k :essen/middleware)
    (middleware/init-process k opts)

    (ig/derived-from? k :essen/reactor)
    (reactor/init-process k opts)

    (ig/derived-from? k :essen/ticker)
    (ticker/init-process k opts)

    :else
    (ig/init-key k opts)))

(defn init
  ([config] (init config (keys config)))
  ([config keys]
   (ig/build config keys (partial essen-init-key))))

(defn start-scene [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom []))
  (-> (:essen/config @game)
      (ig/prep [scene-key])
      (init [scene-key])
      (it/find-derived-value scene-key)
      (atom)
      (->> (swap! state assoc-in [:essen/scenes scene-key])))
  ((-> @game :essen.module/render :essen/stage-start) (:essen/config @game) scene-key))

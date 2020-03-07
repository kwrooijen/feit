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

(defn start-scene [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom []))
  (-> (:essen/config @game)
      (it/prep [:it/prep-meta :ig/prep] [scene-key])
      (it/init [:essen/init] [scene-key])
      (it/find-derived-value scene-key)
      (atom)
      (->> (swap! state assoc-in [:essen/scenes scene-key])))
  ((-> @game :essen.module/render :essen/stage-start) (:essen/config @game) scene-key))

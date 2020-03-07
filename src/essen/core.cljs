(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.state :refer [input-messages messages game state systems]]
   [essen.system]
   [essen.system.component]
   [essen.system.entity]
   [essen.system.handler]
   [essen.system.keyboard]
   [essen.system.middleware]
   [essen.system.reactor]
   [essen.system.scene]
   [essen.system.ticker]
   [essen.util :refer [spy]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [spec-signature.core :refer-macros [sdef]]))

(defmethod ig/init-key :essen/const [_ opts] opts)

(defn setup [{:keys [:essen/config :essen.module/render] :as game-config}]
  (reset! game game-config)
  ((:essen/setup render) config))

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
  (let [scene-system (-> (:essen/config @game)
                         (it/prep [:it/prep-meta :ig/prep] [scene-key])
                         (it/init [:essen/init] [scene-key]))]

    (swap! state assoc-in [:essen/scenes scene-key]
           (atom (it/find-derived-value scene-system scene-key)))
    (swap! systems assoc-in [:essen/scenes scene-key] scene-system))

  ((-> @game :essen.module/render :essen/stage-start) (:essen/config @game) scene-key))

(defn resume-scene [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom []))
  (let [old-system (get-in @systems [:essen/scenes scene-key])
        scene-system (ig/resume (:essen/config @game) old-system [scene-key])]
    (swap! state assoc-in [:essen/scenes scene-key]
           (atom (it/find-derived-value scene-system scene-key)))
    (swap! systems assoc-in [:essen/scenes scene-key] scene-system))

  ((-> @game :essen.module/render :essen/stage-resume) (:essen/config @game) scene-key))

(defn suspend-scene [scene-key]
  (swap! systems update-in [:essen/scenes scene-key]
         #(ig/suspend! % [scene-key]))
  ((-> @game :essen.module/render :essen/stage-suspend) (:essen/config @game) scene-key))

(defn scenes []
  (set (keys (:essen/scenes @state))))

(defn suspend! []
  (doseq [scene (scenes)]
    (suspend-scene scene)))

(defn resume [config]
  (swap! game assoc :essen/config config)
  (doseq [scene (scenes)]
    (start-scene scene)))

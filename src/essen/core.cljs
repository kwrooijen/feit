(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.keyboard]
   [essen.entity :as entity]
   [essen.state :refer [input-messages messages game state]]
   [integrant.core :as ig]
   [spec-signature.core :refer-macros [sdef]]))

(defmethod ig/init-key :essen/const [_ opts] opts)

(defn setup [{:essen/keys [setup config] :as game-config}]
  (reset! game game-config)
  (setup config))

(defn emit-keydown! [event]
  (doall
   (map (fn [[_ v]]
          (swap! v conj
                 {:input-message/type :key/down
                  :input-message/key (essen.keyboard/get-key event)}))
        @input-messages)))

(defn emit-keyup! [event]
  (doall
   (map (fn [[_ v]]
          (swap! v conj
                 {:input-message/type :key/up
                  :input-message/key (essen.keyboard/get-key event)}))
        @input-messages)))

(defn emit! [scene entity route content]
  (swap! (get @messages scene) conj {:message/entity entity
                                     :message/route route
                                     :message/content content}))

(defn start-scene [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom []))
  ((:essen/stage-start @game) (:essen/config @game) scene-key))

(defn get-scene [scene-key]
  (get-in @state [:essen/scenes scene-key]))

(defn start-ticker!
  ([{:context/keys [scene entity component]} ticker opts]
   (start-ticker! scene entity component ticker opts))
  ([scene entity component ticker opts]
   (swap! (get-scene scene)
          assoc-in
          (entity/path-ticker entity component ticker)
          {:ticker/key ticker
           :ticker/fn (ig/init-key ticker opts)})))

(defn remove-ticker!
  ([{:context/keys [scene entity component]} ticker]
   (remove-ticker! scene entity component ticker))
  ([scene entity component ticker]
   (swap! (get-scene scene)
          update-in
          (entity/path-ticker entity component)
         dissoc ticker)))

(defn start-middleware!
  ([{:context/keys [scene entity component]} handler middleware opts]
   (start-middleware! scene entity component handler middleware opts))
  ([scene entity component handler middleware opts]
   (swap! (get-scene scene)
          assoc-in
          (entity/path-middleware entity component handler middleware)
          {:middleware/key middleware
           :middleware/fn (ig/init-key middleware opts)})))

(defn remove-middleware!
  ([{:context/keys [scene entity component]} handler middleware]
   (remove-middleware! scene entity component handler middleware))
  ([scene entity component handler middleware]
   (swap! (get-scene scene)
          update-in
          (entity/path-middleware entity component handler)
          dissoc middleware)))

(comment
  (remove-ticker! :scene/start
                  :entity/player
                  :component/stats
                  :ticker.stats/poisoned)

  (start-ticker! :scene/start
                 :entity/player
                 :component/stats
                 :ticker.stats/poisoned
                 {:ticker/ticks 2
                  :ticker/damage 30})

  @(get-scene :scene/start)

  (start-middleware! :scene/start
                     :entity/player
                     :component/stats
                     :handler.stats/attack
                     :middleware.stats/invincible
                     {})
 
  (remove-middleware! :scene/start
                      :entity/player
                      :component/stats
                      :handler.stats/attack
                      :middleware.stats/invincible))

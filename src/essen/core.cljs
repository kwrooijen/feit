(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.keyboard]
   [essen.state :refer [messages game]]
   [integrant.core :as ig]
   [spec-signature.core :refer-macros [sdef]]))

(defmethod ig/init-key :essen/const [_ opts] opts)

(defn setup [{:essen/keys [setup config] :as game-config}]
  (reset! game game-config)
  (setup config))

(defn emit-keydown!
  "A standard format for emitting keydown events. This is to keep keyboard
  events consistent between libraries"
  [event]
  ;; (emit! {:event/key-down (essen.keyboard/get-key event)})
  )

(defn emit-keyup!
  "A standard format for emitting keyup events. This is to keep keyboard
  events consistent between libraries"
  [event]
  ;; (emit! {:event/key-up (essen.keyboard/get-key event)})
  )

(defn emit! [entity route content]
  ;; TODO scene only events
  (swap! messages conj {:message/entity entity
                        :message/route route
                        :message/content content}))

(defn start-scene [scene-key]
  ((:essen/stage-start @game) (:essen/config @game) scene-key))

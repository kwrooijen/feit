(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.keyboard]
   [essen.state :refer [input-messages messages game]]
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
  (doall
   (map (fn [[_ v]]
          (swap! v conj
                 {:input-message/type :key/down
                  :input-message/key (essen.keyboard/get-key event)}))
        @input-messages)))

(defn emit-keyup!
  "A standard format for emitting keyup events. This is to keep keyboard
  events consistent between libraries"
  [event]
  (doall
   (map (fn [[_ v]]
          (swap! v conj
                 {:input-message/type :key/up
                  :input-message/key (essen.keyboard/get-key event)}))
        @input-messages)))

(defn emit! [scene entity route content]
  ;; TODO scene only events
  (swap! (get @messages scene) conj {:message/entity entity
                                     :message/route route
                                     :message/content content}))

(defn start-scene [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom []))
  ((:essen/stage-start @game) (:essen/config @game) scene-key))

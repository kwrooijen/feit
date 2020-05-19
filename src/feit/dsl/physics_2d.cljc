(ns feit.dsl.physics-2d
  (:require [feit.dsl :as r]))

(defn component-rectangle
  "TODO"
  ([config k x y w h]
   (r/ref-component config [:physics-2d.component/rectangle k] {:x x :y y :w w :h h})))

(defn static?
  "TODO"
  [config boolean]
  (r/component-merge config {:static? boolean}))

(defn angle
  "TODO"
  [config angle]
  (r/component-merge config {:angle angle}))

(defn set-position
  "TODO"
  [config x y]
  (r/component-merge config {:x x :y y}))

(defn position-emitter
  "TODO"
  [config]
  (when-not (:component/last (meta config))
    (throw (ex-info (str "You can only make components position-emitters")
                    {:reason ::invalid-position-emitter})))
  (r/ref-ticker config :general-2d.ticker.position/emitter))

(defn rotation?
  "TODO"
  [config boolean]
  (r/component-merge config {:rotation? boolean}))

(defn add-sensor
  ""
  [config sensor-key x y w h opts]
  (r/component-meta-merge config {:sensors [{:sensor/key sensor-key
                                             :sensor/x x
                                             :sensor/y y
                                             :sensor/w w
                                             :sensor/h h
                                             :sensor/opts opts}]}))

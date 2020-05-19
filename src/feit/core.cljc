(ns feit.core
  (:require
   [feit.config]
   [feit.core.error]
   [feit.interface.graphics-2d.core :as interface.graphics-2d]
   [feit.interface.keyboard.core :as interface.keyboard]
   [feit.interface.physics-2d.core :as interface.physics-2d]
   [feit.logger]
   [feit.loop.core]
   [feit.spec]
   [feit.core.state :as state]
   [feit.system.component]
   [feit.system.core :as system]
   [feit.system.entity]
   [feit.system.handler]
   [feit.system.keyboard]
   [feit.system.middleware]
   [feit.system.reactor]
   [feit.system.scene :as scene]
   [feit.system.ticker]
   [taoensso.timbre :as timbre]))

(set! *print-meta* true)

(defn- start []
  (system/start)
  (interface.graphics-2d/init)
  (interface.physics-2d/init)
  (interface.keyboard/init)
  (feit.loop.core/start!)
  (scene/start-initial-scene))

(defn setup
  ([] (setup {}))
  ([config]
   (feit.config/merge-user! config)
   (try
     (timbre/debug ::setup @feit.config/config)
     (start)
     (catch #?(:clj Throwable :cljs :default) e
       (feit.core.error/handle-error e)
       (throw e)))))

(defn wireframe-enabled! [boolean]
  (reset! state/wireframe-enabled? boolean))

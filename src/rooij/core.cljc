(ns rooij.core
  (:require
   [rooij.config]
   [rooij.error]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [rooij.interface.keyboard.core :as interface.keyboard]
   [rooij.interface.physics-2d.core :as interface.physics-2d]
   [rooij.logger]
   [rooij.loop.core]
   [rooij.spec]
   [rooij.state :as state]
   [rooij.system.component]
   [rooij.system.core :as system]
   [rooij.system.entity]
   [rooij.system.handler]
   [rooij.system.keyboard]
   [rooij.system.middleware]
   [rooij.system.reactor]
   [rooij.system.scene :as scene]
   [rooij.system.ticker]
   [taoensso.timbre :as timbre]))

(set! *print-meta* true)

(defn- start []
  (system/start)
  (interface.graphics-2d/init)
  (interface.physics-2d/init)
  (interface.keyboard/init)
  (rooij.loop.core/start!)
  (scene/start-initial-scene))

(defn setup
  ([] (setup {}))
  ([config]
   (rooij.config/merge-user! config)
   (try
     (timbre/debug ::setup @rooij.config/config)
     (start)
     (catch #?(:clj Throwable :cljs :default) e
       (rooij.error/handle-error e)
       (throw e)))))

(defn wireframe-enabled! [boolean]
  (reset! state/wireframe-enabled? boolean))

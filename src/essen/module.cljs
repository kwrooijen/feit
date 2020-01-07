(ns essen.module
  (:require [integrant.core :as ig]
            [essen.state :refer [this]]))

(defn module-apply [obj opts]
  (when (:apply opts)
    ((:apply opts) obj opts)))

(defmethod ig/init-key :essen.module/add.image [[_ _] opts]
  (println "THIS" @this)
  (-> @this
      (.. -add (image (:x opts) (:y opts) (:texture opts) (:frame opts)))
      (module-apply opts)))

(defmethod ig/init-key :essen.module/load.image [[_ _] opts]
  (.. @this -load (image (:key opts) (:url opts))))

(defmethod ig/init-key :essen.module/scene.start [_ opts]
  #(.. % this -scene (start (:name opts))))

(defmethod ig/init-key :essen.module/cameras-main [_ opts]
  (fn [this]
    (-> this
        (.. -cameras -main)
        (module-apply opts))))

(defmethod ig/init-key :essen.module/physics-world [_ opts]
  (fn [this]
    (-> this
        (.. -physics -world)
        (module-apply opts))))

(defmethod ig/init-key :essen.module/input-keyboard [_ opts]
  (fn [this]
    (-> this
        (.. -input -keyboard)
        (module-apply opts))))

(defmethod ig/init-key :essen.module/physics.add.image
  [_ {:keys [x y texture frame] :as opts}]
  (fn [this]
    (-> this
        (.. -physics -add (image x y texture frame))
        (module-apply opts))))

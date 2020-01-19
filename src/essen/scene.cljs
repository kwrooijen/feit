(ns essen.scene
  (:require
   [integrant.core :as ig]
   [re-frame.core :as re-frame]
   [essen.state :refer [scene-states]]
   [essen.events.scene]))

(defn scene-state [k]
  (get @scene-states k))

;;
;; :essen.scene/state
;;

(defmethod ig/init-key :essen.scene.state/atom [_ opts]
  opts)

(defmethod ig/prep-key :essen.scene/state [[_ k] opts]
  {:data opts
   :state (ig/ref :essen.scene.state/atom)})

(defmethod ig/init-key :essen.scene/state [[_ k] {:keys [data state]}]
  (swap! state assoc k data))

;;
;; Scene
;;

(defmethod ig/prep-key :essen.scene/run [_ opts]
  (merge opts
         {:essen/this (ig/ref :essen/this)
          :essen/init (ig/ref :essen/init)}))

(defmethod ig/init-key :essen/init [_ opts]
  opts)

(defmethod ig/init-key :essen.scene/config [_ opts]
  opts)

(defmethod ig/init-key :essen.scene.update/list [_ opts]
  opts)


(defmethod ig/prep-key :essen.scene/create [_ opts]
  (assoc opts :essen.scene/create (ig/refset :essen.scene/key)))

(defmethod ig/init-key :essen.scene/create [_ opts]
  opts)

(defmethod ig/prep-key :essen/scene [[_ k] opts]
  (-> opts
      (update :essen.scene/preload (fn [preload] (fn [] preload)))
      (update :essen.scene/create (fn [create] (fn [] create)))
      (update :essen.scene/update (fn [update] (fn [] update)))))

(defn scene-init [k]
  (fn [data]
    (re-frame/dispatch [:essen.events.scene/set-active-scenes])
    (swap! (scene-state k) assoc :essen/init data)))

(defn scene-preload [opts]
  #(this-as this
     (-> ((:essen.scene/preload opts))
         (assoc :essen/this this)
         (ig/prep)
         (ig/init))))

(defn scene-create [opts k]
  #(this-as this
     (-> ((:essen.scene/create opts))
         (assoc :essen/this this)
         (assoc :essen.scene.state/atom (scene-state k))
         (assoc :essen/init (:essen/init @(scene-state k)))
         (ig/prep)
         (ig/init))))

(defn scene-updaters [opts]
  (-> ((:essen.scene/update opts))
      (ig/prep)
      (ig/init)
      (:essen.scene.update/list)))

(defn apply-updaters [scene-state updaters this time delta]
  (reduce #(%2 %1 this time delta) scene-state updaters))

(defn empty-queue [scene-state]
  (assoc scene-state :essen/queue []))

(defn scene-update [opts k]
  (let [updaters (scene-updaters opts)
        state (scene-state k)]
    (fn [time delta]
      (this-as this
        (swap! state (fn [scene-state]
                       (-> scene-state
                           (apply-updaters updaters this time delta)
                           (empty-queue))))))))

(defmethod ig/init-key :essen/scene [[_ k] opts]
  (swap! scene-states assoc k (atom {:essen/queue []}))
  (-> (merge {:key (name k)}
             (:essen.scene/config opts))
      (assoc :init (scene-init k))
      (cond->
          ((:essen.scene/preload opts))
          (assoc :preload (scene-preload opts)))
      (cond->
          ((:essen.scene/create opts))
          (assoc :create (scene-create opts k)))
      (cond->
          ((:essen.scene/update opts))
          (assoc :update (scene-update opts k)))))

;; TODO reset scene?
;; Preserve state of the atom
(defmethod ig/suspend-key! :essen/scene [_ opts])

(defmethod ig/resume-key :essen/scene [k opts old-opts old-impl]
  (ig/init-key k opts))

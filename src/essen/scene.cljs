(ns essen.scene
  (:require
   [integrant.core :as ig]
   [re-frame.core :as re-frame]
   [essen.state :refer [scene-queues]]
   [essen.events.scene]))

(defonce scene-states (atom {}))

(defn scene-state [k]
  (get @scene-states k))

;;
;; :essen.scene/state
;;

(defmethod ig/init-key :essen.scene.state/volatile [_ opts]
  opts)

(defmethod ig/prep-key :essen.scene/state [[_ k] opts]
  {:data opts
   :state (ig/ref :essen.scene.state/volatile)})

(defmethod ig/init-key :essen.scene/state [[_ k] {:keys [data state]}]
  (vswap! state assoc k data))

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

(defmethod ig/init-key :essen.scene.update/emitters [_ opts]
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
    (vswap! (scene-state k) assoc :essen/init data)))

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
         (assoc :essen.scene.state/volatile (scene-state k))
         (assoc :essen/init (:essen/init @(scene-state k)))
         (ig/prep)
         (ig/init))))

(defn init-update-scene [opts]
  (-> ((:essen.scene/update opts))
      (ig/prep)
      (ig/init)))

(defn apply-emitters [scene-state emitters this time delta]
  (->> emitters
       (mapv #(%1 scene-state this time delta))
       (flatten)))

(defn apply-updaters [scene-state updaters this time delta]
  (reduce #(%2 %1 this time delta) scene-state updaters))

(defn empty-queue [k]
  (swap! scene-queues assoc k []))

(defn scene-queue
  [scene-key]
  (let [current-queues (get @scene-queues scene-key)]
    (swap! scene-queues assoc scene-key [])
    current-queues))

(defn scene-update [opts k]
  (let [init-scene (init-update-scene opts)
        updaters (:essen.scene.update/list init-scene)
        emitters (:essen.scene.update/emitters init-scene)
        state (scene-state k)]
    (fn [time delta]
      (this-as this
        (vswap! state assoc :essen/queue (scene-queue k))
        (vswap! state update :essen/queue
                concat (apply-emitters @state emitters this time delta))
        (vswap! state #(apply-updaters % updaters this time delta))))))

(def initial-state
  (volatile! {:essen/queue []}))

(defmethod ig/init-key :essen/scene [[_ k] opts]
  (swap! scene-states assoc k initial-state)
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

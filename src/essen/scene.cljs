(ns essen.scene
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :essen/this [_ opts]
  opts)

(def method-collection
  (atom
   {[:image 3]                    #(.image %1 %2 %3)
    [:image 4]                    #(.image %1 %2 %3 %4)
    [:set-origin 2]               #(.setOrigin %1 %2)
    [:set-flip-x 2]               #(.setFlipX %1 %2)
    [:set-flip-y 2]               #(.setFlipY %1 %2)
    [:set-bounds 5]               #(.setBounds %1 %2 %3 %4 %5)
    [:create-cursor-keys 1]       #(.createCursorKeys %1)
    [:start-follow 5]             #(.startFollow %1 %2 %3 %4 %5)
    [:set-collide-world-bounds 2] #(.setCollideWorldBounds %1 %2)}))

(def essen-scene-key-collection
  {:essen.scene/load           #(.. % -load)
   :essen.scene/add            #(.. % -add)
   :essen.scene/lights         #(.. % -lights)
   :essen.scene/physics.world  #(.. % -physics -world)
   :essen.scene/physics.add    #(.. % -physics -add)
   :essen.scene/input.keyboard #(.. % -input -keyboard)
   :essen.scene/cameras.main   #(.. % -cameras -main)})

(doall
 (for [[k _] essen-scene-key-collection]
   (derive k :essen.scene/key)))

;;
;; :essen.scene/key
;;

(def method->method-key
  (juxt first count))

(defn apply-method [obj-acc [method args]]
  (apply method (cons obj-acc args)))

(defn apply-methods [obj methods]
  ;; TODO Add proper error message if method does not exist
  ;; TODO Check if we can provide a proper error message if method exists, but
  ;; errors
  (let [keys (map method->method-key methods)
        fns (map (partial get @method-collection) keys)
        args (map rest methods)
        fargs (map vector fns args)]
    (reduce apply-method obj fargs)))

(defmethod ig/prep-key :essen.scene/key [[_ k] opts]
  (derive k :essen.scene/key)
  {:essen/methods opts
   :essen/this (ig/ref :essen/this)})

(defmethod ig/init-key :essen.scene/key [[k _] {:essen/keys [this methods] :as opts}]
  (apply-methods ((get essen-scene-key-collection k) this) methods))

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

(defmethod ig/init-key :essen/scene [[_ k] opts]
  (let [state (atom {})
        updaters  (-> ((:essen.scene/update opts))
                      (ig/prep)
                      (ig/init)
                      (:essen.scene.update/list))]
    {:key (name k)
     ;; TODO Only add keys if :essen.scene/{preload, create, update} exist
     :preload
     (fn [] (this-as this
              (-> ((:essen.scene/preload opts))
                  (assoc :essen/this this)
                  (ig/prep)
                  (ig/init))))
     :create
     (fn [] (this-as this
              (-> ((:essen.scene/create opts))
                  (assoc :essen/this this)
                  (assoc :essen.scene.state/atom state)
                  (ig/prep)
                  (ig/init))))
     :update
     (fn [delta]
       (this-as this
         (reset! state
                 (reduce (fn [acc u] (u acc delta this)) @state updaters))))}))

;; TODO reset scene?
;; Preserve state of the atom
(defmethod ig/suspend-key! :essen/scene [_ opts])

(defmethod ig/resume-key :essen/scene [k opts old-opts old-impl]
  (ig/init-key k opts))

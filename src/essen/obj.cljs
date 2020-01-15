(ns essen.obj
  (:require
   [integrant.core :as ig]))

(defonce custom-methods (atom {}))

;; TODO Maybe create ig/init-key for these?
;; Then the end user can create methods that way
(def method-collection
  {[:add 2]                      #(.add %1 %2)
   [:debug 2]                    #(do (println "DEBUG:" %2) %1)
   [:create 2]                   #(.create %1 %2)
   [:create 3]                   #(.create %1 %2 %3)
   [:create 4]                   #(.create %1 %2 %3 %4)
   [:create 5]                   #(.create %1 %2 %3 %4 %5)
   [:create 6]                   #(.create %1 %2 %3 %4 %5 %6)
   [:create 7]                   #(.create %1 %2 %3 %4 %5 %6 %7)
   [:create-cursor-keys 1]       #(.createCursorKeys %1)
   [:group 1]                    #(.group (clj->js %1))
   [:group 2]                    #(.group (clj->js %1) (clj->js %2))
   [:play 2]                     #(.play %1 %2)
   [:set-scale 2]                #(.setScale %1 %2)
   [:multiatlas 4]               #(.multiatlas %1 %2 %3 %4)
   [:image 3]                    #(.image %1 %2 %3)
   [:image 4]                    #(.image %1 %2 %3 %4)
   [:set-active 2]               #(.setActive %1 %2)
   [:set-bounds 5]               #(.setBounds %1 %2 %3 %4 %5)
   [:sprite 4]                   #(.sprite %1 %2 %3 %4)
   [:sprite 5]                   #(.sprite %1 %2 %3 %4 %5)
   [:generate-frame-names 3]     #(.generateFrameNames %1 %2 %3)
   [:set-collide-world-bounds 2] #(.setCollideWorldBounds %1 %2)
   [:set-depth 2]                #(.setDepth %1 %2)
   [:set-flip-x 2]               #(.setFlipX %1 %2)
   [:set-flip-y 2]               #(.setFlipY %1 %2)
   [:set-origin 2]               #(.setOrigin %1 %2)
   [:set-visible 2]              #(.setVisible %1 %2)
   [:start-follow 5]             #(.startFollow %1 %2 %3 %4 %5)})

(def essen-scene-key-collection
  {:add            #(.. % -add)
   :apply          #(%)
   :this           (fn [this] this)
   :cameras.main   #(.. % -cameras -main)
   :input.keyboard #(.. % -input -keyboard)
   :lights         #(.. % -lights)
   :load           #(.. % -load)
   :physics.add    #(.. % -physics -add)
   :anims          #(.. % -anims)
   :physics.world  #(.. % -physics -world)})

(doall
 (for [[k _] essen-scene-key-collection]
   (do
     (derive (keyword :essen.obj k) :essen.obj/key)
     (derive (keyword :essen.obj-do k) :essen.obj-do/key)
     (derive (keyword :essen.obj-fn k) :essen.obj-fn/key))))

(defmethod ig/init-key :essen/this [_ opts]
  opts)

;;
;; ::essen.obj/key
;;

(def method->method-key
  (juxt first count))

(defn process-methods [methods k]
  (if (keyword-identical? :essen.obj/apply k)
    (rest methods)
    methods))

(defn methods->fargs [methods k]
  (let [methods (process-methods methods k)
        keys (map method->method-key methods)
        fns (map (partial get (merge method-collection @custom-methods)) keys)
        args (map rest methods)]
    (map vector fns args)))

(defn get-obj [this k methods]
  (if (keyword-identical? :essen.obj/apply k)
    (first methods)
    ((get essen-scene-key-collection (keyword (name k))) this)))

(defn get-failing-method [method]
  (reduce (fn [acc [k v]]
            (if (= v method)
              (reduced k)
              :METHOD/UNKNOWN))
          :METHOD/UNKNOWN
          method-collection))

(defn apply-method [obj-acc [method args]]
  (try
    (apply method (cons obj-acc args))
    (catch js/Error e
      (println "Object Type:" (.-type obj-acc))
      (println "Method: " (get-failing-method method))
      (println "Args: " args)
      (js/throw e))))

(defn apply-fargs [obj fargs]
  ;; TODO Add proper error message if method does not exist
  ;; TODO Check if we can provide a proper error message if method exists, but
  ;; errors
  (reduce apply-method obj fargs))


(defn apply-method-do [obj-acc [method args]]
  (apply method (cons obj-acc args))
  obj-acc)

(defn apply-fargs-do [obj fargs]
  ;; TODO Add proper error message if method does not exist
  ;; TODO Check if we can provide a proper error message if method exists, but
  ;; errors
  (reduce apply-method-do obj fargs))

(defmethod ig/prep-key :essen.obj/key [[_ k] opts]
  {:essen/methods opts
   :essen/this (ig/ref :essen/this)})

(defmethod ig/prep-key :essen.obj-do/key [[_ k] opts]
  {:essen/methods opts
   :essen/this (ig/ref :essen/this)})

(defmethod ig/prep-key :essen.obj-fn/key [[_ k] opts]
  {:essen/methods opts})

;; TODO create version that returns a function instead of call?
(defmethod ig/init-key :essen.obj/key [[k _] {:essen/keys [this methods] :as opts}]
  (apply-fargs (get-obj this k methods)
               (methods->fargs methods k)))

(defmethod ig/init-key :essen.obj-do/key [[k _] {:essen/keys [this methods] :as opts}]
  (let [obj (get-obj this k methods)]
    (apply-fargs-do obj (methods->fargs methods k))
    obj))

(defmethod ig/init-key :essen.obj-fn/key [[k _] {:essen/keys [methods] :as opts}]
  (if (keyword-identical? :essen.obj-fn/apply k)
    (let [fargs (methods->fargs methods k)]
      (fn [_this]
        (apply-fargs (first methods) fargs)))
    (let [this->obj (get essen-scene-key-collection (keyword (name k)))
          fargs (methods->fargs methods k)]
      (fn [this]
        (apply-fargs (this->obj this) fargs)))))

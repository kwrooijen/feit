(ns essen.scene
  (:require [integrant.core :as ig]
            [essen.state :refer [phaser-scenes phaser-game]]))

;; (defmethod ig/init-key :my/kevin [_ opts]
;;   (println "INIT KEVIN")
;;   (update opts :create-image (partial str "PRE-")))

;; (defmethod ig/init-key :my/poep [_ opts]
;;   (println "INIT POEP :( " opts)
;;   opts)

;; (defmethod ig/init-key :scene.boot/create [_ opts]
;;   (println "BOOT CREATE! :) " opts))

;; (defmethod ig/init-key :essen/const [_ opts]
;;   opts)

;; (defmethod ig/init-key :essen.scene/create [_ opts]
;;   (println "CREATE " opts)
;;   opts)

;; (defmethod ig/init-key :my/demi [_ opts]
;;   (println "INIT DEMI <3 " opts)
;;   opts)





(defmethod ig/init-key :essen/this [_ opts]
  opts)

(def method-collection
  {[:image 3] #(.image %1 %2 %3)
   [:image 4] #(.image %1 %2 %3 %4)})

;; Load

(def method->method-key
  (juxt first count))

(defn apply-methods [obj methods]
  (let [k (method->method-key (first methods))
        f (get method-collection k)
        args (cons obj (rest (first methods)))]
    (apply f args)))

(defmethod ig/prep-key :essen.scene/load [[_ k] opts]
  (derive k :essen.scene/key)
  (assoc opts :essen/this (ig/ref :essen/this)))

(defmethod ig/prep-key :essen.scene/add [[_ k] opts]
  (derive k :essen.scene/key)
  (assoc opts :essen/this (ig/ref :essen/this)))

(defmethod ig/init-key :essen.scene/load [_ {:essen/keys [this methods] :as opts}]
  (assoc opts :object (apply-methods (.-load this) methods)))

(defmethod ig/init-key :essen.scene/add [_ {:essen/keys [this methods] :as opts}]
  (println "THIS " this)
  (println "METHODS " methods)
  (assoc opts :object (apply-methods (.-add this) methods)))

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
  (println "HM")
  {:key (name k)
   ;; TODO Only add keys if :essen.scene/{preload, create, update} exist
   :preload
   (fn [] (this-as this
            (println "PRELAOD " this)
            (-> ((:essen.scene/preload opts))
                (assoc :essen/this this)
                (ig/prep)
                (ig/init))))
   :create
   (fn [] (this-as this
            (println "CREATE")
            (-> ((:essen.scene/create opts))
                (assoc :essen/this this)
                (ig/prep)
                (ig/init))))
   :update
   (fn [] (this-as this
            (-> ((:essen.scene/update opts))
                (assoc :essen/this this)
                (ig/prep)
                (ig/init))))})




;; (defmethod ig/init-key :essen/scene [[_ k] opts]
;;   (let [scene (get @phaser-scenes k)]
;;     (println "SCENES " @phaser-scenes)
;;     (set! (.-preload scene) (fn []
;;                               (ig/init (:essen.scene/create opts))
;;                               (println "NEW PRELOAD :D")))
;;     (set! (.-create scene) (fn [] (println "Creating..")))
;;     (.. @phaser-game -scene (start (name k)))
;;     {})
;;   ;; {:key (:key opts)
;;   ;;  :preload (fn []
;;   ;;             (this-as this (swap! essen.state/this assoc k this))
;;   ;;             (this-as this (doall (map #(% this) (:modules/preload opts))))
;;   ;;             (:modules/preload opts))
;;   ;;  :create  (fn [] ;; (this-as this (doall (map #(% this) (:modules/create opts))))
;;   ;;             (:modules/create opts)
;;   ;;             )
;;   ;;  :update  (fn [time] (this-as this (doall (map #(% this time) (:modules/update opts)))))}
;;   )

;; (defmethod ig/halt-key! :essen/scene [_ opts])

;; (defmethod ig/suspend-key! :essen/scene [_ opts])

;; (defmethod ig/resume-key :essen/scene [k opts old-opts old-impl]
;;   (if (not= opts old-opts)
;;     (ig/init-key k opts)
;;     old-impl))

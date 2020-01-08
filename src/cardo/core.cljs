(ns cardo.core
  (:require [phaser]
            [cardo.config :refer [config]]
            [integrant.core :as ig]
            [essen.core]))

;; (defmethod ig/init-key :my/update [_ opts]
;;   (fn [_this time]
;;     ;; (println "UPDATE" time)
;;     ))

;; (defmethod ig/init-key :my/update2 [_ opts]
;;   (fn [_this time]
;;     ;; (println "UPDATE2" time)
;;     ))

;; (defonce state (atom {}))

;; (defn new-scene [k]
;;   #js {:key k
;;        :preload #(this-as this (swap! state assoc k this))})

;; (defn add-scene [game scene]
;;   (doto
;;       (.-scene game)
;;       (.add (.-key scene) scene true)
;;       (.getScene (.-key scene))))

(defn ^:export init []
  ;; (essen.core/init config)
  (-> config
      (ig/prep)
      (ig/init))
  ;; (let [game (js/Phaser.Game. (clj->js {:type js/Phaser.AUTO}))
  ;;       scene1 (new-scene "scene1")
  ;;       scene2 (new-scene "scene2")
  ;;       scene3 (new-scene "scene3")
  ;;       scene4 (new-scene "scene4")
  ;;       scenes [scene1 scene2 scene3 scene4]]

  ;;   (doseq [scene scenes]
  ;;     (add-scene game scene))

  ;;   (wait-for-scenes scenes #(println "STATUH " @state))


  ;;   ;; (js/setTimeout (fn []
  ;;   ;;                  (println "STATUH " @state)
  ;;   ;;                  (println "SETTING CREATE")
  ;;   ;;                  (set! (.-preload (get @state "scene1")) (fn [] (println "NEW PRELOAD :D")))
  ;;   ;;                  (set! (.-create (get @state "scene1")) (fn []
  ;;   ;;                                                        (println @state)
  ;;   ;;                                                        (println "Creating..")))
  ;;   ;;                  (.. game -scene (start (name "scene1"))))
  ;;   ;;                3000)
  ;;   )
  )

;; (defn wait-for-scenes [scenes callback]
;;   (if (= (count scenes) (count (keys @state)))
;;     (callback)
;;     (js/setTimeout #(wait-for-scenes scenes callback)
;;                    10)))

(defn stop []
  ;; (essen.core/suspend!)
  )

(defn start []
  ;; (essen.core/resume config)
  )

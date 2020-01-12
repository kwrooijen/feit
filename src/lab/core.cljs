(ns lab.core
  (:require [integrant.core :as ig]
            [essen.core]))

(defn set-bg [obj x y flip-x flip-y]
  (.. obj
      (image x y "bg")
      (setOrigin 0)
      (setFlipX flip-x)
      (setFlipY flip-y)))

(essen.core/custom-methods!
 {[:set-bg 5] set-bg})

(defmethod ig/init-key :my/updater [_ opts]
  (fn [{:game/keys [cursor player bgs] :as state} delta this]
    ;; (when-let [bg (.get bgs)]
    ;;   (println "ONE")
    ;;   (.setPosition bg 0 0))
    ;; (when-let [bg (.get bgs)]
    ;;   (println "TWO")
    ;;   (.. bg
    ;;       (setPosition 1920 0)
    ;;       (setFlipX true)))
    ;; (when-let [bg (.get bgs)]
    ;;   (println "THRE")
    ;;   (.. bg
    ;;       (setPosition 0 1080)
    ;;       (setFlipY true)))
    ;; (when-let [bg (.get bgs)]
    ;;   (println "FOUR")
    ;;   (.. bg
    ;;       (setPosition 1920 1080)
    ;;       (setFlipX true)
    ;;       (setFlipY true)))

    (.setVelocity player 0)
    (when (.. cursor -left -isDown)
      (.setVelocityX player -500))
    (when (.. cursor -right -isDown)
      (.setVelocityX player 500))
    (when (.. cursor -up -isDown)
      (.setVelocityY player -500))
    (when (.. cursor -down -isDown)
      (.setVelocityY player 500))
    state))

(ns cardo.create.battle
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :adventurer/timer [_ {:keys [adventurer]}]
  (fn []
    (.play adventurer  "adventurer/attack")
    (.. adventurer -anims (chain "adventurer/idle"))))

(defmethod ig/init-key :my/run [_ {:essen/keys [this init] :as opts}]
  opts)

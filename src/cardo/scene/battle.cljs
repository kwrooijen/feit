(ns cardo.scene.battle
  (:require
   [essen.core :as essen :refer [emit!]]
   [integrant.core :as ig]))

(def monsters [:entity/yeti :entity/skeleton])

(defmethod ig/init-key :scene/battle [_ opts]
  (update opts :scene/entities conj
          (take 2 (random-sample 0.1 (flatten (repeat monsters))))))

(def config
  {[:essen/keyboard :keyboard/attack]
   {:keyboard/subs {:entity/monster [:component/stats]
                    :entity/player [:component/stats]}}

   [:essen/scene :scene/battle]
   {:scene/entities [:entity/player
                     :entity/yeti]
    :scene/keyboard {:down/p (ig/ref :keyboard/attack)}}})

(defmethod ig/init-key :keyboard/attack [_ {:context/keys [scene]}]
  (fn [{:context/keys [subs]}]
    (doseq [[k _] subs]
      (emit! scene k :handler.stats/attack {:event/damage 2}))))

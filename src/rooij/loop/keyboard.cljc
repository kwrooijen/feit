(ns rooij.loop.keyboard
  (:require
   [integrant.core :as ig]))

(defn- subs-states [{:scene/keys [entities]} {:keyboard/keys [subs]}]
  (apply merge
         (for [[key components] subs
               [derived-key opts] (ig/find-derived entities key)]
           {derived-key (select-keys (:entity/state opts) components)})))

(defn process [{:context/keys [scene] :as context} {:input-event/keys [key type]}]
  (when-let [keyboard-key (get-in scene [:scene/keyboard [type key]])]
    (-> context
        (assoc :context/subs (subs-states scene keyboard-key))
        ((:keyboard/fn keyboard-key)))))

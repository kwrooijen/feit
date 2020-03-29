(ns essen.system
  (:require
   [essen.util :refer [top-key derive-composite-all]]
   [essen.state :as state]
   [meta-merge.core :refer [meta-merge]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [parent parent? descendant?]]
   [integrant.core :as ig]))

(it/derive-hierarchy
 {:essen/scene [:essen/system]
  :essen/entity [:essen/system]
  :essen/component [:essen/system]
  :essen/handler [:essen/system]
  :essen/keyboard [:essen/system]
  :essen/reactor [:essen/system]
  :essen/ticker [:essen/system]})

(defmulti init-key
  "The init-key for essen system components. This is used internally by essen
  and should not be called directly."
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defn init
  "Starts an essen system (scene or entity). This is used internally by essen
  and should not be called directly."
  [config key]
  (ig/build config [key] init-key ig/assert-pre-init-spec ig/resolve-key))

(defn- first-gen-system? [k]
  (and (descendant? (top-key k) :essen/component)
       (->> (top-key k)
            (parent? :essen/component))))

(defn- second-gen-system? [k]
  (and (descendant? (top-key k) :essen/component)
        (->> (top-key k)
             (parent? :essen/component)
             (not))))

(defn- second-gen-systems [config]
  (remove first-gen-system?
          (it/find-derived-keys config :essen/component)))

(defn- first-gen-key [key]
  (loop [k (top-key key)]
    (cond
      (nil? k) k
      (first-gen-system? k) k
      :else (recur (parent k)))))

(defn- first-gen-system-value [config system-key]
  (->> (ig/find-derived config system-key)
       (remove (fn [[k _]] (some second-gen-system? k)))
       (last)
       (last)))

(defn- merge-parent-system
  [config k]
  (let [v1 (it/find-derived-value config k)
        v2 (first-gen-system-value config (first-gen-key k))]
    (assoc config k (meta-merge v2 v1))))

(defn- inherit-parent-systems [config]
  (reduce merge-parent-system config (second-gen-systems config)))

(defn prep
  "Prepares the config system with a composite derive on all keys. This is used
  internally by essen and should not be called directly."
  [config]
  (derive-composite-all config)
  (inherit-parent-systems (ig/prep config)))

(defn get-init-key [derived-k entity-opts]
  (if-let [f (get-method ig/init-key (ig/normalize-key derived-k))]
    (f derived-k entity-opts)
    (fn [v _] v)))

(defn get-halt-key [derived-k entity-opts]
  (if-let [f (get-method ig/halt-key! (ig/normalize-key derived-k))]
    (or (f derived-k entity-opts)
        (fn [_] nil))
    (fn [_] nil)))

(defn start [config]
  (reset! state/config (prep config))
  (-> @state/config
      (init [:essen/system])
      (->> (reset! state/system))))

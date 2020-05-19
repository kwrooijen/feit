(ns feit.system.core
  (:require
   [feit.config]
   [taoensso.timbre :as timbre]
   [feit.core.state :as state]
   [feit.core.util :refer [derive-all-composites derive-all-hierarchies]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(it/derive-hierarchy
 {:feit/scene [:feit/system]
  :feit/entity [:feit/system]
  :feit/component [:feit/system]
  :feit/handler [:feit/system]
  :feit/keyboard [:feit/system]
  :feit/reactor [:feit/system]
  :feit/ticker [:feit/system]})

(defmulti init-key
  "The init-key for feit system components. This is used internally by feit
  and should not be called directly."
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defn init
  "Starts an feit system (scene or entity). This is used internally by feit
  and should not be called directly."
  [config key]
  ;; TODO Add assertions
  (ig/build config [key] init-key (fn [_ _ _]) ig/resolve-key))

(defn prep
  "Prepares the config system with a composite derive on all keys. This is used
  internally by feit and should not be called directly."
  [config]
  (derive-all-hierarchies config)
  (derive-all-composites config)
  (ig/prep config))

(defn get-init-key
  ([derived-k] (get-init-key derived-k {}))
  ([derived-k opts]
   (if-let [f (get-method ig/init-key (#'ig/normalize-key derived-k))]
     f
     (if (:required? opts)
       (throw (ex-info (str "No ig/init-key found for key " derived-k) {:missing-key derived-k}))
       (fn [_ opts] opts)))))

;; TODO This is currently used for components.
;; Components needs to add a nested function. `opts` doesn't hold context.
;; This should be changed that opts is a merged map of state + context keys
;; (defmethod ig/halt-key! :graphics-2d.component/sprite [_ _opts]
;;   (fn [state]
;;     (halt! state)))
(defn get-halt-key [derived-k entity-opts]
  (if-let [f (get-method ig/halt-key! (#'ig/normalize-key derived-k))]
    (or (f derived-k entity-opts)
        (fn [_] nil))
    (fn [_] nil)))

(defn get-entity-halt-key [derived-k]
  (if-let [f (get-method ig/halt-key! (#'ig/normalize-key derived-k))]
    f
    (fn [_key _opts] nil)))

(defn start []
  (timbre/info ::start)
  (feit.config/reset-config! (prep @feit.config/config))
  (-> @feit.config/config
      (init [:feit/system])
      (->> (reset! state/system))))

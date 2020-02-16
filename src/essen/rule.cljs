(ns essen.rule
  (:require
   [integrant.core :as ig]))

(defn normalize-key [k]
  (if (vector? k) (ig/composite-keyword k) k))

(defmulti rule-prep
  (fn [key _ _]
    (normalize-key key)))

(defmethod rule-prep :ir/rule [[_ k] opts state]
  (swap! state assoc-in [:subs k] (:subs opts))
  opts)

(defmethod rule-prep :default [_ opts state]
  opts)

(defmulti rule-init
  (fn [key _ _] (normalize-key key)))

(defmethod rule-init :ir/rule [[_ k] opts state]
  (doseq [sub (get-in @state [:subs k])]
    (swap! state update-in [:rule sub] conj opts))
  opts)

(defmethod rule-init :ir/state [[_ k] opts state]
  opts)

(defmethod rule-init :default [_ opts state]
  opts)

(defn setup-rule
  ([system state]
   (setup-rule system state (keys system)))
  ([system state keys]
   (ig/build system keys (fn [key opts] (rule-init key opts state)))))

(defn new-state! [k opts state]
  (if-let [entity-state (k @state)]
    entity-state
    (let [entity-state (atom opts)]
      (swap! state assoc k entity-state)
      (add-watch entity-state k
                 (fn [_key _atom _old-state new-state]
                   (if (nil? new-state)
                     (do
                       (remove-watch entity-state k)
                       (swap! state assoc-in [:rule k] []))
                     (reduce
                      (fn [_ f]
                        ;; Whenever a rule changes the state of specific atom,
                        ;; we need to short-circuit this loop, because
                        ;; changing the atom will have triggered a new watch
                        ;; event. Rendering this loop useless.
                        (if (identical? @entity-state
                                        new-state)
                          (f)
                          (reduced nil)))
                      nil
                      (get-in @state [:rule k])))))
      entity-state)))

(defn setup
  ([config]
   (setup config (atom {})))
  ([config state]
   (doseq [[k v] config]
     (rule-prep k v state))
   ;; Shouldn't be a problem in CLJS, since we don't have multiple
   ;; threads. In Clojure this could be a problem.
   (defmethod ig/resolve-key :ir/state [[_ k] opts]
     (new-state! k opts state))
   {:state state
    :system (-> config
                (ig/prep)
                (ig/init)
                (setup-rule state))}))

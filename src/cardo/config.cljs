(ns cardo.config
  (:require
   [essen.core :as essen :refer [emit!]]
   [essen.system.ticker :as ticker]
   [integrant-tools.keyword :as it.keyword]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(def debug?
  ^boolean goog.DEBUG)

(def config
  {:game/renderer {:renderer/view "game"
                   :renderer/width (.-innerWidth js/window)
                   :renderer/height (.-innerHeight js/window)
                   :renderer/resolution (.-devicePixelRatio js/window)
                   :renderer/autoDencity true}

   [:essen/middleware :middleware.stats/invincible] {}

   [:essen/reactor :reactor.stats/dead?] {}

   [:essen/handler :handler.stats/attack]
   {:handler/middleware
    [(ig/ref :middleware.stats/invincible)]}

   [:essen/ticker :ticker.stats/poisoned]
   {:ticker/ticks 4
    :ticker/damage 3}

   [:essen/component :component/stats]
   {:component/handlers
    [(ig/ref :handler.stats/attack)]
    :component/reactors
    [(ig/ref :reactor.stats/dead?)]
    :component/tickers
    []}

   [:essen/component :component/equipment]
   {:component/handlers []
    :component/reactors []
    :component/tickers []}

   [:essen/entity :entity/monster :entity/yeti]
   {:entity/components
    [(ig/ref :component/stats)]}

   [:essen/entity :entity/monster :entity/skeleton]
   {:entity/components
    [(ig/ref :component/stats)
     (ig/ref :component/equipment)]}

   ^:persistent
   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component/stats)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}

   [:essen/keyboard :keyboard/attack]
   {:keyboard/subs {:entity/monster [:component/stats]
                    :entity/player [:component/stats]}}

   [:essen/scene :scene/start]
   {:scene/entities [(ig/ref :entity/player)
                     (it/child-ref :entity/yeti)
                     (it/child-ref :entity/yeti)
                     (ig/ref :entity/skeleton)]
    :scene/keyboard {:down/p (ig/ref :keyboard/attack)}}})

(defmethod ig/init-key :component/stats [_ opts]
  {:stats/hp 10})

(defmethod ig/init-key :component/equipment [_ opts]
  {:equipment/armor :cool})

(defmethod ig/init-key :ticker.stats/poisoned
  [_context {:ticker/keys [ticks damage]}]
  (let [remaining (atom ticks)
        last-time (atom (.now js/Date))
        poison-event {:event/damage damage
                      :event/damage-type :damage/poison}]
    (fn [{:context/keys [entity] :as context}  _delta time _state]
      (cond
        (zero? @remaining)
        (ticker/remove! context :ticker.stats/poisoned)

        (> (- time @last-time) 1000)
        (do (reset! last-time time)
            (swap! remaining dec)
            (emit! :scene/start entity :handler.stats/attack poison-event))))))

(defmethod ig/init-key :middleware.stats/invincible [_ _opts]
  (fn [_context event _state]
    (assoc event
           :event/damage 0
           :event/invincible? true)))

;; TODO Create spec check for events targetting handlers

(defmethod ig/init-key :reactor.stats/dead? [[_ k] opts]
  (fn [_context _event old-state state]
    (when (and (not (zero? (:stats/hp old-state)))
               (zero? (:stats/hp state)))
      ;; (essen/emit! k :handler.sprite/animation-dead)
      (println "YOU DIED"))
    state))

(defmethod ig/init-key :handler.stats/attack [_ opts]
  (fn [{:context/keys [entity subs]}
       {:event/keys [damage invincible?] :as _event}
       {:stats/keys [hp] :as state}]
    (if invincible?
      (println entity "was attacked, but" entity "is invincible!")
      (println "Attacked for " damage))
    (assoc state :stats/hp (max 0 (- hp damage)))))

(defmethod ig/init-key :keyboard/attack [_ opts]
  (fn [{:context/keys [subs]}]
    (doseq [[k _] subs]
      (emit! :scene/start k :handler.stats/attack {:event/damage 2}))))

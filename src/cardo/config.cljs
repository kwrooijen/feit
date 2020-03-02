(ns cardo.config
  (:require
   [essen.core :as essen :refer [emit!]]
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

   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component/stats)]}

   [:essen/scene :scene/start]
   {:scene/entities [(ig/ref :entity/player)]}})

(defmethod ig/init-key :component/stats [_ opts]
  {:stats/hp 10})

(defmethod ig/init-key :ticker.stats/poisoned
  [_ {:ticker/keys [ticks damage]}]
  (let [remaining (atom ticks)
        last-time (atom (.now js/Date))
        poison-event {:event/damage damage
                      :event/damage-type :damage/poison}]
    (fn [{:context/keys [entity]} _delta time _state]
      (cond
        ;; TODO (remove-ticker! context :ticker.stats/poisoned)
        (zero? @remaining) nil

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
  (fn [_context
       {:event/keys [damage invincible?] :as _event}
       {:stats/keys [hp] :as state}]
    (if invincible?
      (println "I was attacked, but I am invincible!")
      (println "Attacked for " damage))
    (assoc state :stats/hp (max 0 (- hp damage)))))

;; (comment
;;   ;; TODO: Entity collection?
;;   ;;       OR derived keys?
;;   ;; Entity     - [Component]
;;   ;; Component  - [Handler] [Reactor] [Ticker]
;;   ;; Handler    - fn [Middleware] [Link]
;;   ;; Link       - fn ; Links can be attached to handlers, to extend "generic"
;;   ;;                   components for specific needs
;;   ;; Middleware -
;;   ;; Reactor    -
;;   ;; Ticker     -
;;   )

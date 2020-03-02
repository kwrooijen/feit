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

   [:essen/middleware :middleware.stats/invincible]
   {:middleware/active? false}

   [:essen/reactor :reactor.stats/dead?] {}

   [:essen/handler :handler.stats/attack]
   {:handler/middleware
    [(ig/ref :middleware.stats/invincible)]}

   [:essen/component :component/stats]
   {:component/handlers
    [(ig/ref :handler.stats/attack)]
    :component/reactors
    [(ig/ref :reactor.stats/dead?)]}

   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component/stats)]}

   [:essen/scene :scene/start]
   {:scene/entities [(ig/ref :entity/player)]}})

(defmethod ig/init-key :component/stats [_ opts]
  {:stats/hp 10})

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
    (when (and (> hp 0) (not invincible?))
      (emit! :entity/player :handler.stats/attack {:event/damage 2}))
    (assoc state :stats/hp (max 0 (- hp damage)))))

;; (comment
;;   (let [scene (atom {:entity/player (init-entity config :entity/player)})]
;;     ;; (emit! :entity/player :handler.stats/attack {:event/damage 5})
;;     (reset! messages [])
;;     (emit! :entity/player :handler.stats/attack {:event/damage 5})
;;     (swap! scene run-messages))

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

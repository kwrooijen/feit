(ns cardo.component.stats
  (:require
   [cardo.reactor.stats]
   [cardo.ticker.stats]
   [cardo.handler.stats]
   [cardo.middleware.stats]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]))

(defmethod ig/prep-key :component/stats [_ opts]
  (meta-merge opts
   {:component/handlers
    [(ig/ref :handler.stats/attack)]

    :component/reactors
    [(ig/ref :reactor.stats/dead?)]

    :component/tickers
    []}))

(defmethod ig/init-key :component/stats [_ {:stats/keys [hp]}]
  {:stats/hp (* 2.3 hp)})

(def config
  {[:essen/handler :handler.stats/attack]
   {:handler/middleware []}
   [:essen/component :component/stats] {}

   [:essen/ticker :ticker.stats/poisoned] {}
   ;; (ig/ref :middleware.stats/invincible)

   [:essen/reactor :reactor.stats/dead?] {}})
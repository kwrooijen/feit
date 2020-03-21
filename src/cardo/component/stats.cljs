(ns cardo.component.stats
  (:require
   [cardo.reactor.stats]
   [cardo.ticker.stats]
   [cardo.handler.stats]
   [cardo.middleware.stats]
   [integrant.core :as ig]))

(defmethod ig/init-key :component/stats [_ {:stats/keys [hp]}]
  {:stats/hp (* 2.3 hp)})

(def config
  {[:essen/component :component/stats]
   {:component/handlers [(ig/ref :handler.stats/attack)]
    :component/reactors [(ig/ref :reactor.stats/dead?)]
    :component/tickers  []}

   [:essen/handler :handler.stats/attack]
   {:handler/middleware []}

   [:essen/ticker :ticker.stats/poisoned] {}

   [:essen/reactor :reactor.stats/dead?] {}})

(ns essen.loop.middleware)

(defn- middlewares [{:context/keys [component handler-key]} ]
  (get-in component [:component/handlers handler-key :handler/middleware]))

(defn- process-event-content-reducer [ctx event [_ middleware]]
  ((:middleware/fn middleware) ctx event))

(defn- process-event-content [content ctx]
  (reduce (partial process-event-content-reducer ctx)
          content
          (middlewares ctx)))

(defn process [ctx]
  (update ctx :context/event process-event-content ctx))

(ns rooij.loop.middleware)

(defn- middlewares [{:context/keys [component handler-key]} ]
  (get-in component [:component/handlers handler-key :handler/middleware]))

(defn- process-event-content-reducer [context event [_ middleware]]
  ((:middleware/fn middleware) context event))

(defn- process-event-content [content context]
  (reduce (partial process-event-content-reducer context)
          content
          (middlewares context)))

(defn process [[scene context]]
  [scene (update context :context/event process-event-content context)])

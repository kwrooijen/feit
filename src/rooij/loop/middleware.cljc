(ns rooij.loop.middleware)

(defn has-handler?
  ([handler-key] #(has-handler? handler-key %))
  ([handler-key middleware]
   (some #{handler-key} (:middleware/handlers middleware))))

(defn- middlewares [{:context/keys [component handler-key]} ]
  (->> (:component/middlewares component)
       (filter (comp (has-handler? handler-key)
                     last))
       (into {})))

(defn- process-event-content-reducer [context event [_ middleware]]
  ((:middleware/fn middleware) context event))

(defn- process-event-content [content context]
  (reduce (partial process-event-content-reducer context)
          content
          (middlewares context)))

(defn process [[scene context]]
  [scene (update context :context/event process-event-content context)])

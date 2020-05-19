(ns feit.loop.middleware)

(defn- sort-by-priority [middlewares]
  (sort-by (comp :middleware/priority second) middlewares))

(defn- ^boolean has-handler?
  ([handler-key] #(has-handler? handler-key %))
  ([handler-key middleware]
   (some #{handler-key} (:middleware/handlers middleware))))

(defn- filter-handler-middlewares [handler-key middlewares]
  (filter (comp (has-handler? handler-key) second) middlewares))

(defn- middlewares [{:context/keys [component handler-key]} ]
  (->> (:component/middlewares component)
       (filter-handler-middlewares handler-key)
       (sort-by-priority)))

(defn- process-event-content-reducer [context event [_ middleware]]
  ((:middleware/fn middleware) context event))

(defn- process-event-content [content context]
  (reduce (partial process-event-content-reducer context)
          content
          (middlewares context)))

(defn process [[scene context]]
  [scene (update context :context/event process-event-content context)])

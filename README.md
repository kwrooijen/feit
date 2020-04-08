# Rooij - Modular, Data driven Game Engine

Heavily WIP, not for use yet, no documentation at the moment

## Concept

This project is built using Integrant. The idea is that you define your game
*Structure* using a single data structure (multiple EDN files merged). This data
structure is composed of Integrant keys, and the functionality is implemented in
multimethods (it's useful to know how Integrant itself works).

This engine provides the following top level keys to build your game:

### Scene

A scene is the current context of the game, and where drawing / physics happen.
For example the menu screen, battle scene, game over scene. You can also layer
scenes on top of each other (for example a play scene + UI scene). A scene can
have multiple entities nested in it.

### Entity

An entity is something that lives in your scene. However by itself it's an empty
shell with only an init function. In order to add functionality to an entity, we
add Components.

### Component

A component holds state, nothing more. For example it could hold the current
health of a player, or the position. Components can have Handlers, Middleware,
Reactors and Tickers to add behavior (again, a component itself is only state).


### Handler

A handler receives events and changes the state of the component. A component
can have multiple handlers. For example a health component could have a "heal"
and "damage" hander. One to increase the current health, the other to lower it.

### Middleware

Middleware modifies events before being sent to a handler. This is useful if you
want to add new features to a component, but don't want to raise the complexity
of the handler.

For example if you were to add an invincibility feature to the
health component, you could write an if statement in your damage handler, and
not apply the event. Instead of adding more functionality to the handler, you
can create an invinciblity middleware, which decreases the event's damage to 0.

### Reactor

Reactors are triggered when component state changes. For example if the health
component reaches 0, the entity is considered dead. You could trigger an
animation using a reactor. The main purpose of this tool is also to reduce
complexity of handlers.

### Ticker

Tickers are pieces of functionality that happens over time. For example you
could have a poison ticker that damages the health component over time. Or a
regeneration ticker that heals every second.

### Keyword Hierarchy

Keyword hierarchy is used to group entities together. For example you might want
to reference all monsters in a certain scene. But you could have many different
kinds of monsters. As long as all of these are descendants of `:entity/monster`,
for example, you could lookup all of them through that specific keyword. This
method is also used to share state, emit events to multiple entities, and more.

---

## Project code structure

This project is structured in 3 (or 4) parts.

### System

Under the `rooij.system` namespace, all system parts are defined. Scene, Entity,
Component, Handler, Middleware, Reactor, Ticker. System is responsible for
setting up your game, it's the first thing that happens. Once this is setup, it
won't be referred again, it's purely to bootstrap your system. This is mainly
where all the Integrant logic lives.


### Loop

In the loop namespace is all the game loop logic. Handling keyboard events,
running tickers, handling messages and going through the flow (middleware ->
handler -> reactor).

After the data processing part it runs the configured graphics + physics engine.

### Interface

The interface namespace is the abstraction layer for Graphics and Physics
engines. It defines schemas of entities, components, handlers, middleware,
reactors, and tickers, that third party Graphics and Physics engines should
implement. These schemas are defined using Malli. The idea is that we can
implement these schemas in both java and javascript libraries, making it
platform agnostic.

### (extra) Module

The module namespace contains 3rd party modules that implement these schemas.
These shouldn't be in the Rooij project, but this is for development purposes.

---

## Code usage

### Data implementation

Here's a simple example how we could describe a new "health" component. This
component has a handler to apply damage, a reactor to check if health reached
zero, a ticker to apply poison damage over time, and a middleware to reduce
damage to 0 (invincibility).

``` clojure
{[:rooij/component :component/health]
 {:component/handlers [#ig/ref :handler.health/damage]
  :component/reactors [#ig/ref :reactor.health/dead?]
  :component/ticker   [#ig/ref :ticker.health/poisoned]}

 [:rooij/handler :handler.health/damage]
 {:handler/middleware [#ig/ref :middleware.health/invinciblity]}

 [:rooij/middleware :middleware.health/invincible] {}

 [:rooij/ticker :ticker.health/poisoned] {}

 [:rooij/reactor :reactor.health/dead?] {}}
```

### Code implementation

Implementation of the health component. All it does is return a piece of state,
based on the options given.

``` clojure
(defmethod ig/init-key :component/health
  [_ {:health/keys [amount]}]
  {:component/health amount})
```


Implementation of the damage handler. It receives an event and update the
`:component/health` state, reducing it by the amount of the event.

``` clojure
(defmethod ig/init-key :handler.health/damage [_ _opts]
  (fn handler-health--damage
    [_context event state]
    (update state :component/health - (:event/damage event))))
```

Implementation of the dead? reactor. Whenever the component state changes, it
checks if the state is zero. If that happens then it emits a new events to play
an animation.

``` clojure
(defmethod ig/init-key :reactor.health/dead? [_ _opts]
  (fn reactor-health--dead?
    [{:context/keys [scene-key entity-key component-key]} _old-state new-state]
    (when (zero? (:component/health new-state))
      (rooij/emit! {:event/scene scene-key
                    :event/entity entity-key
                    :event/handler :animation/dead}))))
```

Implementation of the invincibility middleware. When active, any events received
will reduce the damage to 0.

``` clojure
(defmethod ig/init-key :middleware.health/invincible [_ _opts]
  (fn middleware-health--invincible
    [_subs event _state _entity-state]
    (assoc event :event/damage 0)))

```

Implementation of the poisoned ticker. This is a bit more complex than the rest.
It keeps track of the last time it applied damage, and emits a damage event
every second.

``` clojure
(defmethod ig/init-key :ticker.health/poisoned
  [_key {:ticker/keys [ticks damage] :context/keys [scene-key entity-key]}]
  (let [remaining (atom ticks)
        last-time (atom 0)
        poison-event {:event/damage damage
                      :event/damage-type :damage/poison}]
    (fn ticker-stats--poisoned
      [{:context/keys [component-key time]} _state]
      (cond
        (zero? @remaining)
        (ticker/remove! scene-key entity-key component-key :ticker.stats/poisoned)
        (> (- time @last-time) 1000)
        (do (reset! last-time time)
            (swap! remaining dec)
            (rooij/emit!
             {:event/scene scene-key
              :event/entity entity-key
              :event/handler :handler.health/damage
              :event/content poison-event}))))))
```

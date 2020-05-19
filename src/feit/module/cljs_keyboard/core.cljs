(ns feit.module.cljs-keyboard.core
  (:require
   [integrant.core :as ig]
   [feit.keyboard]
   [feit.module.cljs-keyboard.interface :refer [->CljsKeyboard]]))

(defn- body-active?
  "Check if the user is currently focused on the body. use `identical?` to
  compare memory addresses. This is faster than using `=`.

  TODO: Will this still work if the user embeds the game in their website? Or
  should the user be able to choose the focused element? Maybe a different solution?"
  []
  (identical? (.-body js/document)
              (.-activeElement js/document)))

(defn- valid-event? [event]
  (and (body-active?)
       (not (.-repeat event))))

(defn- get-key [event]
  (get feit.keyboard/keys (.-keyCode event)))

(defn- add-event-listener
  "Add an event listener to the body. Listen to `trigger` and execute `callback`.

  This is useful for checking keydown / keyup
  events, while ignoring these event within input fields. This can be used in
  conjunction with `emit-keydown!` and emit-keyup!`

  Example:

  ```clojure
  (feit.keyboard/add-event-listener \"keydown\" es/emit-keydown!)
  (feit.keyboard/add-event-listener \"keyup\" es/emit-keyup!)
  ```

  TODO: when keydown is triggered, and body loses focus, keyup is never emitted.
  Is this an issue, and if so, how do we fix it?
  "
  [trigger callback]
  (.addEventListener (js/document.querySelector "body") trigger
                     #(when (valid-event? %)
                        (callback %))))

(defn add-event-listeners! []
  (add-event-listener "keydown" (comp feit.keyboard/emit-keydown! get-key))
  (add-event-listener "keyup"   (comp feit.keyboard/emit-keyup!   get-key)))

(defn disable-tabbing!
  "This disables the tabbing of HTML elements. This ensures that the user can't
  accidentally tab focus away from the body, which we use to listen to key
  events. We only listen to the body so that input fields don't trigger events."
  []
  (let [tab-key 9]
    (set! (.-onkeydown js/document)
          #(not= tab-key (.-which %)))))

(defmethod ig/init-key :feit.interface.keyboard/system [_ init-opts]
  (add-event-listeners!)
  (disable-tabbing!)
  (->CljsKeyboard init-opts))

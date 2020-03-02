(ns essen.keyboard)

;; https://keycode.info/
;; TODO complete this list
(def event-keys
  {3 :key/break
   8 :key/backspace
   9 :key/tab
   12 :key/clear
   13 :key/enter
   16 :key/shift
   17 :key/ctrl
   18 :key/alt
   19 :key/pause
   20 :key/caps-lock
   21 :key/hangul
   25 :key/hanja
   27 :key/escape
   28 :key/conversion
   29 :key/non-conversion
   32 :key/spacebar
   33 :key/page-up
   34 :key/page-down
   35 :key/end
   36 :key/home
   37 :key/arrow-left
   38 :key/arrow-up
   39 :key/arrow-right
   40 :key/arrow-down
   41 :key/select
   42 :key/print
   43 :key/execute
   44 :key/print-screen
   45 :key/insert
   46 :key/delete
   47 :key/help
   48 :key/n-0
   49 :key/n-1
   50 :key/n-2
   51 :key/n-3
   52 :key/n-4
   53 :key/n-5
   54 :key/n-6
   55 :key/n-7
   56 :key/n-8
   57 :key/n-9
   58 :key/colon
   60 :key/less-than
   65 :key/a
   66 :key/b
   67 :key/c
   68 :key/d
   69 :key/e
   70 :key/f
   71 :key/g
   72 :key/h
   73 :key/i
   74 :key/j
   75 :key/k
   76 :key/l
   77 :key/m
   78 :key/n
   79 :key/o
   80 :key/p
   81 :key/q
   82 :key/r
   83 :key/s
   84 :key/t
   85 :key/u
   86 :key/v
   87 :key/w
   88 :key/x
   89 :key/y
   90 :key/z
   91 :key/super-left
   93 :key/super-right
   95 :key/sleep
   96 :key/numpad-0
   97 :key/numpad-1
   98 :key/numpad-2
   99 :key/numpad-3
   100 :key/numpad-4
   101 :key/numpad-5
   102 :key/numpad-6
   103 :key/numpad-7
   104 :key/numpad-8
   105 :key/numpad-9
   106 :key/multiply
   107 :key/add
   109 :key/subtract
   110 :key/decimal-point
   111 :key/slash
   112 :key/f1
   113 :key/f2
   114 :key/f3
   115 :key/f4
   116 :key/f5
   117 :key/f6
   118 :key/f7
   119 :key/f8
   120 :key/f9
   121 :key/f10
   122 :key/f11
   123 :key/f12
   124 :key/f13
   125 :key/f14
   126 :key/f15
   127 :key/f16
   128 :key/f17
   129 :key/f18
   130 :key/f19
   131 :key/f20
   132 :key/f21
   133 :key/f22
   134 :key/f23
   135 :key/f24
   160 :key/caret
   161 :key/exclamation-mark
   163 :key/hash
   164 :key/dollar})

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

(defn get-key [event]
  (get event-keys (.-keyCode event) (.-keyCode event)))

(defn add-event-listener
  "Add an event listener to the body. Listen to `trigger` and execute `callback`.

  This is useful for checking keydown / keyup
  events, while ignoring these event within input fields. This can be used in
  conjunction with `emit-keydown!` and emit-keyup!`

  Example:

  ```clojure
  (essen.keyboard/add-event-listener \"keydown\" es/emit-keydown!)
  (essen.keyboard/add-event-listener \"keyup\" es/emit-keyup!)
  ```

  TODO: when keydown is triggered, and body loses focus, keyup is never emitted.
  Is this an issue, and if so, how do we fix it?
  "
  [trigger callback]
  (.addEventListener (js/document.querySelector "body") trigger
                     #(when (valid-event? %)
                        (callback %))))

(defn disable-tabbing!
  "This disables the tabbing of HTML elements. This ensures that the user can't
  accidentally tab focus away from the body, which we use to listen to key
  events. We only listen to the body so that input fields don't trigger events."
  []
  (let [tab-key 9]
    (set! (.-onkeydown js/document)
          #(not= tab-key (.-which %)))))

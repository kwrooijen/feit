(ns essen.module.pixi.component.sprite
  (:require
   [clojure.spec.alpha :as s]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]))

(defmethod ig/pre-init-spec :component.pixi/sprite [_]
  (s/keys :req [:component.pixi/spritesheet]))

(defmethod ig/prep-key :component.pixi/sprite [_ opts]
  (meta-merge opts
         {:component/handlers [(ig/ref :handler.pixi.sprite/play)]}))

(defmethod ig/init-key :component.pixi/sprite
  [_ {:pixi.sprite/keys [spritesheet] :as opts}]
  {:ok 1})

(defmethod ig/init-key :handler.pixi.sprite/play [_ opts]
  (fn [_ _ state]
    (println "pixi state! " state)
    state))

;; TODO Modules should get a :module/components key (maybe). which makes sure this
;; gets derived. It's probably not logical to define the components here.
(derive :component.pixi/sprite :essen/component)

(def config
  {[:essen/handler :handler.pixi.sprite/play] {}})

(ns rooij.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def :context/scene-key qualified-keyword?)
(s/def :context/entity-key qualified-keyword?)
(s/def :context/component-key qualified-keyword?)

(s/def :rooij/context
  (s/keys :opt [:context/scene-key
                :context/entity-key
                :context/component-key]))

(ns essen.interface.graphics-2d.entity
  (:require
   [essen.methods :as es]))

(defmethod es/ui-key :graphics-2d.entity/asset-loader [_ _schema]
  [:div])

(defmethod es/doc-key :graphics-2d.entity/asset-loader [_]
  "Entity to load assets.")

(defmethod es/schema-key :graphics-2d.entity/asset-loader [_]
  [:map
   [:g2d.asset-loader/files [:vector {:file-names? true} 'string?]]
   [:g2d.asset-loader/prefix 'string?]
   [:g2d.asset-loader/next-scene 'qualified-keyword?]])

(ns edomus.impl.style
  (:require [edomus.core :as core]))

(defn set-style! [style k v]
  (if (core/important? v)
    (.setProperty style (name k)
                  (core/important-value v)
                  "important")
    (.setProperty style (name k) v)))

(defn remove-style! [style k]
  (.removeProperty style (name k)))

(defn get-style [style k]
  (let [k (name k)
        imp? (= (.getPropertyPriority style k)
                "important")
        v (.getPropertyValue style k)]
    (if imp?
      (core/important v)
      v)))


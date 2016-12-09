(ns edomus.impl.create)

(def ^{:private true
       :arglists '([element name])}
  js-contains?
  (js* "function (e, n) { return (n in e); }"))

(def create-options
  (memoize (fn [options]
             (when-let [is (:is options)]
               #{"is" is}
               nil))))

(defn element [document type & [options]]
  (.createElement document type (create-options options)))

(defn element-ns [document ns name & [options]]
  (.createElementNS document ns name (create-options options)))
   
(defn text-node [document text]
  (.createTextNode document text))

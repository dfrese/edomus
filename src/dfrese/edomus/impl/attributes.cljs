(ns dfrese.edomus.impl.attributes)

(defn remove-attribute [element k]
  (let [ks (name k)]
    ;; TODO: some browsers (Chrome) complains if removing an undefined attribute
    (if (map? element)
      (assoc element ks nil)
      (do (.removeNamedItem (.-attributes element) ks)
          element))))

(defn set-attribute [element k v]
  (if (nil? v)
    (remove-attribute element k)
    (let [ks (name k)]
      (if (map? element)
        (assoc element ks v)
        (do
          (.setAttribute element ks v)
          element)))))

(defn has-attribute? [element k]
  (let [ks (name k)]
    (if (map? element)
      (not= js/undefined (get element ks js/undefined))
      (.hasAttribute element ks))))

(defn get-attribute [element k]
  (let [ks (name k)]
    (if (map? element)
      (get element ks)
      (.getAttribute element ks))))

(defn attribute-map [element]
  (persistent! (reduce (fn [r attr]
                         (assoc! r (.-name attr)
                                 (.-value attr)))
                       (transient {})
                       (array-seq (.-attributes element)))))

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
          (if-let [a (.getNamedItem (.-attributes element) ks)]
            (set! (.-value a) v)
            (let [document (.-ownerDocument element)]
              (.setNamedItem (.-attributes element)
                             (let [a (.createAttribute document ks)]
                               (set! (.-value a) v)
                               a))))
          element)))))

(defn has-attribute? [element k]
  (let [ks (name k)]
    (if (map? element)
      (not= js/undefined (get element ks js/undefined))
      (some? (.getNamedItem (.-attributes element) ks)))))

(defn get-attribute [element k]
  (let [ks (name k)]
    (if (map? element)
      (get element ks)
      (when-let [a (.getNamedItem (.-attributes element) ks)]
        (.-value a)))))

(defn attribute-map [element]
  (persistent! (reduce (fn [r attr]
                         (assoc! r (.-name attr)
                                 (.-value attr)))
                       (transient {})
                       (array-seq (.-attributes element)))))

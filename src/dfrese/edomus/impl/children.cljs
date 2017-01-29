(ns dfrese.edomus.impl.children)

(defn append-child [base node]
  (if (vector? base)
    (conj base node)
    (do (.appendChild base node)
        base)))

(defn- child-pos [base node]
  (loop [idx 0]
    (if (= idx (count base))
      -1
      (if (identical? node (nth base idx))
        idx
        (recur (inc idx))))))

(defn- contains-child? [base node]
  (if (vector? base)
    (> (child-pos base node) 0)
    (identical? base (.-parentNode node))))

(defn actually-insert-before [base node ref-node]
  (when (contains-child? base node)
    ;; Note: DOM allows it, unless really needed, this is too much for us (not that the count increment it not correct then too)
    (throw (new js/Error "The node to be inserted is already a child. Use [[move-before]] instead.")))
  (if (vector? base)
    ;; if node is a child already, this is equiv to moving the child
    (let [pos (child-pos base ref-node)]
      (when-not (< (child-pos base node) 0)
        (new js/Error "The node to be inserted is a child of this node already.")) ;; not sure; do an implicit move then?
      (cond
        (< pos 0)
        (throw (new js/Error "The node before which the new node is to be inserted is not a child of this node."))
        (= pos 0)
        (apply vector node base)
        :else
        (vec (concat (subvec base 0 pos)
                     (cons node (subvec base pos))))))
    (do (.insertBefore base node ref-node)
        base)))

(defn insert-before [base node ref-node]
  (if (nil? ref-node)
    (append-child base node)
    (actually-insert-before base node ref-node)))

(defn remove-child [base node]
  (if (vector? base)
    (let [pos (child-pos base node)]
      (cond
        (< pos 0)
        (throw (new js/Error "The node to be removed is not a child of this node."))
        (= pos 0)
        (subvec base 1)
        (= pos (dec (count base)))
        (subvec base 0 pos)
        :else
        (vec (concat (subvec base 0 pos)
                     (subvec base (inc pos))))))
    (do (.removeChild base node)
        base)))

;; TODO: worth optimizing? insertBefore does this automatically..
(defn move-before [base node ref-node]
  (-> base
      (remove-child node)
      (insert-before node ref-node)))

;; TODO: worth optimizing?
(defn replace-child [base new-node old-node]
  (-> base
      (insert-before new-node old-node)
      (remove-child old-node)))

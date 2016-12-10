(ns edomus.impl.batch
  (:require [edomus.impl.style :as style]
            [edomus.impl.children :as children]
            [edomus.impl.attributes :as attributes]))

(def empty-batch {})

(defn update-properties [batch element f & args]
  (update batch element (fn [updates]
                          (apply f (or updates {}) args))))

(defn get-properties [batch element]
  (get batch element))

;; Attributes

(defn update-attributes [batch element f & args]
  (update-properties batch element
                     update "attributes"
                     (fn [updates]
                       (conj (or updates []) #(apply f % args)))))

(defn apply-attributes [base updates]
  (reduce #(%2 %1)
          base
          updates))

(defn get-attribute [batch element name]
  (if-let [updates (get (get-properties batch element) "attributes")]
    (let [m (-> (attributes/attribute-map element)
                (apply-attributes updates))]
      (attributes/get-attribute m name))
    (attributes/get-attribute element name)))

(defn has-attribute? [batch element name]
  (if-let [updates (get (get-properties batch element) "attributes")]
    (let [m (-> (attributes/attribute-map element)
                (apply-attributes updates))]
      (attributes/has-attribute? m name))
    (attributes/has-attribute? element name)))

;; Styles

(defn update-style [batch element f & args]
  (update-properties batch element
                     update "style"
                     (fn [updates]
                       (conj (or updates []) #(apply f % args)))))

(defn create-style [document]
  ;; damn it, isn't there a better way...? :-/
  (let [e (.createElement document "div")]
    (.-style e)))

(defn copy-style [element]
  (let [new-st (create-style (.-ownerDocument element))]
    (set! (.-cssText new-st) (.-cssText (.-style element)))
    new-st))

(defn apply-style! [style updates]
  (doseq [u updates]
    (u style)))

(defn get-style [batch element k]
  (if-let [updates (get (get-properties batch element) "style")]
    ;; we operate on a copied style object, to have the 'css knowledge' on how styles correlate to each other.
    (let [style (copy-style element)]
      (apply-style! style updates)
      (style/get-style style k))
    (style/get-style (.-style element) k)))

;; Children

(defn update-children [batch element f & args]
  (update-properties batch element
                     update "childNodes"
                     (fn [updates]
                       ;; Note: the update fns must work on vectors as well as elements.
                       (conj (or updates []) #(apply f % args)))))

(defn apply-child-updates [base updates]
  (reduce #(%2 %1)
          base
          updates))

(defn get-children [batch element]
  (let [base (vec (array-seq (.-childNodes element)))]
    (if-let [updates (get (get-properties batch element) "childNodes")]
      (apply-child-updates base updates)
      base)))

;; Classes

(defn update-classes [batch element f arg]
  (update-properties batch element
                     update "classList"
                     (fn [classes]
                       (f (or classes (set (array-seq (.-classList element))))
                          arg))))

(defn apply-class-updates! [element classes] ;; is it worth to do something more complicated?
  ;; remove all not there anymore
  (.apply (.-remove (.-classList element))
          (.-classList element)
          (apply array (filter #(not (contains? classes %))
                               (array-seq (.-classList element)))))
  ;; set all new
  (.apply (.-add (.-classList element))
          (.-classList element)
          (apply array classes)))

(defn get-classes [batch element]
  (let [classes (get (get-properties batch element) "classList")]
    (or classes (set (array-seq (.-classList element))))))

(defn contains-class? [batch element k]
  (if-let [classes (get (get-properties batch element) "classList")]
    (contains? classes k)
    (.contains (.-classList element) k)))

;; Commiting

(defn apply-property! [element k v]
  (case k
    "attributes" (do (apply-attributes element v) nil)
    "style" (apply-style! (.-style element) v)
    "childNodes" (do (apply-child-updates element v) nil)
    "classList" (do (apply-class-updates! element v) nil)
    (do (aset element (name k) v) nil)))

(defn apply-properties! [element updates]
  (reduce-kv #(apply-property! element %2 %3) nil updates))

(defn apply-batch! [batch]
  ;; TODO: any reasonably good order to apply the props?
  (reduce-kv #(apply-properties! %2 %3)
             nil
             batch))

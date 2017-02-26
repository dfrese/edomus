(ns dfrese.edomus.virtual
  "Defines a virtual execution context. An extension allows to get a hiccup representations of elements."
  (:require #?(:clj [dfrese.edomus.impl.commands :as cmd])
            #?(:cljs [dfrese.edomus.impl.commands :as cmd :include-macros true])
            [dfrese.edomus.core :as core]
            [clojure.string :as string]
            [dfrese.edomus.impl.html5 :as html5]))

;; Need to have objects with identity, to store their props in a map, and to reference them as children.
(deftype ^:no-doc Element [ns name is]
         ;;#?@(:cljs [Object (toString [this] (str "#element{" (.-name this) "}"))])
         )
(deftype ^:no-doc Text [])

(cmd/defcmd to-hiccup
  [element]
  "Returns a hiccup representation of the given
  dom element. Note that hiccup represents HTML, which does not fully
  cover the DOM API. This assumes that the hiccup representation is
  later used to render a HTML5 document.")

(defn- compile-to-hiccup [elements element]
  (cond
    (instance? Element element)
    (let [props (get elements element)
          tag (.-name element)
          ns (.-ns element)
          attrs (reduce-kv (fn [m k v]
                             (case k
                               "childNodes" m ;; handled below
                               (html5/add-property m tag ns k v))
                             )
                           (cond-> {}
                             (.-is element) (assoc :is (.-is element)))
                           props)]
      (apply vector
             (keyword (string/lower-case (.-name element))) ;; TODO: namespaces in hiccup?
             (cond->> (map (partial compile-to-hiccup elements)
                           (get props "childNodes"))
               (not-empty attrs) (cons attrs))))
    (instance? Text element)
    (get elements element)))

(def ^:private empty-state {})

(defn- create-element-ns [elements document ns name & [options]]
  (assert name)
  (let [e (Element. ns
                    (if (= ns core/html-ns) ;; TODO: also svg and math?
                      (string/upper-case name)
                      name)
                    (:is options))]
    [e (assoc elements e html5/default-element-props)]))

(defn execute
  "Runs `(apply f args)` in a context, where the core functions are
  implemented against a virtual in-memory dom."
  [f & args]
  (let [sta (atom empty-state)
        state-m (fn [f]
                  (fn [& args]
                    (let [[res st] (apply f @sta args)]
                      (reset! sta st)
                      res)))]
    (binding
        [to-hiccup
         (state-m (fn [elements element]
                    [(compile-to-hiccup elements element) elements]))

         core/document nil
         core/element-owner-document (constantly nil)

         ;; Creation
         core/create-element
         (state-m (fn [elements document type & [options]]
                    (create-element-ns elements document core/html-ns type options)))

         core/create-element-ns
         (state-m create-element-ns)
  
         core/element?
         (state-m (fn [elements v]
                    [(instance? Element v) elements]))

         core/element-name
         (state-m (fn [elements v]
                    [(.-name v) elements]))

         core/element-namespace
         (state-m (fn [elements v]
                    [(.-ns v) elements]))

         core/create-text-node
         (state-m (fn [elements document text]
                    (let [e (Text.)]
                      [e (assoc elements e text)])))

         core/text-node?
         (state-m (fn [elements v]
                    [(instance? Text v) elements]))

         core/text-node-value
         (state-m (fn [elements v]
                    [(get elements v) elements]))

         core/set-text-node-value!
         (state-m (fn [elements v text]
                    [nil (assoc elements v text)]))

         ;; Properties

         core/has-property?
         (state-m (fn [elements element k]
                    [(not= core/undefined (get-in elements [element (name k)] core/undefined))
                     elements]))
   
         core/get-property
         (state-m (fn [elements element k]
                    [(get-in elements [element (name k)] core/undefined) elements]))
   
         core/set-property!
         (state-m (fn [elements element k v]
                    [nil (assoc-in elements [element (name k)] v)]))

         core/remove-property!
         (state-m (fn [elements element k]
                    [nil (update elements element dissoc (name k))]))

         ;; Attributes

         core/has-attribute?
         (state-m (fn [elements element k]
                    [(some? (get-in elements [element "attributes" (name k)] nil))
                     elements]))

         core/get-attribute
         (state-m (fn [elements element k]
                    [(get-in elements [element "attributes" (name k)] nil) elements]))

         core/set-attribute!
         (state-m (fn [elements element k v]
                    [nil (assoc-in elements [element "attributes" (name k)] v)]))

         core/remove-attribute!
         (state-m (fn [elements element k]
                    [nil (update-in elements [element "attributes"] dissoc (name k))]))

         ;; Style

         core/get-style
         (state-m (fn [elements element k]
                    [(get-in elements [element "style" (name k)] "") elements]))

         core/set-style!
         (state-m (fn [elements element k v]
                    [nil (update-in elements [element "style"]
                                    assoc (name k) v)]))

         core/remove-style!
         (state-m (fn [elements element k] ;; dissoc would be wrong...   TODO: why?
                    [nil (update-in elements [element "style"] assoc (name k) "")]))

         ;; Children

         ;; TODO: reuse impl/children
         core/child-nodes
         (state-m (fn [elements element]
                    [(get-in elements [element "childNodes"]) elements]))

         core/append-child!
         (state-m (fn [elements element node]
                    [nil (update-in elements [element "childNodes"]
                                    conj node)]))

         core/remove-child!
         (state-m (fn [elements element node]
                    ;; TODO: throw if node is not a child
                    [nil (update-in elements [element "childNodes"]
                                    (fn [v] (vec (remove #(identical? node %) v))))]))

         core/insert-before!
         (state-m (fn [elements element node ref]
                    ;; TODO: make it an exn
                    (assert (some #(identical? ref %) (get-in elements [element "childNodes"]))
                            (str "Reference node " ref " not in children: " (get-in elements [element "childNodes"])))
                    [nil (update-in elements [element "childNodes"]
                                    (fn [v] (vec (mapcat #(if (identical? ref %)
                                                            [node ref]
                                                            [ref])
                                                         v))))]))

         core/replace-child!
         (state-m (fn [elements element node old-node]
                    ;; TODO: make it an exn
                    (assert (some #(identical? old-node %) (get-in elements [element "childNodes"]))
                            (str "Old node " old-node " not in children: " (get-in elements [element "childNodes"])))
                    [nil (update-in elements [element "childNodes"]
                                    (fn [v] (mapv #(if (identical? old-node %)
                                                     node
                                                     %)
                                                  v)))]))

         ;; Classes

         ;; TODO
         core/classes
         (state-m (fn [elements element]
                    [(get-in elements [element "classList"]) elements]))

         core/contains-class?
         (state-m (fn [elements element name]
                    [(contains? (get-in elements [element "classList"]) name) elements]))

         core/add-class!
         (state-m (fn [elements element name]
                    [nil (update-in elements [element "classList"] conj name)]))

         core/remove-class!
         (state-m (fn [elements element name]
                    [nil (update-in elements [element "classList"] disj name)]))

         core/toggle-class!
         (state-m (fn [elements element name]
                    [nil (update-in elements [element "classList"] #(if (contains? % name) (disj % name) (conj % name)))]))

         ;; hiccup does not support event handling
         core/add-event-listener!
         (fn [element type listener & [options]] nil)
         
         core/remove-event-listener!
         (fn [element type listener & [options]] nil)
         ]
      (apply f args))))

#_(defn with-new-document
  [f & args]
  (execute (fn []
             (let [doc core/document
                   body (core/create-element doc "body")]
               (to-hiccup (apply f args))))))

(ns dfrese.edomus.virtual
  "Defines a virtual execution context. An extension allows to get a hiccup representations of elements."
  (:require [dfrese.edomus.core :as core]
            [dfrese.edomus.ext :as ext]
            [clojure.string :as string]
            [dfrese.edomus.impl.html5 :as html5]))

#_(defn with-new-document
  [f & args]
  (execute (fn []
             (let [doc core/document
                   body (core/create-element doc "body")]
               (to-hiccup (apply f args))))))

(deftype Element [owner ns ename is properties]
  ext/IElement
  (-element-name [this] (.-ename this))
  (-element-namespace [this] (.-ns this))
  (-element-owner-document [this] (.-owner this))
  
  (-element-has-property? [this k] (contains? (.-properties this) (name k)))
  (-element-get-property [this k] (get (.-properties this) (name k) core/undefined))
  (-element-set-property! [this k value]
    (set! (.-properties this) (assoc (.-properties this)
                                     (name k) value)))
  (-element-remove-property [this k]
    (set! (.-properties this) (dissoc (.-properties this) (name k))))

  (-element-has-attribute? [this k]
    (contains? (core/get-property this "attributes") (name k)))
  (-element-get-attribute [this k]
    (get (core/get-property this "attributes") (name k) nil))
  (-element-set-attribute! [this k value]
    (core/set-property! this "attributes"
                        (assoc (core/get-property this "attributes") (name k) value)))
  (-element-remove-attribute! [this k]
    (core/set-property! this "attributes"
                        (dissoc (core/get-property this "attributes") (name k))))

  (-element-get-style [this k]
    (get (core/get-property this "style") (name k) ""))
  (-element-set-style! [this k value]
    (core/set-property! this "style"
                        (assoc (core/get-property this "style") (name k) value)))
  (-element-remove-style! [this k]
    (core/set-property! this "style"
                        (dissoc (core/get-property this "style") (name k))))

  (-element-child-nodes [this]
    (core/get-property this "childNodes"))
  (-element-child-nodes-count [this]
    (count (core/get-property this "childNodes")))
  (-element-get-child [this n]
    (get (core/get-property this "childNodes") n))
  (-element-clear-child-nodes! [this]
    (core/set-property! this "childNodes" []))
  (-element-append-child! [this node]
    (core/set-property! this "childNodes"
                        (conj (core/get-property this "childNodes") node)))
  (-element-remove-child! [this node]
    ;; TODO: throw if node is not a child
    (core/set-property! this "childNodes"
                        (persistent! (reduce (fn [r n]
                                               (if (identical? node n)
                                                 r
                                                 (conj! r n)))
                                             (transient [])
                                             (core/get-property this "childNodes")))))
  (-element-insert-before! [this node ref]
    ;; TODO: make it an exn
    #_(assert (some #(identical? ref %) (get-in elements [element "childNodes"]))
              (str "Reference node " ref " not in children: " (get-in elements [element "childNodes"])))
    (core/set-property! this "childNodes"
                        (persistent! (reduce (fn [r n]
                                               (if (identical? ref n)
                                                 (conj! (conj! r node) n)
                                                 (conj! r n)))
                                             (transient [])
                                             (core/get-property this "childNodes")))))
  (-element-replace-child! [this node old-node]
    ;; TODO: make it an exn
    #_(assert (some #(identical? old-node %) (get-in elements [element "childNodes"]))
              (str "Old node " old-node " not in children: " (get-in elements [element "childNodes"])))
    (core/set-property! this "childNodes"
                        (mapv #(if (identical? old-node %)
                                 node
                                 %)
                              (core/get-property this "childNodes"))))

  (-element-classes [this]
    (core/get-property this "classList"))
  (-element-contains-class? [this name]
    (contains? (core/get-property this "classList") name))
  (-element-add-class! [this name]
    (core/set-property! this "classList"
                        (conj (core/get-property this "classList") name)))
  (-element-remove-class! [this name]
    (core/set-property! this "classList"
                        (disj (core/get-property this "classList") name)))
  (-element-toggle-class! [this name]
    (core/set-property! this "classList"
                        (#(if (contains? % name) (disj % name) (conj % name))
                         (core/get-property this "classList"))))

  (-element-add-event-listener! [this type listener options]
    ;; (throw (ex-info "Not implemented yet." {}))
    nil ;; need to ignore to render static view on server (make it an option to remove them?!)
    )
  (-element-remove-event-listener! [this type listener options]
    ;;(throw (ex-info "Not implemented yet." {}))
    nil
    )

  (-element-focus! [this]
    (throw (ex-info "Not implemented yet." {})))
  (-element-blur! [this]
    (throw (ex-info "Not implemented yet." {}))))

(defn- create-element-ns [document ns name & [options]]
  (assert name)
  (Element. document
            ns
            (if (= ns core/html-ns) ;; TODO: also svg and math?
              (string/upper-case name)
              name)
            (:is options)
            html5/default-element-props))

(deftype TextNode [value]
  ext/ITextNode
  (-text-node-value [this] (.-value this))
  (-set-text-node-value! [this value] (set! (.-value this) value)))

(deftype Document [body]
  ext/IDocument
  (-create-element-node-ns [this ns name options]
    (if (nil? ns)
      (create-element-ns this core/html-ns name options)
      (create-element-ns this ns name options)))
  (-create-text-node [this value]
    (TextNode. value))

  (-at-next-animation-frame! [this f args]
    (throw (ex-info "Not implemented yet." {})))
  (-cancel-animation-frame! [this id]
    (throw (ex-info "Not implemented yet." {}))))

(defn new-document []
  (let [doc (Document. nil)
        body (Element. doc core/html-ns "BODY" nil html5/default-element-props)]
    ;; FIXME (set! (.-body doc) body)
    doc))

(defn to-hiccup ;; TODO -> core!?
  "Returns a hiccup representation of the given
  dom element. Note that hiccup represents HTML, which does not fully
  cover the DOM API. This assumes that the hiccup representation is
  later used to render a HTML5 document."
  [^Element element]
  (cond
    (core/element? element)
    (let [props (.-properties element)
          tag (core/element-name element)
          ns (core/element-namespace element)
          attrs (reduce-kv (fn [m k v]
                             (case k
                               "childNodes" m ;; handled below
                               (html5/add-property m tag ns k v))
                             )
                           (cond-> {}
                             (.-is element) (assoc :is (.-is element)))
                           props)]
      (apply vector
             (keyword (string/lower-case (core/element-name element))) ;; TODO: namespaces in hiccup?
             (cond->> (map to-hiccup
                           (get props "childNodes"))
               (not-empty attrs) (cons attrs))))
    (core/text-node? element)
    (core/text-node-value element)

    :else
    (throw (ex-info "Unknown type of element." {:element element}))))

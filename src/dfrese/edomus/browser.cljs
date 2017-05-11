(ns dfrese.edomus.browser
  "Defines the execution in the browser/JavaScript context."
  (:require [dfrese.edomus.impl.create :as create]
            [dfrese.edomus.impl.style :as style]
            [dfrese.edomus.impl.attributes :as attributes]
            [dfrese.edomus.ext :as ext]))

(defn ^:no-doc element-name [e]
  (.-nodeName e))

(defn ^:no-doc element-namespace [e]
  (.-namespaceURI e))

(defn ^:no-doc text-node-value [n]
  (.-nodeValue n))

(defn ^:no-doc set-text-node-value! [n value]
  (set! (.-nodeValue n) value))

(defn- has-property? [element k]
  (create/js-contains? element (name k)))

(defn- get-property [element k]
  (aget element (name k)))

(defn- set-property! [element k v]
  (aset element (name k) v))

(defn- remove-property! [element k]
  (js-delete element (name k)))

(defn- set-attribute! [element k v]
  (do (attributes/set-attribute element k v)
      nil))

(defn- remove-attribute! [element k]
  (do (attributes/remove-attribute element k)
      nil))
   
(defn- get-style [element name]
  (style/get-style (.-style element) name))

(defn- set-style! [element name value]
  (style/set-style! (.-style element) name value))

(defn- remove-style! [element name]
  (style/remove-style! (.-style element) name))

(defn- child-nodes [element]
  (let [res (transient [])
        cns (.-childNodes element)]
    (dotimes [i (.-length cns)]
      (conj! res (.item cns i)))
    (persistent! res)))

(defn- clear-child-nodes! [^js/Node element]
  (loop []
    (when-let [n (.-lastChild element)]
      (.removeChild element n)
      (recur))))

(defn- append-child! [element node]
  (.appendChild element node))
    
(defn- remove-child! [element node]
  (.removeChild element node))
    
(defn- insert-before! [element node ref]
  (.insertBefore element node ref))
    
(defn- replace-child! [element node old-node]
  (.replaceChild element node old-node))

(defn- classes [element]
  (set (array-seq (.-classList element))))
   
(defn- contains-class? [element name]
  (.contains (.-classList element) name))

(defn- add-class! [element name]
  (.add (.-classList element) name))
   
(defn- remove-class! [element name]
  (.remove (.-classList element) name))
   
(defn- toggle-class! [element name]
  (.toggle (.-classList element) name))

(defn- add-event-listener! [element type listener & [options]]
  (.addEventListener element type listener (clj->js options)))

(defn- remove-event-listener! [element type listener & [options]]
  (.removeEventListener element type listener (clj->js options)))

(defn- focus! [element]
  (.focus element))

(defn- blur! [element]
  (.blur element))

;; TODO: any polyfill/cljs lib?
(if (.-requestAnimationFrame js/window)
  (do
    (defn- at-next-animation-frame! [f & args]
      (.requestAnimationFrame js/window #(apply f args)))
    (defn- cancel-animation-frame! [id]
      (.cancelAnimationFrame js/window id)))
  (do
    (defn- at-next-animation-frame! [f & args]
      (.setTimeout js/window #(apply f args) 16)) ;; 60fps => 16ms = (/ 1000ms 60)
    (defn- cancel-animation-frame! [id]
      (.clearTimeout js/window id))))

(extend-type js/Document
  ext/IDocument
  (-create-element-node-ns [this ns name options]
    (if (nil? ns)
      (create/element this name options)
      (create/element-ns this ns name options)))
  (-create-text-node [this value]
    (create/text-node this value))

  (-create-event [this event-name init]
    (new js/Event event-name (clj->js init)))
  (-create-custom-event [this event-name init]
    (new js/CustomEvent event-name (clj->js init)))

  (-at-next-animation-frame! [document f args]
    (apply at-next-animation-frame! f args))
  (-cancel-animation-frame! [document id]
    (cancel-animation-frame! id)))

(extend-type js/Element
  ext/IElement
  (-element-name [this] (element-name this))
  (-element-namespace [this] (element-namespace this))
  (-element-owner-document [this] (.-ownerDocument this))

  (-dispatch-event! [^js/Element this ^js/Event event] (.dispatchEvent this event))
  
  (-element-has-property? [this name] (has-property? this name))
  (-element-get-property [this name] (get-property this name))
  (-element-set-property! [this name value] (set-property! this name value))
  (-element-remove-property [this name] (remove-property! this name))

  (-element-has-attribute? [this name] (attributes/has-attribute? this name))
  (-element-get-attribute [this name] (attributes/get-attribute this name))
  (-element-set-attribute! [this name value] (set-attribute! this name value))
  (-element-remove-attribute! [this name] (remove-attribute! this name))

  (-element-get-style [this name] (get-style this name))
  (-element-set-style! [this name value] (set-style! this name value))
  (-element-remove-style! [this name] (remove-style! this name))

  (-element-child-nodes [this] (child-nodes this))
  (-element-child-nodes-count [^js/Node this] (.-length (.-childNodes this)))
  (-element-get-child [^js/Node this n] (aget (.-childNodes this) n))
  (-element-clear-child-nodes! [this] (clear-child-nodes! this))

  (-element-append-child! [this node] (append-child! this node))
  (-element-remove-child! [this node] (remove-child! this node))
  (-element-insert-before! [this node ref] (insert-before! this node ref))
  (-element-replace-child! [this node old-node] (replace-child! this node old-node))

  (-element-classes [this] (classes this))
  (-element-contains-class? [this name] (contains-class? this name))
  (-element-add-class! [this name] (add-class! this name))
  (-element-remove-class! [this name] (remove-class! this name))
  (-element-toggle-class! [this name] (toggle-class! this name))

  (-element-add-event-listener! [this type listener options]
    (add-event-listener! this type listener options))
  (-element-remove-event-listener! [this type listener options]
    (remove-event-listener! this type listener options))

  (-element-focus! [this] (focus! this))
  (-element-blur! [this] (blur! this)))

(extend-type js/Text
  ext/ITextNode
  (-text-node-value [this] (text-node-value this))
  (-set-text-node-value! [this value] (set-text-node-value! this value)))

(def document js/document)

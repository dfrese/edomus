(ns edomus.browser
  "Defines the execution in the global browsing context."
  (:require [edomus.impl.create :as create]
            [edomus.impl.style :as style]
            [edomus.impl.attributes :as attributes]
            [edomus.core :as core]))

(defn ^:no-doc element? [v]
  (instance? js/Element v))

(defn ^:no-doc element-name [e]
  (.-nodeName e))

(defn ^:no-doc element-namespace [e]
  (.-namespaceURI e))

(defn ^:no-doc text-node? [v]
  (instance? js/Text v))

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
  (vec (array-seq (.-childNodes element))))
    
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

(defn execute [f & args]
  (binding [core/document js/document
            core/element-owner-document #(.-ownerDocument %)
            core/create-element create/element
            core/create-element-ns create/element-ns
            core/element? element?
            core/element-name element-name
            core/element-namespace element-namespace
            core/create-text-node create/text-node
            core/text-node? text-node?
            core/text-node-value text-node-value
            core/set-text-node-value! set-text-node-value!
            core/has-property? has-property?
            core/get-property get-property
            core/set-property! set-property!
            core/remove-property! remove-property!
            core/has-attribute? attributes/has-attribute?
            core/get-attribute attributes/get-attribute
            core/set-attribute! set-attribute!
            core/remove-attribute! remove-attribute!
            core/get-style get-style
            core/set-style! set-style!
            core/remove-style! remove-style!
            core/child-nodes child-nodes
            core/append-child! append-child!
            core/remove-child! remove-child!
            core/insert-before! insert-before!
            core/replace-child! replace-child!
            core/classes classes
            core/contains-class? contains-class?
            core/add-class! add-class!
            core/remove-class! remove-class!
            core/toggle-class! toggle-class!
            core/add-event-listener! add-event-listener!
            core/remove-event-listener! remove-event-listener!
            core/at-next-animation-frame! at-next-animation-frame!
            core/cancel-animation-frame! cancel-animation-frame!
            core/focus! focus!
            core/blur! blur!]
    (apply f args)))

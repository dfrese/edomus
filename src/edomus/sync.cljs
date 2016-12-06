(ns edomus.sync
  (:require [edomus.impl.commands :as cmd]
            [edomus.impl.create :as create]
            [edomus.impl.style :as style]
            [edomus.impl.attributes :as attributes]
            [edomus.core :as core]
            [active.clojure.monad :as monad]))

(defn execute [cmd]
  (cmd/case-cmd
   cmd
   ;; Creation
   
   (core/create-element document type & [options])
   (create/element document type options)

   (core/create-element-ns document ns name & [options])
   (create/element-ns document ns name options)

   (core/create-text-node document text)
   (create/text-node document text)

   ;; Properties

   (core/has-property? element k)
   (create/js-contains? element (name k))

   (core/get-property element k)
   (aget element (name k))

   (core/set-property! element k v)
   (aset element (name k) v)

   (core/remove-property! element k)
   (js-delete element (name k))

   ;; Attributes
   
   (core/has-attribute? element k)
   (attributes/has-attribute? element k)

   (core/get-attribute element k)
   (attributes/get-attribute element k)

   (core/set-attribute! element k v)
   (do (attributes/set-attribute element k v)
       nil)

   (core/remove-attribute! element k)
   (do (attributes/remove-attribute element k)
       nil)

   ;; Style

   (core/get-style element name)
   (style/get-style (.-style element) name)

   (core/set-style! element name value)
   (style/set-style! (.-style element) name value)

   (core/remove-style! element name)
   (style/remove-style! element name)

   ;; Children
   
   (core/child-nodes element)
   (vec (array-seq (.-childNodes element)))
    
   (core/append-child! element node)
   (.appendChild element node)
    
   (core/remove-child! element node)
   (.removeChild element node)
    
   (core/insert-before! element node ref)
   (.insertBefore element node ref)
    
   (core/replace-child! element node old-node)
   (.replaceChild element node old-node)

   ;; Classes
   (core/classes element)
   (set (array-seq (.-classList element)))
   
   (core/contains-class? element name)
   (.contains (.-classList element) name)

   (core/add-class! element name)
   (.add (.-classList element) name)
   
   (core/remove-class! element name)
   (.remove (.-classList element) name)
   
   (core/toggle-class! element name)
   (.toggle (.-classList element) name)

   monad/unknown-command))

(def sync-command-config
  (monad/make-monad-command-config
   (fn [run-any env state cmd]
     [(execute cmd) state])
   {}
   {}))

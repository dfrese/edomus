(ns edomus.async
  (:require [edomus.impl.commands :as cmd :include-macros true]
            [edomus.impl.style :as style]
            [edomus.impl.create :as create]
            [edomus.impl.chilren :as children]
            [edomus.impl.attributes :as attributes]
            [edomus.impl.batch :as batch]
            [edomus.core :as core]
            [active.clojure.monad :as monad]))

(def ^:private get-or-else
  (let [not-found ::not-found]
    (fn [mp k f]
      (let [v (get mp k not-found)]
        (if (identical? v not-found)
          (f)
          v)))))

(cmd/defcmd flush! [])

(defn- set-toggle [set v]
  ((if (contains? set v)
     disj
     conj) set v))

(defn run [batch cmd]
  (cmd/case-cmd
   cmd

   (flush!)
   [(batch/apply-batch! batch) batch/empty-batch]
   
   ;; Creation
   
   (core/create-element document type & [options])
   [(create/element document type options) batch]

   (core/create-element-ns document ns name & [options])
   [(create/element-ns document ns name options) batch]

   (core/create-text-node document text)
   [(create/text-node document text) batch]

   ;; Properties
   
   (core/has-property? element k)
   [(let [ks (name k)]
      (or (not= js/undefined (get (batch/get-properties batch element) ks js/undefined))
          (create/js-contains? element ks)))
    batch]
   
   (core/get-property element k)
   [(let [ks (name k)]
      ;; TODO: could/should check it's not one of the managed ones?
      ;; TODO: do we constitently return js/undefined (or not?)
      (get-or-else (batch/get-properties batch element) ks
                   #(aget element ks)))
    batch]
   
   (core/set-property! element k v)
   [nil (batch/update-properties batch element assoc (name k) v)]

   (core/remove-property! element k)
   [nil (batch/update-properties batch element assoc (name k) js/undefined)]

   ;; Attributes
   
   (core/has-attribute? element name)
   [(batch/has-attribute? batch element name) batch]
   
   (core/get-attribute element name)
   [(batch/get-attribute batch element name) batch]
   
   (core/set-attribute! element name value)
   [nil (batch/update-attributes batch element
                                 attributes/set-attribute name value)]

   (core/remove-attribute! element name)
   [nil (batch/update-attributes batch element
                                 attributes/remove-attribute name)]

   ;; Style
   
   (core/get-style element name)
   [(batch/get-style batch element name) batch]
    
   (core/set-style! element name value)
   [nil (batch/update-style batch element
                            style/set-style! name value)]

   (core/remove-style! element name)
   [nil (batch/update-style batch element
                            style/remove-style! name)]

   ;; Children

   (core/child-nodes element)
   [(batch/get-children batch element) batch]
    
   (core/append-child! element node)
   [nil (batch/update-children batch element
                               children/append-child node)]
    
   (core/remove-child! element node)
   [nil (batch/update-children batch element
                               children/remove-child node)]
    
   (core/insert-before! element node ref)
   [nil (batch/update-children batch element
                               children/insert-before node ref)]
    
   (core/replace-child! element node old-node)
   [nil (batch/update-children batch element
                               children/replace-child node old-node)]

   ;; Classes
   
   (core/classes element)
   [(batch/get-classes batch element) batch]
   
   (core/contains-class? element name)
   [(batch/contains-class? batch element name) batch]

   (core/add-class! element name)
   [nil (batch/update-classes batch element conj name)]
   
   (core/remove-class! element name)
   [nil (batch/update-classes batch element disj name)]
   
   (core/toggle-class! element name)
   [nil (batch/update-classes batch element set-toggle name)]

   monad/unknown-command))

(def ^:private state-key ::batch)

(def async-command-config
  (monad/make-monad-command-config
   (fn [run-any env state cmd]
     (let [[res batch] (run (state-key state) cmd)]
       [res (assoc state state-key batch)]))
   {}
   {state-key batch/empty-batch}))

#_(defn execute [cmd]
    ;; TODO: animation frame util..
  (let [[res batch] (run (get-current-batch) cmd)]
    (set-current-batch! batch)
    res))

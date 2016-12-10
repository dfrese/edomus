(ns edomus.async
  (:require [edomus.impl.commands :as cmd :include-macros true]
            [edomus.impl.style :as style]
            [edomus.impl.create :as create]
            [edomus.impl.children :as children]
            [edomus.impl.attributes :as attributes]
            [edomus.impl.batch :as batch]
            [edomus.core :as core]
            [active.clojure.monad :as monad]
            [edomus.impl.aframe :as aframe]
            [edomus.sync :as sync]))

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

(defn- do-flush! [batch]
  (aframe/cancel-frame!)
  [(batch/apply-batch! batch) batch/empty-batch])

;; TODO
#_(defn add-post-commit-hook! [f & args]
  (update-batch! update :post-commit-hooks
                 conj [f args]))

;; Properties

(defn- has-property? [batch element k]
  [(let [ks (name k)]
     (or (not= js/undefined (get (batch/get-properties batch element) ks js/undefined))
         (create/js-contains? element ks)))
   batch])

(defn- get-property [batch element k]
  [(let [ks (name k)]
     ;; TODO: could/should check it's not one of the managed ones?
     ;; TODO: do we constitently return js/undefined (or not?)
     (get-or-else (batch/get-properties batch element) ks
                  #(aget element ks)))
   batch])

(defn- set-property! [batch element k v]
  [nil (batch/update-properties batch element assoc (name k) v)])

(defn- remove-property! [batch element k]
  [nil (batch/update-properties batch element assoc (name k) js/undefined)])

;; Attributes
   
(defn- has-attribute? [batch element name]
  [(batch/has-attribute? batch element name) batch])

(defn- get-attribute [batch element name]
  [(batch/get-attribute batch element name) batch])

(defn- set-attribute! [batch element name value]
  [nil (batch/update-attributes batch element
                                attributes/set-attribute name value)])

(defn- remove-attribute! [batch element name]
  [nil (batch/update-attributes batch element
                                attributes/remove-attribute name)])

;; Style

(defn- get-style [batch element name]
  [(batch/get-style batch element name) batch])
    
(defn- set-style! [batch element name value]
  [nil (batch/update-style batch element
                           style/set-style! name value)])

(defn- remove-style! [batch element name]
  [nil (batch/update-style batch element
                           style/remove-style! name)])

;; Children

(defn- child-nodes [batch element]
  [(batch/get-children batch element) batch])

(defn- append-child! [batch element node]
  [nil (batch/update-children batch element
                              children/append-child node)])

(defn- remove-child! [batch element node]
  [nil (batch/update-children batch element
                              children/remove-child node)])

(defn- insert-before! [batch element node ref]
  [nil (batch/update-children batch element
                              children/insert-before node ref)])

(defn- replace-child! [batch element node old-node]
  [nil (batch/update-children batch element
                              children/replace-child node old-node)])

;; Classes
   
(defn- classes [batch element]
  [(batch/get-classes batch element) batch])

(defn- contains-class? [batch element name]
  [(batch/contains-class? batch element name) batch])

(defn- add-class! [batch element name]
  [nil (batch/update-classes batch element conj name)])

(defn- remove-class! [batch element name]
  [nil (batch/update-classes batch element disj name)])

(defn- toggle-class! [batch element name]
  [nil (batch/update-classes batch element set-toggle name)])



(defn- state-m [state f]
  (fn [& args]
    (let [st0 @state
          [r st1] (apply f @state args)]
      (assert (identical? @state st0) "Interleaved modification not allowed.")
      (when (not (identical? st0 st1))
        (reset! state st1))
      r)))

;; Global batch

(defonce ^:private current-batch (atom batch/empty-batch))

(defn- commit-current-batch! []
  (let [b @current-batch]
    (reset! current-batch batch/empty-batch)
    (batch/apply-batch! b)))

(defn execute [f & args]
  (let [batch current-batch]
    (binding [flush! (state-m batch do-flush!)
              ;; TODO: post-commit-hook
              ;; TODO: focus?
              core/document js/document
              core/element-owner-document #(.-ownerDocument %)
              core/create-element create/element
              core/create-element-ns create/element-ns
              core/element? sync/element?
              core/element-name sync/element-name
              core/element-namespace sync/element-namespace
              core/create-text-node create/text-node
              core/text-node? sync/text-node?
              core/text-node-value sync/text-node-value
              core/set-text-node-value! sync/set-text-node-value!
              core/has-property? (state-m batch has-property?)
              core/get-property (state-m batch get-property)
              core/set-property! (state-m batch set-property!)
              core/remove-property! (state-m batch remove-property!)
              core/has-attribute? (state-m batch has-attribute?)
              core/get-attribute (state-m batch get-attribute)
              core/set-attribute! (state-m batch set-attribute!)
              core/remove-attribute! (state-m batch remove-attribute!)
              core/get-style (state-m batch get-style)
              core/set-style! (state-m batch set-style!)
              core/remove-style! (state-m batch remove-style!)
              core/child-nodes (state-m batch child-nodes)
              core/append-child! (state-m batch append-child!)
              core/remove-child! (state-m batch remove-child!)
              core/insert-before! (state-m batch insert-before!)
              core/replace-child! (state-m batch replace-child!)
              core/classes (state-m batch classes)
              core/contains-class? (state-m batch contains-class?)
              core/add-class! (state-m batch add-class!)
              core/remove-class! (state-m batch remove-class!)
              core/toggle-class! (state-m batch toggle-class!)]
      (let [res (apply f args)]
        (aframe/request-frame! commit-current-batch!)
        res))))

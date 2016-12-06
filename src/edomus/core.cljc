(ns edomus.core
  (:require [edomus.impl.commands :as c :include-macros true]))

(c/defcmd create-element [document type & [options]])
(c/defcmd create-element-ns [document ns name & [options]])
(c/defcmd create-text-node [document text])

(c/defcmd has-property? [element name])
(c/defcmd get-property [element name])
(c/defcmd set-property! [element name value])
(c/defcmd remove-property! [element name])

(c/defcmd has-attribute? [element name])
(c/defcmd get-attribute [element name])
(c/defcmd set-attribute! [element name value])
(c/defcmd remove-attribute! [element name])

(defrecord ^:no-doc ImportantStyle [v])
(defn important [v]
  (ImportantStyle. v))

(defn important? [v]
  (instance? ImportantStyle v))

(defn important-value [v]
  (if (important? v)
    (.-v v)
    v))

;; (c/defcmd style [element])
(c/defcmd get-style [element name])
(c/defcmd set-style! [element name value])
(c/defcmd remove-style! [element name])

(c/defcmd child-nodes [element])
(c/defcmd append-child! [element node])
(c/defcmd remove-child! [element node])
(c/defcmd insert-before! [element node ref])
(c/defcmd replace-child! [element node old-node])

(c/defcmd classes [element])
(c/defcmd contains-class? [element name])
(c/defcmd add-class! [element name])
(c/defcmd remove-class! [element name])
(c/defcmd toggle-class! [element name])

;; ?? (c/defcmd focus [element])


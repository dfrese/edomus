(ns dfrese.edomus.core
  "TODO: explain commands/execution contexts."
  (:require [dfrese.edomus.impl.commands :as c :include-macros true]))

(def html-ns "http://www.w3.org/1999/xhtml")

(def ^{:dynamic true
       :doc "The global dom document, which can be passed to node creation functions."}
  document)

(c/defcmd create-element [document type & [options]] "Create an element for the given document and of the given type (e.g. tag name).")
(c/defcmd create-element-ns [document ns name & [options]] "Create an element for the given document and of the given tag and xml namespace.")
(c/defcmd element? [v] "Returns true for an element returned by [[create-element]] or [[create-element-ns]].")
(c/defcmd element-name [e] "Returns the name or type of an element. For html elements, this is the upper-cased tag name.")
(c/defcmd element-namespace [e] "Returns the xml namespace of an element.")
(c/defcmd element-owner-document [e] "Returns the document that was passed to the create function that returned the given element.")

(c/defcmd create-text-node [document value] "Create a text node in the given document and with the given content.")
(c/defcmd text-node? [v] "Returns true for the text nodes returned by [[create-text-node]].")
(c/defcmd text-node-value [v] "Returns the content of the given text node.")
(c/defcmd set-text-node-value! [node v] "Changes the content of the given text node.")

#?(:cljs (def undefined js/undefined))
#?(:clj (def undefined ::undefined))

(c/defcmd has-property? [element name] "Returns true if the given property is set on the given element.")
(c/defcmd get-property [element name] "Returns the value of the given property on the given element. Returns [[undefined]] if the element does not have that property set.")
(c/defcmd set-property! [element name value] "Assigns a new value for the given property and element.")
(c/defcmd remove-property! [element name] "Removes the given property from the element.")

(c/defcmd has-attribute? [element name] "Returns true if the given attribute is set on the given element.")
(c/defcmd get-attribute [element name] "Returns the value of the given attribute on the given element. Returns nil if the element does not have that attribute set.")
(c/defcmd set-attribute! [element name value] "Assigns a new value for the given attribute and element.")
(c/defcmd remove-attribute! [element name] "Removes the given attribute from the element.")

(defrecord ^:no-doc ImportantStyle [v])
(defn important "Marks this value as an \"!important\" css style value." [v]
  (ImportantStyle. v))

(defn important? "Returns if the given value is marked as [[important]]." [v]
  (instance? ImportantStyle v))

(defn important-value "Returns the base value of an [[important]] css style value, or `v` itself if is is not marked as important." [v]
  (if (important? v)
    (.-v v)
    v))

(c/defcmd get-style [element name] "Returns the value of the given css style on the given element. Returns \"\" if the style is not set. Note the value can be an [[important]] value.")
(c/defcmd set-style! [element name value] "Sets the given css style for the given element. Setting it to \"\" is equivalent to removing it. Note the value may be an [[important]] value.")
(c/defcmd remove-style! [element name] "Remove the given css style for the given element.")

(c/defcmd child-nodes [element] "Returns a persistent vector of the current child nodes of the given element.")
(c/defcmd append-child! [element node] "Appends the given node to the end of the child nodes of the given element.")
(c/defcmd remove-child! [element node] "Remove the given node from the child nodes of the given element. Raises an error if the node is not a child of that element.")
(c/defcmd insert-before! [element node ref] "Insert the given node before the given reference node within the children of the given element. Throws if `ref` is not a child of that element.")
(c/defcmd replace-child! [element node old-node] "Replace `old-node` with `node` within the children of the given element. Raises an error if `old-node` is not a child of that element.")

(c/defcmd classes [element] "Returns a set of the css classes currently set for the given element.")
(c/defcmd contains-class? [element name] "Returns if the given class name is set for the given element.")
(c/defcmd add-class! [element name] "Adds the given css class to the set of classes of the given element.")
(c/defcmd remove-class! [element name] "Removes the given css class from the set of classes of the given element.")
(c/defcmd toggle-class! [element name] "Adds or removes the given css class from the set of classes of the given element, depending on weather the class is contained or not.")

(c/defcmd add-event-listener! [element type listener & [options]] "Adds an event listener function for the given event type on the given element. Possible boolean options are `:capture`, `:once` and `:passive`.")
(c/defcmd remove-event-listener! [element type listener & [options]] "Removes an event listener previously registered with [[add-event-listener!]. Possible boolean options are `:capture` and `:passive`.")
;; TODO?: dispatch-event! create event, custom event?

(c/defcmd at-next-animation-frame! [f & args])
(c/defcmd cancel-animation-frame! [id])

;; The following should be done in an animation frame: (force a reflow)

(c/defcmd focus! [element] "Set the focus on the given element, if it can be focused. Requires that the element is actually attached to the document.")
(c/defcmd blur! [element] "Remove the focus from the given element, if it can be focused. Requires that the element is actually attached to the document.")

;; These properties force a reflow:
;; elem.offsetLeft, elem.offsetTop, elem.offsetWidth, elem.offsetHeight, elem.offsetParent
;; elem.clientLeft, elem.clientTop, elem.clientWidth, elem.clientHeight

;; elem.getClientRects() - non standard
;; elem.getBoundingClientRect() - TODO?

;; TODO?:
;; elem.scrollBy(), elem.scrollTo()
;; elem.scrollIntoView(), elem.scrollIntoViewIfNeeded()

;; These properties force a reflow:

;; elem.scrollWidth, elem.scrollHeight
;; elem.scrollLeft, elem.scrollTop also, setting them

;; window.scrollX, window.scrollY
;; window.innerHeight, window.innerWidth

;; TODO:?
;; inputElem.select()
;; doc.scrollingElement

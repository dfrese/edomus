(ns dfrese.edomus.core
  "Defines the functions of the DOM API. Note that you can develop
  against this API, but in order to actually execute that code, you
  have to choose an implementation by calling one of the `execute`
  functions from the other namespaces."
  (:require [dfrese.edomus.ext :as ext]))

(def ^{:doc "The html namespace - \"http://www.w3.org/1999/xhtml\"."} html-ns "http://www.w3.org/1999/xhtml")

(defn create-element
  "Create an element for the given document and of the given type (e.g. tag name)."
  [document type & [options]]
  (ext/-create-element-node-ns document nil type options))

(defn create-element-ns
  "Create an element for the given document and of the given tag and xml namespace."
  [document ns name & [options]]
  (ext/-create-element-node-ns document ns name options))

(defn element?
  "Returns true for an element returned by [[create-element]] or [[create-element-ns]]."
  [v]
  (satisfies? ext/IElement v))

(defn element-name
  "Returns the name or type of an element. For html elements, this is the upper-cased tag name."
  [e]
  (ext/-element-name e))

(defn element-namespace
  "Returns the xml namespace of an element."
  [e]
  (ext/-element-namespace e))

(defn element-owner-document
  "Returns the document that was passed to the create function that returned the given element."
  [e]
  (ext/-element-owner-document e))

(defn create-text-node
  "Create a text node in the given document and with the given content."
  [document value]
  (ext/-create-text-node document value))

(defn text-node?
  "Returns true for the text nodes returned by [[create-text-node]]."
  [v]
  (satisfies? ext/ITextNode v))

(defn text-node-value
  "Returns the content of the given text node."
  [v]
  (ext/-text-node-value v))

(defn set-text-node-value!
  "Changes the content of the given text node."
  [node v]
  (ext/-set-text-node-value! node v))

#?(:cljs (def undefined js/undefined))
#?(:clj (def undefined nil)) ;; Note: js/undefined is false on cljs; so proper replacement is not easy.

#?(:clj (defn undefined? [v] ;; already defined in cljs
          (= v undefined)))

(defn has-property?
  "Returns true if the given property is set on the given element."
  [element name]
  (ext/-element-has-property? element name))

(defn get-property
  "Returns the value of the given property on the given element. Returns [[undefined]] if the element does not have that property set."
  [element name]
  (ext/-element-get-property element name))

(defn set-property!
  "Assigns a new value for the given property and element."
  [element name value]
  (ext/-element-set-property! element name value))

(defn remove-property!
  "Removes the given property from the element."
  [element name]
  (ext/-element-remove-property element name))

(defn has-attribute?
  "Returns true if the given attribute is set on the given element."
  [element name]
  (ext/-element-has-attribute? element name))

(defn get-attribute
  "Returns the value of the given attribute on the given element. Returns nil if the element does not have that attribute set."
  [element name]
  (ext/-element-get-attribute element name))

(defn set-attribute!
  "Assigns a new value for the given attribute and element."
  [element name value]
  (ext/-element-set-attribute! element name value))

(defn remove-attribute!
  "Removes the given attribute from the element."
  [element name]
  (ext/-element-remove-attribute! element name))

(defrecord ^:no-doc ImportantStyle [v])
(defn important "Marks this value as an \"!important\" css style value." [v]
  (ImportantStyle. v))

(defn important? "Returns if the given value is marked as [[important]]." [v]
  (instance? ImportantStyle v))

(defn important-value "Returns the base value of an [[important]] css style value, or `v` itself if is is not marked as important." [v]
  (if (important? v)
    (:v v)
    v))

(defn get-style
  "Returns the value of the given css style on the given element. Returns \"\" if the style is not set. Note the value can be an [[important]] value."
  [element name]
  (ext/-element-get-style element name))

(defn set-style!
  "Sets the given css style for the given element. Setting it to \"\" is equivalent to removing it. Note the value may be an [[important]] value."
  [element name value]
  (ext/-element-set-style! element name value))

(defn remove-style!
  "Remove the given css style for the given element."
  [element name]
  (ext/-element-remove-style! element name))

(defn child-nodes
  "Returns a persistent vector of the current child nodes of the given element."
  [element]
  (ext/-element-child-nodes element))

(defn append-child!
  "Appends the given node to the end of the child nodes of the given element."
  [element node]
  #_(assert (not (contains? (set (child-nodes element)) node)) "Node must not be a child of the element.") ;; and not of any other; harder to test though.
  (ext/-element-append-child! element node))

(defn remove-child!
  "Remove the given node from the child nodes of the given element. Raises an error if the node is not a child of that element."
  [element node]
  #_(assert (contains? (set (child-nodes element)) node) "Node must be a child of the element.")
  (ext/-element-remove-child! element node))

(defn insert-before!
  "Insert the given node before the given reference node within the children of the given element. Throws if `ref` is not a child of that element."
  [element node ref]
  (assert (some? ref) "Referenced not must not be nil. Use append-child! to append a child.")
  #_(assert (contains? (set (child-nodes element)) ref) "Referenced node must be a child of the element.")
  #_(assert (not (contains? (set (child-nodes element)) node)) "Inserted node must not be a child of the element.") ;; and not of any other; harder to test though.
  (ext/-element-insert-before! element node ref))

(defn replace-child!
  "Replace `old-node` with `node` within the children of the given element. Raises an error if `old-node` is not a child of that element."
  [element node old-node]
  #_(assert (contains? (set (child-nodes element)) old-node) "Previous node must be a child of the element.")
  #_(assert (not (contains? (set (child-nodes element)) node)) "Inserted node must not be a child of the element.") ;; and not of any other; harder to test though.
  (ext/-element-replace-child! element node old-node))

(defn classes
  "Returns a set of the css classes currently set for the given element."
  [element]
  (ext/-element-classes element))

(defn contains-class?
  "Returns if the given class name is set for the given element."
  [element name]
  (ext/-element-contains-class? element name))

(defn add-class!
  "Adds the given css class to the set of classes of the given element."
  [element name]
  (ext/-element-add-class! element name))

(defn remove-class!
  "Removes the given css class from the set of classes of the given element."
  [element name]
  (ext/-element-remove-class! element name))

(defn toggle-class!
  "Adds or removes the given css class from the set of classes of the given element, depending on weather the class is contained or not."
  [element name]
  (ext/-element-toggle-class! element name))

(defn add-event-listener!
  "Adds an event listener function for the given event type on the given element. Possible boolean options are `:capture`, `:once` and `:passive`."
  [element type listener & [options]]
  (ext/-element-add-event-listener! element type listener options))

(defn remove-event-listener!
  "Removes an event listener previously registered with [[add-event-listener!]. Possible boolean options are `:capture` and `:passive`."
  [element type listener & [options]]
  (ext/-element-remove-event-listener! element type listener options))

;; TODO?: dispatch-event! create event, custom event?

(defn at-next-animation-frame! [document f & args]
  (ext/-at-next-animation-frame! document f args))

(defn cancel-animation-frame! [document id]
  (ext/-cancel-animation-frame! document id))

;; The following should be done in an animation frame: (force a reflow)

(defn focus!
  "Set the focus to the given element, if it can be focused. Requires that the element is actually attached to the document."
  [element]
  (ext/-element-focus! element))

(defn blur!
  "Remove the focus from the given element, if it can be focused. Requires that the element is actually attached to the document."
  [element]
  (ext/-element-blur! element))

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

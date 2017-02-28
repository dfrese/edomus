(ns dfrese.edomus.ext)

(defprotocol IDocument
  (-create-element-node-ns [this ns name options])
  (-create-text-node [this value])
  
  (-at-next-animation-frame! [this f args])
  (-cancel-animation-frame! [this id]))

(defprotocol IElement
  (-element-name [this])
  (-element-namespace [this])
  (-element-owner-document [this])
  
  (-element-has-property? [this name])
  (-element-get-property [this name])
  (-element-set-property! [this name value])
  (-element-remove-property [this name])

  (-element-has-attribute? [this name])
  (-element-get-attribute [this name])
  (-element-set-attribute! [this name value])
  (-element-remove-attribute! [this name])

  (-element-get-style [this name])
  (-element-set-style! [this name value])
  (-element-remove-style! [this name])

  (-element-child-nodes [this])
  (-element-append-child! [this node])
  (-element-remove-child! [this node])
  (-element-insert-before! [this node ref])
  (-element-replace-child! [this node old-node])

  (-element-classes [this])
  (-element-contains-class? [this name])
  (-element-add-class! [this name])
  (-element-remove-class! [this name])
  (-element-toggle-class! [this name])

  (-element-add-event-listener! [this type listener options])
  (-element-remove-event-listener! [this type listener options])

  (-element-focus! [this])
  (-element-blur! [this]))

(defprotocol ITextNode
  (-text-node-value [this])
  (-set-text-node-value! [this value]))

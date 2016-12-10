(ns edomus.mock
  (:require [edomus.core :as core]))

(defrecord ^:no-doc MockCall [call result])

#?(:clj
   (defmacro mock [form & [result]]
     `(MockCall. [~@form] ~result)))

(defn execute [mock-seq f & args]
  (let [cmds (set (map first mock-seq))]
    (let [rem (atom mock-seq)
          m (fn [cmd]
              (fn [& args]
                (let [n (first @rem)]
                  (let [actual (apply vector cmd args)
                        expected (:call n)]
                    (assert (= actual expected)
                            (str "Expected " expected ", but was " actual ".")))
                  (reset! rem (rest @rem))
                  (:result n))))]
      (binding [core/document nil
                core/element-owner-document (m core/element-owner-document)
                core/create-element (m core/create-element)
                core/create-element-ns (m core/create-element-ns)
                core/element? (m core/element?)
                core/element-name (m core/element-name)
                core/element-namespace (m core/element-namespace)
                core/create-text-node (m core/create-text-node)
                core/text-node? (m core/text-node?)
                core/text-node-value (m core/text-node-value)
                core/set-text-node-value! (m core/set-text-node-value!)
                core/has-property? (m core/has-property?)
                core/get-property (m core/get-property)
                core/set-property! (m core/set-property!)
                core/remove-property! (m core/remove-property!)
                core/has-attribute? (m core/has-attribute?)
                core/get-attribute (m core/get-attribute)
                core/set-attribute! (m core/set-attribute!)
                core/remove-attribute! (m core/remove-attribute!)
                core/get-style (m core/get-style)
                core/set-style! (m core/set-style!)
                core/remove-style! (m core/remove-style!)
                core/child-nodes (m core/child-nodes)
                core/append-child! (m core/append-child!)
                core/remove-child! (m core/remove-child!)
                core/insert-before! (m core/insert-before!)
                core/replace-child! (m core/replace-child!)
                core/classes (m core/classes)
                core/contains-class? (m core/contains-class?)
                core/add-class! (m core/add-class!)
                core/remove-class! (m core/remove-class!)
                core/toggle-class! (m core/toggle-class!)
                ]
        (apply f args)))))

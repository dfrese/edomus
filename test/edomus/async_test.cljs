(ns edomus.async-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [edomus.async :as async]
            [edomus.core :as core]
            [edomus.test-commands :as tc]))

(defn exec! [f & args]
  (async/execute (fn []
                   (apply f args)
                   ;; synchronously flushing batch at end - easier to test for now.
                   (async/flush!))))

(deftest async-create-test
  (tc/create-test exec!))

(deftest async-property-test
  (tc/property-test exec!))

(deftest async-attribute-test
  (tc/attribute-test exec!))

(deftest async-style-test
  (tc/style-test exec!))

(deftest async-children-test
  (tc/children-test exec!))

(deftest async-classes-test
  (tc/classes-test exec!))

(deftest async-children-test2
  (exec! (fn []
           (let [e1 (core/create-element core/document "div")
                 e2 (core/create-element core/document "span")
                 e3 (core/create-element core/document "span")
                 e4 (core/create-element core/document "span")]

             (core/append-child! e1 e2)
             (is (= [e2] (core/child-nodes e1)))
             (async/flush!)
             (is (= [e2] (core/child-nodes e1)))

             (core/remove-child! e1 e2)
             (is (= [] (core/child-nodes e1)))
             (async/flush!)
             (is (= [] (core/child-nodes e1)))

             (core/append-child! e1 e2)
             (core/insert-before! e1 e3 e2)
             (core/insert-before! e1 e4 e3)
             (is (= [e4 e3 e2] (core/child-nodes e1)))
             (async/flush!)
             (is (= [e4 e3 e2] (core/child-nodes e1)))
             
             ))))

(ns edomus.async-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [edomus.async :as async]
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

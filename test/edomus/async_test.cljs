(ns edomus.async-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [edomus.async :as async]
            [edomus.impl.batch :as batch]
            [edomus.test-commands :as tc]
            [active.clojure.monad :as monad :include-macros true]))

(def exec!
  (let [e1 (tc/exec-with! async/async-command-config)]
    (fn [cmd]
      ;; run monad, synchronously flushing batch at end - easier to test for now.
      (e1 (monad/monadic
           cmd
           (async/flush!))))))

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

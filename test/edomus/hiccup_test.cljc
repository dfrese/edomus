(ns edomus.hiccup-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [edomus.hiccup :as hiccup]
            [edomus.test-commands :as tc]
            [active.clojure.monad :as monad :include-macros true]))

(def exec!
  (let [e1 (tc/exec-with! hiccup/hiccup-command-config)]
    (fn [cmd]
      ;; run monad, synchronously flushing batch at end - easier to test for now.
      (e1 (monad/monadic
           [res cmd]
           [v (hiccup/to-hiccup res)]
           (let [_ (println (pr-str v))])
           (monad/return nil))))))

(deftest hiccup-create-test
  (tc/create-test exec!))

(deftest hiccup-property-test
  (tc/property-test exec!))

(deftest hiccup-attribute-test
  (tc/attribute-test exec!))

(deftest hiccup-style-test
  (tc/style-test exec!))

(deftest hiccup-children-test
  (tc/children-test exec!))

(deftest hiccup-classes-test
  (tc/classes-test exec!))

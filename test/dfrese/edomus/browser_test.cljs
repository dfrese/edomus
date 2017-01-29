(ns dfrese.edomus.browser-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [dfrese.edomus.test-commands :as tc]
            [dfrese.edomus.browser :as browser]))

(def exec! browser/execute)

(deftest sync-create-test
  (tc/create-test exec!))

(deftest sync-property-test
  (tc/property-test exec!))

(deftest sync-attribute-test
  (tc/attribute-test exec!))

(deftest sync-style-test
  (tc/style-test exec!))

(deftest sync-children-test
  (tc/children-test exec!))

(deftest sync-classes-test
  (tc/classes-test exec!))

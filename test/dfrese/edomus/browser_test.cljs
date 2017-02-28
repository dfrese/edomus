(ns dfrese.edomus.browser-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [dfrese.edomus.test-commands :as tc]
            [dfrese.edomus.browser :as browser]))

(deftest sync-create-test
  (tc/create-test browser/document))

(deftest sync-property-test
  (tc/property-test browser/document))

(deftest sync-attribute-test
  (tc/attribute-test browser/document))

(deftest sync-style-test
  (tc/style-test browser/document))

(deftest sync-children-test
  (tc/children-test browser/document))

(deftest sync-classes-test
  (tc/classes-test browser/document))

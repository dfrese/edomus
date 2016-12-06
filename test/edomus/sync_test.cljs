(ns edomus.sync-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [edomus.sync :as sync]
            [edomus.test-commands :as tc]))

(def exec! (tc/exec-with! sync/sync-command-config))

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
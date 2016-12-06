(ns edomus.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [edomus.core :as core]))

(deftest important-test
  (is (not= 42
            (core/important 42)))
  (is (= (core/important 42)
         (core/important 42)))
  (is (core/important? (core/important 42)))
  (is (not (core/important? 42)))
  
  (is (= 42 (core/important-value 42)))
  (is (= 42 (core/important-value (core/important 42)))))

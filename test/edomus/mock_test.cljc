(ns edomus.mock-test
  (:require #?(:cljs [cljs.test :refer-macros [deftest is testing]])
            #?(:clj [clojure.test :refer [deftest is testing]])
            [edomus.core :as core]
            [edomus.mock :as mock :include-macros true]))

(deftest mock-test
  (mock/execute [(mock/mock (core/create-element core/document "div") :e1)]
                (fn []
                  (core/create-element core/document "div"))))

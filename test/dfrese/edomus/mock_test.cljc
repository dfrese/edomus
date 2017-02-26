(ns dfrese.edomus.mock-test
  (:require #?(:cljs [cljs.test :refer-macros [deftest is testing]])
            #?(:clj [clojure.test :refer [deftest is testing]])
            [dfrese.edomus.core :as core]
            #?(:cljs [dfrese.edomus.mock :as mock :include-macros true])
            #?(:clj [dfrese.edomus.mock :as mock])))

(deftest mock-test
  (mock/execute [(mock/mock (core/create-element mock/document "div") :e1)]
                (fn []
                  (core/create-element core/document "div"))))

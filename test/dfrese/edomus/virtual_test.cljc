(ns dfrese.edomus.virtual-test
  (:require #?(:cljs [cljs.test :refer-macros [deftest is testing]])
            #?(:clj [clojure.test :refer [deftest is testing]])
            [dfrese.edomus.core :as core]
            [dfrese.edomus.virtual :as v]
            [dfrese.edomus.test-commands :as tc]))

(def document (v/new-document))

(deftest to-hiccup-test
  (is (= [:div]
         (v/to-hiccup (core/create-element document "div"))))
  (is (= [:div {:style "color: white; padding-left: 5px"}]
         (v/to-hiccup (doto (core/create-element document "div")
                        (core/set-style! "color" "white")
                        (core/set-style! :padding-left "5px")))))
  (is (= [:div {:width "14"}]
         (v/to-hiccup (doto (core/create-element document "div")
                        (core/set-attribute! "width" "14")))))
  (let [x (doto (core/create-element document "span")
            (core/append-child! (core/create-text-node document "Hello")))
        y (doto (core/create-element document "div")
            (core/append-child! x))]
    (is (= [:div [:span "Hello"]]
           (v/to-hiccup y)))))

(deftest hiccup-create-test
  (tc/create-test document))

(deftest hiccup-property-test
  (tc/property-test document))

(deftest hiccup-attribute-test
  (tc/attribute-test document))

;; cannot work the same, unless we have some general CSS library?!
#_(deftest hiccup-style-test
  (tc/style-test document))

(deftest hiccup-children-test
  (tc/children-test document))

(deftest hiccup-classes-test
  (tc/classes-test document))

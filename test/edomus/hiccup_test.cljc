(ns edomus.hiccup-test
  (:require #?(:cljs [cljs.test :refer-macros [deftest is testing]])
            #?(:clj [clojure.test :refer [deftest is testing]])
            [edomus.core :as core]
            [edomus.hiccup :as hiccup]
            [edomus.test-commands :as tc]))

(defn exec! [f]
  (hiccup/execute f))

(deftest to-hiccup-test
  (exec! (fn []
           (is (= [:div]
                  (hiccup/to-hiccup (core/create-element core/document "div"))))
           (is (= [:div {:style "color: white; padding-left: 5px"}]
                  (hiccup/to-hiccup (doto (core/create-element core/document "div")
                                      (core/set-style! "color" "white")
                                      (core/set-style! :padding-left "5px")))))
           (is (= [:div {:width "14"}]
                  (hiccup/to-hiccup (doto (core/create-element core/document "div")
                                      (core/set-attribute! "width" "14")))))
           (let [x (doto (core/create-element core/document "span")
                     (core/append-child! (core/create-text-node core/document "Hello")))
                 y (doto (core/create-element core/document "div")
                     (core/append-child! x))]
             (is (= [:div [:span "Hello"]]
                    (hiccup/to-hiccup y))))
           )))

(deftest hiccup-create-test
  (tc/create-test exec!))

(deftest hiccup-property-test
  (tc/property-test exec!))

(deftest hiccup-attribute-test
  (tc/attribute-test exec!))

;; cannot work the same, unless we have some general CSS library?!
#_(deftest hiccup-style-test
  (tc/style-test exec!))

(deftest hiccup-children-test
  (tc/children-test exec!))

(deftest hiccup-classes-test
  (tc/classes-test exec!))

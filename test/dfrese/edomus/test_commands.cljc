(ns dfrese.edomus.test-commands
  (:require #?(:cljs [cljs.test :refer-macros [deftest is testing]])
            #?(:clj [clojure.test :refer [deftest is testing]])
            [dfrese.edomus.core :as core]))

(defn create-test [document]
  (let [e1 (core/create-element document "div")]
    (is (core/element? e1))
    (is (= "DIV" (core/element-name e1))))

  (let [e1 (core/create-element document "span")]
    (is (core/element? e1))
    (is (= "SPAN" (core/element-name e1))))
          
  (let [e2 (core/create-element-ns document "myns" "div")]
    (is (core/element? e2))
    (is (= "div" (core/element-name e2)))
    (is (= "myns" (core/element-namespace e2))))

  (let [e3 (core/create-text-node document "Hello World")]
    (is (core/text-node? e3))
    (is (= "Hello World" (core/text-node-value e3)))))

(defn property-test [document]
  (let [e1 (core/create-element document "div")
        i1 (core/get-property e1 "tabIndex")]

    (is (some? i1))

    (is (core/has-property? e1 "tabIndex"))

    (is (not (core/has-property? e1 "foobar")))

    (core/set-property! e1 "tabIndex" 10)
    (is (= 10 (core/get-property e1 "tabIndex")))
          
    (core/set-property! e1 "foobar" ["test"])
    (is (core/has-property? e1 "foobar"))
    (is (= ["test"] (core/get-property e1 "foobar")))

    (core/remove-property! e1 "foobar")
    (is (not (core/has-property? e1 "foobar")))
    (is (= core/undefined (core/get-property e1 "foobar")))))

(defn attribute-test [document]
  (let [e1 (core/create-element document "div")]

    (is (not (core/has-attribute? e1 "foo-bar")))
    (is (= nil (core/get-attribute e1 "foo-bar")))

    (core/set-attribute! e1 "foo-bar" "test")
    (is (core/has-attribute? e1 "foo-bar"))
    (is (= "test" (core/get-attribute e1 "foo-bar")))

    (core/remove-attribute! e1 "foo-bar")
    (is (not (core/has-attribute? e1 "foo-bar")))
    (is (= nil (core/get-attribute e1 "foo-bar")))))

(defn style-test [document]
  (let [e1 (core/create-element document "div")]

    (let [v0 (core/get-style e1 "padding-left")]
      ;; Note: if nil or "" seems to be browser dependant
      (is (or (= nil v0)
              (= "" v0))))

    (core/set-style! e1 "padding-left" "42px")
    (is (= "42px" (core/get-style e1 "padding-left")))

    (core/set-style! e1 "padding" "1px 2px 3px 4px")
    (is (= "4px" (core/get-style e1 "padding-left")))
          
    (core/set-style! e1 "padding-top" "42px")
    (is (= "42px 2px 3px 4px" (core/get-style e1 "padding")))

    (core/set-style! e1 "padding-right" (core/important "42px"))
    (let [v4 (core/get-style e1 "padding-right")]
      (is (core/important? v4))
      (is (= "42px" (core/important-value v4))))))

(defn children-test [document]
  (let [e1 (core/create-element document "div")
        e2 (core/create-element document "span")
        e3 (core/create-element document "span")
        e4 (core/create-element document "span")]

    (is (empty? (core/child-nodes e1)))

    (core/append-child! e1 e2)
    (is (= [e2] (core/child-nodes e1)))

    (core/remove-child! e1 e2)
    (is (empty? (core/child-nodes e1)))

    (core/append-child! e1 e2)
    (core/insert-before! e1 e3 e2)
    (is (= [e3 e2] (core/child-nodes e1)))

    (core/replace-child! e1 e4 e3)
    ;; [e4 e3] in hiccup??? no clue why
    (is (= [e4 e2] (core/child-nodes e1)))))

(defn classes-test [document]
  (let [e1 (core/create-element document "div")]

    (is (empty? (core/classes e1)))

    (is (not (core/contains-class? e1 "foo")))

    (core/add-class! e1 "foo")
    (is (= #{"foo"} (core/classes e1)))
    (is (core/contains-class? e1 "foo"))
          
    (core/remove-class! e1 "foo")
    (is (empty? (core/classes e1)))
    (is (not (core/contains-class? e1 "foo")))))

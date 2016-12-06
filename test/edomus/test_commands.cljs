(ns edomus.test-commands
  (:require ;; [cljs.test :refer-macros [deftest is testing]]
            [edomus.core :as core]
            [edomus.test-utils :as tu :include-macros true]
            [active.clojure.monad :as monad :include-macros true]))

(defn exec-with! [command-config]
  (fn [cmd] ;; FIXME: does not seem to throw on unknown commands, although it should
    (try (monad/execute-free-reader-state-exception command-config cmd)
         (catch :default e
           (println e)
           (println (.-stack e))
           (throw e)))))

(defn create-test [exec!]
  (exec! (monad/monadic
          [e1 (core/create-element js/document "div")]
          (tu/m-is (instance? js/Element e1))
          (tu/m-is (= "DIV" (.-nodeName e1)))

          [e1 (core/create-element js/document "span")]
          (tu/m-is (instance? js/Element e1))
          (tu/m-is (= "SPAN" (.-nodeName e1)))
          
          [e2 (core/create-element-ns js/document "myns" "div")]
          (tu/m-is (instance? js/Element e2))
          (tu/m-is (= "div" (.-nodeName e2)))
          (tu/m-is (= "myns" (.-namespaceURI e2)))

          [e3 (core/create-text-node js/document "Hello World")]
          (tu/m-is (instance? js/Text e3))
          (tu/m-is (= "Hello World" (.-nodeValue e3))))))

(defn property-test [exec!]
  (exec! (monad/monadic
          [e1 (core/create-element js/document "div")]
          [i1 (core/get-property e1 "tabIndex")]
          (tu/m-is (some? i1))

          [h1 (core/has-property? e1 "tabIndex")]
          (tu/m-is h1)

          [h2 (core/has-property? e1 "foobar")]
          (tu/m-is (not h2))

          (core/set-property! e1 "tabIndex" 10)
          [i2 (core/get-property e1 "tabIndex")]
          (tu/m-is (= 10 i2))
          
          (core/set-property! e1 "foobar" ["test"])
          [h3 (core/has-property? e1 "foobar")]
          (tu/m-is h3)
          [v1 (core/get-property e1 "foobar")]
          (tu/m-is (= ["test"] v1))

          (core/remove-property! e1 "foobar")
          [h4 (core/has-property? e1 "foobar")]
          (tu/m-is (not h4))
          [v2 (core/get-property e1 "foobar")]
          (tu/m-is (= nil v2))
          )))

(defn attribute-test [exec!]
  (exec! (monad/monadic
          [e1 (core/create-element js/document "div")]

          [h2 (core/has-attribute? e1 "foo-bar")]
          (tu/m-is (not h2))
          [v0 (core/get-attribute e1 "foo-bar")]
          (tu/m-is (= nil v0))

          (core/set-attribute! e1 "foo-bar" "test")
          [h3 (core/has-attribute? e1 "foo-bar")]
          (tu/m-is h3)
          [v1 (core/get-attribute e1 "foo-bar")]
          (tu/m-is (= "test" v1))

          (core/remove-attribute! e1 "foo-bar")
          [h4 (core/has-attribute? e1 "foo-bar")]
          (tu/m-is (not h4))
          [v2 (core/get-attribute e1 "foo-bar")]
          (tu/m-is (= nil v2))
          )))

(defn style-test [exec!]
  (exec! (monad/monadic
          [e1 (core/create-element js/document "div")]

          [v0 (core/get-style e1 "padding-left")]
          ;; Note: if nil or "" seems to be browser dependant
          (tu/m-is (or (= nil v0)
                       (= "" v0)))

          (core/set-style! e1 "padding-left" "42px")
          [v1 (core/get-style e1 "padding-left")]
          (tu/m-is (= "42px" v1))

          (core/set-style! e1 "padding" "1px 2px 3px 4px")
          [v2 (core/get-style e1 "padding-left")]
          (tu/m-is (= "4px" v2))
          
          (core/set-style! e1 "padding-top" "42px")
          [v3 (core/get-style e1 "padding")]
          (tu/m-is (= "42px 2px 3px 4px" v3))

          (core/set-style! e1 "padding-right" (core/important "42px"))
          [v4 (core/get-style e1 "padding-right")]
          (tu/m-is (core/important? v4))
          (tu/m-is (= "42px" (core/important-value v4)))

          (monad/return e1))))

(defn children-test [exec!]
  (exec! (monad/monadic
          [e1 (core/create-element js/document "div")]

          [v0 (core/child-nodes e1)]
          (tu/m-is (empty? v0))

          [e2 (core/create-element js/document "span")]
          (core/append-child! e1 e2)
          [v1 (core/child-nodes e1)]
          (tu/m-is (= [e2] v1))

          (core/remove-child! e1 e2)
          [v2 (core/child-nodes e1)]
          (tu/m-is (empty? v2))

          [e3 (core/create-element js/document "span")]
          (core/append-child! e1 e2)
          (core/insert-before! e1 e3 e2)
          [v3 (core/child-nodes e1)]
          (tu/m-is (= [e3 e2] v3))

          [e4 (core/create-element js/document "span")]
          (core/replace-child! e1 e4 e3)
          [v4 (core/child-nodes e1)]
          (tu/m-is (= [e4 e2] v4))
          )))

(defn classes-test [exec!]
  (exec! (monad/monadic
          [e1 (core/create-element js/document "div")]

          [v0 (core/classes e1)]
          (tu/m-is (empty? v0))

          [h0 (core/contains-class? e1 "foo")]
          (tu/m-is (not h0))

          (core/add-class! e1 "foo")
          [v1 (core/classes e1)]
          (tu/m-is (= #{"foo"} v1))
          [h1 (core/contains-class? e1 "foo")]
          (tu/m-is h1)
          
          (core/remove-class! e1 "foo")
          [v2 (core/classes e1)]
          (tu/m-is (empty? v2))
          [h2 (core/contains-class? e1 "foo")]
          (tu/m-is (not h2))
          )))

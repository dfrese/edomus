(ns edomus.test-utils
  #?(:cljs (:require [active.clojure.monad :as monad :include-macros true]
                     [cljs.test :refer-macros [is]])))

#?(:clj
   (defmacro m-is [expr]
     `(monad/return (cljs.test/is ~expr))))

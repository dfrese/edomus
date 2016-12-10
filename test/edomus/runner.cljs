(ns edomus.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            edomus.core-test
            edomus.sync-test
            edomus.async-test
            edomus.hiccup-test
            edomus.mock-test
            ))

(doo-tests 'edomus.core-test
           'edomus.sync-test
           'edomus.async-test
           'edomus.hiccup-test
           'edomus.mock-test
           )

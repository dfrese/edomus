(ns dfrese.edomus.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            dfrese.edomus.core-test
            dfrese.edomus.browser-test
            dfrese.edomus.virtual-test
            ;;dfrese.edomus.mock-test
            ))

(doo-tests 'dfrese.edomus.core-test
           'dfrese.edomus.browser-test
           'dfrese.edomus.virtual-test
           ;;'dfrese.edomus.mock-test
           )

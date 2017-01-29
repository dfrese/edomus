(ns dfrese.edomus.impl.commands)

#?(:clj
   (defmacro defcmd [name params & [docstring]]
     `(defn ~(with-meta name {:doc docstring :dynamic true}) ~params
        (assert false (str "No implementation of command: " '~name ".")))))


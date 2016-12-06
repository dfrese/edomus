(ns edomus.impl.commands)

(defrecord Cmd [name args])

(def cmd-name :name)
(def cmd-args :args)

#?(:clj
   (defmacro defcmd [name params] ;; TODO: docstring; arglists
     (let [args `args#
           sym `~name] ;; FIXME: should be namespace-qualified; but isn't?
       ;; would be good to have arity check, and a name for all args, but how? :-/
       ;; ~(vec (concat params [:as args]))
       `(def ~name
          (with-meta (fn [& ~args]
                       (Cmd. '~sym ~args))
            {:command-name '~sym})))))

#?(:clj
   (defmacro case-cmd [cmd & cases]
     (let [cases' (if (even? (count cases))
                    cases
                    (drop-last cases))
           last-case (if (even? (count cases))
                       []
                       [:else (last cases)])
           name `name#
           args `args#]
       `(let [cmd# ~cmd
              ~name (cmd-name cmd#)
              ~args (cmd-args cmd#)]
          (do #_(println "check against:" ~(mapv (comp first first) (partition 2 cases')))
              (cond
                ~@(mapcat (fn translate [[test expr]]
                            (let [tcmd (first test)
                                  tparams (vec (rest test))]
                              [`(= ~name (:command-name (meta ~tcmd)))
                               `(let [~tparams ~args]
                                  ~expr)]))
                          (partition 2 cases'))
                ~@last-case))))))

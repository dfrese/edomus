(ns edomus.hiccup
  #?(:clj (:require ;;[hiccup.core :as hiccup]
                    [active.clojure.monad :as monad]
                    [edomus.impl.commands :as cmd]
                    [edomus.core :as core]))
  ;;#?(:cljs (:require-macros [hiccups.core :as hiccup]))
  #?(:cljs (:require ;;[hiccups.runtime :as hiccupsrt]
                     [active.clojure.monad :as monad :include-macros true]
                     [edomus.impl.commands :as cmd :include-macros true]
                     [edomus.core :as core])))

(defrecord Element [ns name options])
(defrecord Text [])

;; (cmd/defcmd to-html [element])
(cmd/defcmd to-hiccup [element])

(defn compile-to-hiccup [elements element]
  (let [props (get elements element)]
    (apply vector (:name element)
           (reduce-kv (fn [m k v]
                        (case k
                          "attributes" (reduce (fn [m k v]
                                                 (assoc m (keyword k) v))
                                               m v)
                          ;; "classList" TOOD
                          "childNodes" m ;; handled below
                          "style" (assoc m :style v)
                          m ;; General properties cannot be set, methinks
                          ))
                      {}
                      props)
           (map (partial compile-to-hiccup elements)
                (get props "childNodes")))))

(defn run [elements cmd]
  (cmd/case-cmd
   cmd

   #_(to-html element)
   #_(hiccup/html (compile-to-hiccup elements element))

   (to-hiccup element)
   (compile-to-hiccup elements element)

   ;; Creation
   
   (core/create-element document type & [options])
   (let [e (Element. nil type options)]
     [e (assoc elements e {})])

   (core/create-element-ns document ns name & [options])
   (let [e (Element. ns name options)]
     [e (assoc elements e {})])

   (core/create-text-node document text)
   (let [e (Text.)]
     [e (assoc elements e text)])

   ;; Properties
   
   (core/has-property? element k)
   [(not= js/undefined (get-in elements [element (name k)] js/undefined))
    elements]
   
   (core/get-property element k)
   [(get-in elements [element (name k)] js/undefined) elements]
   
   (core/set-property! element k v)
   [nil (assoc-in elements [element (name k)] v)]

   (core/remove-property! element k)
   [nil (update elements element dissoc (name k))]

   ;; Attributes
   
   (core/has-attribute? element k)
   [(not= js/undefined (get-in elements [element "attributes" (name k)] js/undefined))
    elements]
   
   (core/get-attribute element k)
   [(get-in elements [element "attributes" (name k)] js/undefined) elements]
   
   (core/set-attribute! element k v)
   [nil (assoc-in elements [element "attributes" (name k)] v)]

   (core/remove-attribute! element k)
   [nil (update-in elements [element "attributes"] dissoc (name k))]

   ;; Style
   
   (core/get-style element k)
   [(get-in elements [element "style" (name k)]) elements]
    
   (core/set-style! element k v)
   [nil (update-in elements [element "style"]
                   #(assoc (or % (array-map))
                           (name k) v))]

   (core/remove-style! element k) ;; dissoc would be wrong...
   [nil (update-in elements [element "style"] assoc (name k) "")]

   ;; Children

   ;; TODO: reuse impl/children
   (core/child-nodes element)
   [(or (get-in elements [element "childNodes"]) []) elements]
    
   (core/append-child! element node)
   [nil (update-in element [element "childNodes"]
                   #(conj (or % []) node))]
    
   (core/remove-child! element node)
   [nil (update-in element [element "childNodes"]
                   (fn [v] (vec (remove #(= node %) v))))]
    
   (core/insert-before! element node ref)
   [nil (update-in element [element "childNodes"]
                   (fn [v] (vec (mapcat #(if (= ref %)
                                           [node ref]
                                           [ref])
                                        v))))]
    
   (core/replace-child! element node old-node)
   [nil (update-in element [element "childNodes"]
                   (fn [v] (mapv #(if (= old-node %)
                                    node
                                    old-node)
                                 v)))]

   ;; Classes

   ;; TODO
   (core/classes element)
   [#{} elements]
   
   (core/contains-class? element name)
   [nil elements]

   (core/add-class! element name)
   [nil elements]
   
   (core/remove-class! element name)
   [nil elements]
   
   (core/toggle-class! element name)
   [nil elements]

   monad/unknown-command))

(def state-key ::elements)

(def hiccup-command-config
  (monad/make-monad-command-config
   (fn [run-any env state cmd]
     (let [[res elements] (run (state-key state) cmd)]
       [res (assoc state state-key elements)]))
   {}
   {state-key {}}))

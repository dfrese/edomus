(ns dfrese.edomus.impl.html5
  "Functions to translate dom properties to html attributes, defined in the HTML 5 standard."
  (:require [clojure.string :as string]
            [clojure.set :as set]))

(def default-element-props {"attributes" {}
                            "className" ""
                            "classList" #{}
                            "style" (array-map)
                            "childNodes" []
                            "accessKey" ""
                            "tabIndex" -1
                            "dir" ""
                            "id" ""
                            "lang" ""
                            "title" ""
                            "htmlFor" "" ;; for all?? (TODO?)
                            "type" "" ;; for all??
                            "checked" false
                            "value" ""
                            "placeholder" ""
                            "autofocus" false
                            })

(defn- parse-class [s]
  (if (some? s)
    (map string/trim (string/split s #"\s+"))
    nil))

(defn- add-classes [s set]
  (apply str (interpose " " (set/union (parse-class s) set))))

(defn- make-style [m]
  (apply str (interpose "; " (map (fn [[k v]]
                                    (str (name k) ": " v))
                                  (remove #(nil? (second %)) m)))))


(defn add-property [attributes tag-name tag-ns property-name value]
  #_(keyword (string/lower-case (name tag-name)))

  ;; TODO: complete, and respect full standard instead of this ad-hoc impl?
  ;; TODO: option to fail on untransaltable props!?
  (let [m attributes
        v value]
    (if (= v (get default-element-props property-name))
      ;; don't have to spoil the html code with defaults
      m
      (case property-name
        "attributes" (reduce-kv (fn [m k v]
                                  (assoc m (keyword k) v))
                                m v)
        "className" (update m :class add-classes (parse-class v))
        "classList" (update m :class add-classes v)
        "childNodes" m ;; handled below
        "style" (assoc m :style (make-style v))
        "accessKey" (assoc m :accesskey v)
        "tabIndex" (assoc m :tabindex v)
        "dir" (assoc m :dir v)
        "id" (assoc m :id v)
        "lang" (assoc m :lang v)
        "title" (assoc m :title v)
        "align" (assoc m :align v)
        "htmlFor" (assoc m :for v)
        "type" (assoc m :type v)
        "checked" (assoc m :checked (boolean v))
        "value" (assoc m :value v)
        "placeholder" (assoc m :placeholder v)
        "autofocus" (assoc m :autofocus (boolean v))
        m ;; Arbitrary properties cannot be set, methinks
        ))))


(ns edomus.event
  "Utils for more convenient event handler registration."
  (:require [clojure.string :as string]
            [edomus.core :as core]))

;; This heals (or circumvents) the fundamental flaw of DOM, that you
;; have to keep a reference of the function you added as a handler, to
;; be able to remove it again - instead of returning or accepting an id.

(defn event-type
  "Specify and event type by it's name (e.g. \"click\"), and the
  capturing phase of event handling that is to be handled."
  ([type] (if (vector? type)
            type
            (event-type type false)))
  ([type capture?]
   [(string/lower-case type) (boolean capture?)]))

;; use a random name to reduce collisions
(defonce ^:private property-name (str "edomus_event_" (rand-int 10000)))

(defn- update-data!
  ([element f]
   (core/set-property! element property-name
                       (f (core/get-property element property-name))))
  ([element f a0 a1]
   (core/set-property! element property-name
                       (f (core/get-property element property-name) a0 a1))))

(defn unset-event-handler!
  "Remove the singleton event listener for an event type and element."
  [element type]
  (update-data! element
                (fn [m]
                  ;; keep calm if it was not set already..
                  (if-let [h (get m type)]
                    (do
                      (core/remove-event-listener! element (first type) h {:capture (second type)})
                      (dissoc m type))
                    m))))

(defn set-event-handler!
  "Set the singleton event listener for an event type and element."
  ;; TODO: also support options :once and :passive? (not relevant for unset)
  [element type f & args]
  (unset-event-handler! element type)
  (let [type (event-type type) ;; lift pure strings
        h (if (empty? args)
            f
            (fn [e]
              (apply f e args)))]
    (update-data! element
                  assoc type h)
    (core/add-event-listener! element (first type) h {:capture (second type)})))

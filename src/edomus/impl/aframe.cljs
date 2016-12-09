(ns edomus.impl.aframe)

(defonce current-request (atom nil))

(if (.-requestAnimationFrame js/window)
  (do
    (defn request-frame [f]
      (.requestAnimationFrame js/window f))
    (defn cancel-frame [id]
      (.cancelAnimationFrame js/window id)))
  (do
    (defn request-frame [f]
      (.setTimeout js/window f 16)) ;; 60fps => 16ms = (/ 1000ms 60)
    (defn cancel-frame [id]
      (.clearTimeout js/window id))))

(defn cancel-frame! []
  (when-let [id @current-request]
    (cancel-frame id)
    (reset! current-request nil)))

(defn request-frame! [f & args]
  (when-not @current-request
      (reset! current-request
              (request-frame (fn []
                               (reset! current-request nil)
                               (apply f args))))))

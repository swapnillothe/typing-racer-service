(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [faker.generate :as gen]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def para (atom (gen/sentence {:lang :en})))
(def start-time (atom nil))

(defn convert-to-minutes
  [ms]
  (float (/ ms 60000)))

(defn calculate-speed
  [st words-count et]
  (Math/round (/ words-count (convert-to-minutes (- et st)))))

(defn wrap-cors
  [handler]
  (fn [request]
    (assoc-in (handler request) [:headers "Access-Control-Allow-Origin"] "*")))

(defn start-race []
  (reset! start-time (.getTime (java.util.Date.))))

(defn end-race [words-count]
  (calculate-speed @start-time words-count (.getTime (java.util.Date.))))

(defroutes app-routes
           (GET "/" [] "Hello World")
           (GET "/paragraph" [] @para)
           (POST "/start-race" [] (str (start-race)))
           (POST "/end-race" {:keys [params]}
             (str (end-race (read-string (:words-count params))) " WPM"))
           (route/not-found "Not Found"))


(def app (wrap-cors (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false))))

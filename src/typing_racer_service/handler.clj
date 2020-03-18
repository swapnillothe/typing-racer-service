(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [faker.generate :as gen]
            [clojure.string :as str]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def para (atom (gen/sentence {:lang :en})))
(def start-time (atom nil))

(defn convert-to-minutes
  [ms]
  (float (/ ms 60000)))

(defn calculate-speed
  [st words et]
  (Math/round (/ (count (str/split words #" ")) (convert-to-minutes (- et st)))))

(defn wrap-cors
  [handler]
  (fn [request]
    (assoc-in (handler request) [:headers "Access-Control-Allow-Origin"] "*")))

(defn start-race []
  (reset! start-time (.getTime (java.util.Date.))))

(defn end-race []
  (calculate-speed @start-time @para (.getTime (java.util.Date.))))

(defroutes app-routes
           (GET "/" [] "Hello World")
           (GET "/paragraph" [] @para)
           (POST "/start-race" [] (str (start-race)))
           (POST "/end-race" [] (str (end-race) " WPM"))
           (route/not-found "Not Found"))


(def app (wrap-cors (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false))))

(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [faker.generate :as gen]
            [clojure.string :as str]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:import (java.util UUID)))

(defn random-para []
  (gen/sentence {:lang :en}))

(def para (atom (random-para)))
(def start-time (atom nil))
(def races (atom {}))

(defn convert-to-minutes [ms]
  (float (/ ms 60000)))

(defn calculate-speed [st words et]
  (Math/round (/ (count (str/split words #" "))
                 (convert-to-minutes (- et st)))))

(defn wrap-cors [handler]
  (fn [request]
    (assoc-in (handler request)
              [:headers "Access-Control-Allow-Origin"] "*")))

(defn start-race []
  (reset! start-time (.getTime (java.util.Date.))))

(defn end-race []
  (calculate-speed @start-time @para
                   (.getTime (java.util.Date.))))

(defn random-uuid []
  (str (UUID/randomUUID)))

(defn create-race []
  (let [race-id (random-uuid) player-id (random-uuid) para (random-para)]
    (swap! races #(assoc % race-id {:player-id player-id :para para}))
    {"race-id" race-id "player-id" player-id "paragraph" para}))

(defn host-race []
  (create-race))

(defroutes app-routes
           (GET "/" [] "Hello World")
           (POST "/host" [] (json/json-str (host-race)))
           (GET "/paragraph" [] @para)
           (POST "/start-race" [] (str (start-race)))
           (POST "/end-race" [] (str (end-race) " WPM"))
           (route/not-found "Not Found"))


(def app (wrap-cors
           (wrap-defaults
             app-routes
             (assoc-in site-defaults [:security :anti-forgery] false))))

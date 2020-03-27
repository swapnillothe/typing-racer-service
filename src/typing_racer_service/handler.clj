(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
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
  (subs (str (UUID/randomUUID)) 32))

(defn create-race [host]
  (let [race-id (random-uuid) player-id (random-uuid) para (random-para)]
    (swap! races #(assoc % race-id {:paragraph para :players [{:name host :player-id player-id}]}))
    {"race-id" race-id "name" host "player-id" player-id "paragraph" para}))

(defn host-race [req]
  (json/json-str (create-race (:host (:params req)))))

(defn join-race [req]
  (let [race-id (:race-id (:params req))
        name (:name (:params req))
        player-id (random-uuid)]
    (if (contains? @races race-id)
      (do (swap! races #(assoc-in % [race-id :players] (vec (concat (:players (@races race-id)) [{:name name :player-id player-id}]))))
          (json/json-str {"race-id" race-id "name" name "player-d" player-id "paragraph" (:paragraph (@races race-id))}))
      (json/json-str {:status 400
                      :body   (str "No such race with race id " race-id)}))))

(defn get-race [req]
  (json/json-str (@races (:race-id (:params req)))))

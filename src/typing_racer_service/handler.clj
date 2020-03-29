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

(defn race-details [race-id para name player-id]
  (json/json-str
    {"race-id"   race-id
     "name"      name
     "player-id" player-id
     "paragraph" para}))

(defn add-to-races [keys value]
  (swap! races #(assoc-in % keys value)))

(defn new-player [name id]
  {:name name :player-id id})

(defn new-race [para host host-id]
  {:paragraph para :players [(new-player host host-id)]})

(defn from-race [key race-id]
  (key (@races race-id)))

(defn paragraph [race-id]
  (from-race :paragraph race-id))

(defn players [race-id]
  (from-race :players race-id))

(defn add-player [race-id name id]
  (merge (players race-id) (new-player name id)))

(defn create-race [race-id host host-id para]
  (add-to-races [race-id] (new-race para host host-id))
  (race-details race-id para host host-id))

(defn join-player [race-id name player-id para]
  (add-to-races [race-id :players] (add-player race-id name player-id))
  (race-details race-id para name player-id))

(defn no-such-race [race-id]
  {:status 400
   :body   (json/json-str {:error (str "No such race with race id " race-id "!")})})

(defn host-race [req]
  (create-race
    (random-uuid)
    (:host (:params req))
    (random-uuid)
    (random-para)))

(defn race-exist? [race-id]
  (contains? @races race-id))

(defn join-race [req]
  (let [race-id (:race-id (:params req))
        name (:name (:params req))
        player-id (random-uuid)]
    (if (race-exist? race-id)
      (join-player race-id name player-id (paragraph race-id))
      (no-such-race race-id))))

(defn get-race [req]
  (json/json-str (@races (:race-id (:params req)))))

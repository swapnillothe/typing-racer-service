(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
		  [clojure.data.json :as json]
		  [faker.generate :as gen]
		  [clojure.string :as str]
		  [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
		  [faker.generate :as gen]
		  [clojure.string :as str]
		  [typing-racer-service.utils :refer :all])
  (:import (java.util UUID)))

(defn random-para []
  (gen/sentence {:lang :en}))

(def races (atom {}))

(defn number-of-joined-players
  [race-id]
  (count ((@races race-id) :players)))

(defn has-all-joined
  [race-id]
  (= ((@races race-id) :number-of-players) (number-of-joined-players race-id)))

(defn race-exist? [race-id]
  (contains? @races race-id))

(defn convert-to-minutes [ms]
  (float (/ ms 60000)))

(defn calculate-speed [st words et]
  (Math/round (/ (count (str/split words #" "))
			  (convert-to-minutes (- et st)))))

(defn wrap-cors [handler]
  (fn [request]
	 (assoc-in (handler request)
			 [:headers "Access-Control-Allow-Origin"] "*")))

(defn start-race [race-id]
  (swap! races #(assoc-in % [race-id :start-time] (.getTime (java.util.Date.)))))

(defn get-player-index [race-id player-id]
  (let [players (get-in @races [race-id :players])]
    (.indexOf players (first (filter #(= (% :player-id) player-id) players)))))

(defn update-speed [race-id player-id typed]
  (swap! races #(assoc-in %
					 [race-id :players (get-player-index race-id player-id) :speed]
					 (calculate-speed (get-in @races [race-id :start-time])
								   typed
								   (.getTime (java.util.Date.))))))

(defn end-race [race-id player-id]
  (update-speed race-id player-id (get-in @races [race-id :paragraph]))
  (calculate-speed (get-in @races [race-id :start-time])
			    (get-in @races [race-id :paragraph])
			    (.getTime (java.util.Date.))))

(defn random-uuid []
  (subs (str (UUID/randomUUID)) 32))

(defn race-details [race-id para name player-id]
  {:body {:race-id   race-id
		:name      name
		:player-id player-id
		:paragraph para}})

(defn add-to-races [keys value]
  (swap! races #(assoc-in % keys value)))

(defn new-player [name id]
  {:name name :player-id id :speed 0})

(defn new-race [para no-of-players host host-id]
  {:paragraph         para
   :number-of-players no-of-players
   :players           [(new-player host host-id)]})

(defn from-race [key race-id]
  (key (@races race-id)))

(defn paragraph [race-id]
  (from-race :paragraph race-id))

(defn players [race-id]
  (from-race :players race-id))

(defn add-player [race-id name id]
  (merge (players race-id) (new-player name id)))

(defn create-race [race-id host host-id no-of-players para]
  (add-to-races [race-id] (new-race para no-of-players host host-id))
  (race-details race-id para host host-id))

(defn join-player [race-id name player-id para]
  (when
    (race-exist? race-id)
    (do (add-to-races [race-id :players] (add-player race-id name player-id))
	   (race-details race-id para name player-id))))

(defn no-such-race [race-id]
  {:status 400
   :body   {:error (str "No such race with race id " race-id "!")}})

(defn host-race [req]
  (create-race
    (random-uuid)
    (:host (:params req))
    (random-uuid)
    (read-string (:number-of-players (:params req)))
    (random-para)))

(defn join-race [player]
  (let [race-id (:race-id player)
	   name (:name player)
	   player-id (random-uuid)]
    (if-let [player (join-player race-id name player-id (paragraph race-id))]
	 (when (has-all-joined race-id)
	   (do (start-race race-id) player))
	 (no-such-race race-id))))

(defn get-race [req]
  (json/json-str (@races (:race-id (:params req)))))

(defn get-result [race-id]
  (map #(update % :speed (fn [speed] (str speed " WPM")))
	  (map (partial (flip select-keys) [:name :speed]) (get-in @races [race-id :players]))))

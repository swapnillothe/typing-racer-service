(ns typing-racer-service.core
  (:require [compojure.core :refer :all]
		  [typing-racer-service.handler :refer :all]
		  [compojure.route :as route]
		  [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
		  [ring.middleware.json :refer :all]
		  [ring.util.response :as r]))

(defroutes app-routes
		 (GET "/" [] "Hello World")
		 (GET "/race" req (get-race req))
		 (POST "/host" req (host-race req))
		 (POST "/join-race" {:keys [params]} (join-race params))
		 (GET "/wait-status" [race-id] {:body {:hasAllJoined          (has-all-joined race-id)
									    :numberOfJoinedPlayers (number-of-joined-players race-id)}})
		 (GET "/paragraph" [] @para)
		 (POST "/start-race" [] (str (start-race)))
		 (POST "/end-race" [] (str (end-race) " WPM"))
		 (route/not-found "Not Found"))

(def app (wrap-json-response
		 (wrap-cors
		   (wrap-defaults
			app-routes
			(assoc-in site-defaults [:security :anti-forgery] false)))))

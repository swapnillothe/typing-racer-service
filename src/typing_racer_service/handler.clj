(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [faker.generate :as gen]
            [ring.adapter.jetty :refer :all]))

(def para (atom (gen/paragraph {:lang           :en
                                :sentence-range [1 5]})))

(def default-headers {
                      "Content-Type"                 "text/plain"
                      "Access-Control-Allow-Origin"  "*"
                      "Access-Control-Allow-Methods" "GET"
                      "Access-Control-Allow-Headers" "X-Requested-With,Content-Type,Cache-Control,Origin,Accept"
                      })

(defn wrap-cors
  ([body] (wrap-cors body nil nil))
  ([body headers] (wrap-cors body headers nil))
  ([body headers response]
   (merge {:status  200
           :headers (merge default-headers headers)
           :body    body} response)))

(defroutes app-routes
           (GET "/" [] (wrap-cors "Hello World"))
           (GET "/paragraph" [] (wrap-cors @para))
           (route/not-found (wrap-cors "Not Found")))


(defn main []
  (run-jetty app-routes {:port 9002}))

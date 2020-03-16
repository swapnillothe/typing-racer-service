(ns typing-racer-service.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [faker.generate :as gen]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def para (atom (gen/paragraph {:lang           :en
                                :sentence-range [1 5]})))

(defroutes app-routes
           (GET "/" [] "Hello World")
           (GET "/paragraph" [] para)
           (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

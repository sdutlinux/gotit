(ns gotit.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults]]

            [gotit.config :as config]
            [gotit.common :as gc]
            [gotit.gotit :as gg]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/test" [] gg/hh)
  (POST "/login" [] gg/login)
  (route/not-found "Not Found"))

(defn wrap
  [handler]
  (fn [req]
    (println (:session req))
    (handler req)))

(def app
  (-> app-routes
      wrap
      gc/wrap-yxt-sc
      (wrap-defaults config/site-defaults)))

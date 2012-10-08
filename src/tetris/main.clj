(ns tetris.main
  (:use [compojure.core]
        [ring.adapter.jetty])
  (:require [compojure.route :as route]))

(defroutes rts
  (route/files "/")
  (route/not-found "Page not found"))

(def application-routes
     rts)

(defn -main [port]
  (run-jetty application-routes {:port (read-string port)
                                 :join? false}))



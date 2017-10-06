(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn save-message! [{:keys [params]}]
  (db/save-message!
   (assoc params :timestamp (java.util.Date.)))
  (response/found "/"))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (POST "/message" request (save-message! request))
  (GET "/about" [] (about-page)))


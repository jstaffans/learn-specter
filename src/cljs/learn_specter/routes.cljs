(ns learn-specter.routes
  (:require [re-frame.core :refer [dispatch]]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]))

(def routes
  ["/" {""                   :index
        ["page/" [long :id]] :page}])

(defn- dispatch-route [match]
  (case (:handler match)
    :index (dispatch [:show-page 0])
    :page (dispatch [:show-page (-> match :route-params :id)])))

(def history (pushy/pushy dispatch-route
                          (partial bidi/match-route routes)))

(defn init []
  (pushy/start! history))

(defn set-token! [token]
  (pushy/set-token! history token))

(defn path-for [tag & args]
  (apply bidi/path-for routes tag args))

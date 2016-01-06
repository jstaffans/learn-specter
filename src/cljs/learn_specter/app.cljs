(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defedn]])
  (:require [reagent.core :as reagent :refer [atom]]
            [com.rpl.specter :as s]))

(defedn movies "movies.edn")

(defn some-component []
  [:div
   [:h3 "I am a component"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red"]
    " text."]])

(defn calling-component []
  [:div "Parent component"
   [some-component]])

(defn init []
  (reagent/render-component [calling-component]
                            (.getElementById js/document "container")))

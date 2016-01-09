(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defedn]]
                   [reagent.ratom :refer [reaction]])
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [com.rpl.specter :as s]))

(defedn movies "movies.edn")

(register-handler
  :initialize
  (fn [db _]
    (merge db {:movies movies})))

(register-sub
  :current-dataset
  (fn [db _]
    (reaction (:movies @db))))

(defn dataset
  []
  (let [ds (subscribe [:current-dataset])
        first-movie (reaction (first @ds))]
    (fn dataset-renderer
      []
      [:div
       "First movie: " (with-out-str (pprint @first-movie))])))

(defn init []
  (dispatch-sync [:initialize])
  (reagent/render-component
    [dataset]
    (.getElementById js/document "container")))

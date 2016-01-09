(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defedn defmd]]
                   [reagent.ratom :refer [reaction]])
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [com.rpl.specter :as s]
            [learn-specter.editor :refer [editor]]))

(enable-console-print!)

(defedn movies "movies.edn")

(defmd content-html "content.md")

(register-handler
  :initialize
  (fn [db _]
    (merge db {:movies movies})))

(register-sub
  :current-dataset
  (fn [db _]
    (reaction (:movies @db))))

(defn content
  []
  [:div {:dangerouslySetInnerHTML {:__html content-html}}])

(defn add-ellipse
  "Adds an ellipse at the end of a list, to indicate that the list is shown incomplete."
  [s]
  (clojure.string/replace s #"\)(\n)*$" "\n ...)"))

(defn dataset
  []
  (let [ds (subscribe [:current-dataset])
        first-movies (reaction (take 2 @ds))]
    (fn dataset-renderer
      []
      [:div
       "The dataset used for the excercises has the following form:"
       [:pre (-> @first-movies pprint with-out-str add-ellipse)]])))

(defn lesson
  []
  [:div.row
   [:div.col-md-8
    [content]
    [:h2 "Excercises"]
    [dataset]
    [editor]]
   [:div.col-md-4
    "Result"]])

(defn init []
  (dispatch-sync [:initialize])
  (reagent/render-component
    [lesson]
    (.getElementById js/document "container")))

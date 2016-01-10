(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defedn defcontent]]
                   [reagent.ratom :refer [reaction]])
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [com.rpl.specter :as s]
            [learn-specter.editor :refer [editor]]
            [learn-specter.routes :as routes]))

(enable-console-print!)

(defedn movies "movies.edn")

(defcontent pages "./src/md")

;; Handlers

(register-handler
  :initialize
  (fn [db _]
    (merge db {:movies       movies
               :content      pages
               :current-page 0})))

(register-handler
  :show-page
  (fn [db [_ page-id]]
    (assoc-in db [:current-page] page-id)))

;; Subscriptions

(register-sub
  :current-dataset
  (fn [db _]
    (reaction (:movies @db))))

(register-sub
  :current-page
  (fn [db _]
    (let [content (reaction (:content @db))
          current-page (reaction (:current-page @db))]
      (reaction
        {:html  (get @content @current-page)
         :links (merge {}
                  (when (< @current-page (-> @content count dec)) {:next (routes/path-for :page :id (inc @current-page))})
                  (when (> @current-page 0) {:previous (routes/path-for :page :id (dec @current-page))}))}))))

(defn content
  []
  (let [content (subscribe [:current-page])]
    (fn content-renderer
      []
      (let [html (get-in @content [:html])
            next (get-in @content [:links :next])
            prev (get-in @content [:links :prev])]
        [:div
         [:div {:dangerouslySetInnerHTML {:__html html}}]
         [:section.nav
          (when prev [:a {:href prev} "Prev"])
          (when next [:a {:href next} "Next"])]]))))

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
   [:div.col-md-4.result
    "Result"]])

(defn init []
  (dispatch-sync [:initialize])
  (routes/init)
  (reagent/render-component
    [lesson]
    (.getElementById js/document "container")))

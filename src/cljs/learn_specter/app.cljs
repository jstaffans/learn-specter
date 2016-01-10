(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defcontent]]
                   [reagent.ratom :refer [reaction]])
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [com.rpl.specter :as s]
            [learn-specter.editor :refer [editor]]
            [learn-specter.result :refer [result]]
            [learn-specter.routes :as routes]
            [learn-specter.excercises :refer [page-excercises]]))

(enable-console-print!)

(defcontent pages "./src/md")

;; Handlers

(register-handler
  :initialize
  (fn [db _]
    (merge db {:content      pages
               :excercises   page-excercises
               :current-page 0})))

(register-handler
  :show-page
  (fn [db [_ page-id]]
    (assoc db :current-page page-id)))

(register-handler
  :input-changed
  (fn [db [_ input]]
    (assoc db :current-input input)))

;; Subscriptions

(register-sub
  :current-page
  (fn [db _]
    (let [content (reaction (:content @db))
          excercises (reaction (:excercises @db))
          current-page (reaction (:current-page @db))]
      (reaction
        {:html       (get @content @current-page)
         :links      (merge {}
                       (when (< @current-page (-> @content count dec)) {:next (routes/path-for :page :id (inc @current-page))})
                       (when (> @current-page 0) {:prev (routes/path-for :page :id (dec @current-page))}))
         :dataset    (get-in @excercises [@current-page :dataset])
         :excercises (get-in @excercises [@current-page :excercises])}))))

;; Components

(defn content
  []
  (let [curr (subscribe [:current-page])]
    (fn content-renderer
      []
      (let [html (get-in @curr [:html])
            next (get-in @curr [:links :next])
            prev (get-in @curr [:links :prev])]
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
  (let [curr (subscribe [:current-page])
        first-movies (reaction (take 2 (:dataset @curr)))]
    (fn dataset-renderer
      []
      [:div
       "The dataset used for the excercises has the following form:"
       [:pre (-> @first-movies pprint with-out-str add-ellipse)]])))

(defn eval-button
  []
  [:button.btn.btn-primary.eval "Evaluate"])

(defn excercises
  []
  [:section
   [:h2 "Excercises"]
   [dataset]
   "Some excercises"
   [editor]
   [eval-button]])

(defn lesson
  []
  [:div
   [:div.row
    [:div.col-md-7
     [content]
     [excercises]]
    [:div.col-md-5.result
     [result]]]
   [:hr]
   [:div.row]])

(defn init []
  (dispatch-sync [:initialize])
  (routes/init)
  (reagent/render-component
    [lesson]
    (.getElementById js/document "container")))


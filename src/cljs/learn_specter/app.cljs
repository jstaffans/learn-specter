(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defpages]]
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

(defpages pages "./src/md")

;; Handlers

(register-handler
  :initialize
  (fn [_ _]
    {:content      pages
     :excercises   page-excercises
     :current-page 0}))

(register-handler
  :show-page
  (fn [db [_ page-id]]
    (assoc db :current-page page-id :current-input nil)))

(register-handler
  :input-changed
  (fn [db [_ input]]
    (assoc db :current-input input)))

;; Subscriptions

(register-sub
  :current-content
  (fn [db [_]]
    (let [content (reaction (:content @db))
          current-page (reaction (:current-page @db))]
      (reaction
        {:html  (get @content @current-page)
         :links (merge {}
                  (when (< @current-page (-> @content count dec)) {:next (routes/path-for :page :id (inc @current-page))})
                  (when (> @current-page 0) {:prev (routes/path-for :page :id (dec @current-page))}))}))))

(register-sub
  :current-excercise
  (fn [db [_]]
    (let [current-page (reaction (-> db deref :current-page))]
      (reaction (get (-> db deref :excercises) @current-page)))))

(register-sub
  :current-input
  (fn [db [_]]
    (reaction (:current-input @db))))

;; Components

(defn content
  []
  (let [content (subscribe [:current-content])]
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
  (clojure.string/replace s #"\](\n)*$" " ...]"))

(defn dataset-preview
  [preview]
  [:div
   "The dataset used for the excercises has the following form:"
   [:pre (-> preview pprint with-out-str add-ellipse)]])

(defn eval-button
  []
  [:button.btn.btn-primary.eval {:on-click #(dispatch [:eval-clicked])} "Evaluate"])

(defn excercises
  []
  (let [excercise (subscribe [:current-excercise])
        editor-content (subscribe [:current-input])]
    (fn []
      (let [{:keys [dataset preview-fn]} @excercise]
        (.log js/console (clj->js dataset))
        (.log js/console preview-fn)
        [:section
         [:h2 "Excercises"]
         [dataset-preview (preview-fn dataset)]
         "Some excercises"
         [editor editor-content]
         [eval-button]]))))

(defn lesson
  []
  (fn []
    [:div
     [:div.row
      [:div.col-md-7
       [content]
       [excercises]]
      [:div.col-md-5.result
       [result]]]
     [:hr]
     [:div.row]]))

(defn init []
  (dispatch-sync [:initialize])
  (routes/init)
  (reagent/render-component
    [lesson]
    (.getElementById js/document "container")))


(ns learn-specter.app
  (:require-macros [learn-specter.macros :refer [defpages]]
                   [reagent.ratom :refer [reaction]])
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [com.rpl.specter :as s]
            [learn-specter.editor :refer [editor set-editor-value!]]
            [learn-specter.result :refer [result]]
            [learn-specter.routes :as routes]
            [learn-specter.excercises :refer [page-excercises]]))

(defpages pages "./src/md")

(def num-pages (count pages))

(defn content-for
  [page]
  (get pages page))

(defn excercises-for
  [page]
  (get page-excercises page))

;; Handlers

(register-handler
  :initialize
  (fn [_ _]
    {:current-page 0}))

(register-handler
  :show-page
  (fn [db [_ page-id]]
    (set-editor-value! "")
    (-> db
        (assoc :current-page page-id)
        (dissoc :current-input :eval-input))))


(register-handler
  :input-changed
  (fn [db [_ input]]
    (assoc db :current-input input)))

;; Subscriptions

(register-sub
  :current-lesson
  (fn [db [_]]
    (let [current-page (reaction (:current-page @db))]
      (reaction
        {:content    {:html  (content-for @current-page)
                      :links (merge {}
                               (when (< @current-page (dec num-pages)) {:next (routes/page-path (inc @current-page))})
                               (when (> @current-page 0) {:prev (routes/page-path (dec @current-page))}))}
         :excercises (excercises-for @current-page)}))))

(register-sub
  :current-input
  (fn [db [_]]
    (reaction (:current-input @db))))

;; Components

(defn render-content
  []
  (fn [content]
    (let [html (get-in content [:html])
          next (get-in content [:links :next])
          prev (get-in content [:links :prev])]
      [:div
       [:div {:dangerouslySetInnerHTML {:__html html}}]
       [:section.nav
        (when prev [:a {:href prev} "Prev"])
        (when next [:a {:href next} "Next"])]])))

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

(defn render-excercises
  [excercises editor-input]
  (let [{:keys [dataset preview-fn]} excercises]
    [:section
     [:h2 "Excercises"]
     [dataset-preview (preview-fn dataset)]
     [:div
      "Specter is available under the "
      [:span.fixed-width "s"]
      " namespace alias. The dataset is called "
      [:span.fixed-width "ds"]
      "."]
     [editor editor-input]
     [eval-button]]))


(defn lesson
  []
  (fn []
    (let [current-lesson (subscribe [:current-lesson])
          editor-input (subscribe [:current-input])]
      [:div
       [:div.row
        [:div.col-md-7
         [render-content (:content @current-lesson)]
         [render-excercises (:excercises @current-lesson) @editor-input]]
        [:div.col-md-5.result
         [result]]]
       [:hr]
       [:div.row]])))

(defn init []
  (dispatch-sync [:initialize])
  (routes/init)
  (reagent/render-component
    [lesson]
    (.getElementById js/document "container")))


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
            [learn-specter.excercises :refer [page-excercises]]
            [devtools.core :as devtools]))

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
    {:current-page 0
     :excercises   (mapv
                     #(assoc {}
                       :active-task 0
                       :editor-contents (mapv :hint (:tasks %)))
                     page-excercises)}))

(register-handler
  :show-page
  (fn [db [_ page-id]]
    (set-editor-value! "")
    (-> db
        (assoc :current-page page-id)
        (dissoc :current-input :eval-input))))

(register-handler
  :task-switched
  (fn [db [_ task]]
    (update-in db [:excercises (:current-page db)] #(assoc % :active-task task))))

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
  :current-task
  (fn [db [_]]
    (let [current-page (reaction (:current-page @db))
          excercise-state (reaction (nth (:excercises @db) @current-page))
          active-task (reaction (:active-task @excercise-state))]
      (reaction {:active-task    @active-task
                 :editor-content (nth (:editor-contents @excercise-state) @active-task)}))))

(register-sub
  :current-input
  (fn [db [_]]
    (reaction (:current-input @db))))

;; Components

(defn content
  [c]
  (let [html (get-in c [:html])
        next (get-in c [:links :next])
        prev (get-in c [:links :prev])]
    [:div
     [:div {:dangerouslySetInnerHTML {:__html html}}]
     [:section.nav
      (when prev [:a {:href prev} "Prev"])
      (when next [:a {:href next} "Next"])]]))

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
  [:div.space-after
   [:button.btn.btn-primary.eval {:on-click #(dispatch [:eval-clicked])} "Evaluate"]])

(defn excercise-interaction
  []
  (let [current-task (subscribe [:current-task])]
    ;; TODO: subscribe to excercise state
    (fn [tasks]
      [:div
       [:div.space-before.space-after
        [:ul.nav.nav-tabs
         (doall
           (map-indexed
             (fn [i _]
               [(if (= i (:active-task @current-task)) :li.active :li) {:key i}
                [:a {:href "#" :on-click #(dispatch [:task-switched i])} (inc i)]])
             tasks))]]
       [editor]
       [eval-button]])))

(defn excercises
  [excercises]
  (let [{:keys [dataset preview-fn tasks]} excercises]
    [:section
     [:h2 "Excercises"]
     [dataset-preview (preview-fn dataset)]
     [:div
      "Specter is available under the "
      [:span.fixed-width "s"]
      " namespace alias. The dataset is called "
      [:span.fixed-width "ds"]
      "."]
     [excercise-interaction tasks]]))

(defn lesson
  []
  (let [current-lesson (subscribe [:current-lesson])]
    (fn []
      [:div
       [:div.row
        [:div.col-md-7
         [content (:content @current-lesson)]
         [excercises (:excercises @current-lesson)]]
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


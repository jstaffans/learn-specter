(ns learn-specter.app
  (:require [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [com.rpl.specter :as s]
            [learn-specter.editor :refer [editor]]
            [learn-specter.result :refer [result]]
            [learn-specter.routes :as routes]
            [learn-specter.subs :as subs]
            [learn-specter.handlers :as handlers]
            [devtools.core :as devtools]))

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


;; entry point

(defn init []
  (dispatch-sync [:initialize])
  (routes/init)
  (reagent/render-component
    [lesson]
    (.getElementById js/document "container")))

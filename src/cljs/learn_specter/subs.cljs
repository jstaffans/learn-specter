(ns learn-specter.subs
  (:require-macros [learn-specter.macros :refer [defpages]]
                   [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [learn-specter.routes :as routes]
            [learn-specter.excercises :refer [page-excercises]]))


;; Subscriptions

;; TODO:
;; * move pages, content stuff to separata namespace
;; * build up a signal graph with reg-sub, pure functions, add tests

(defpages pages "./src/md")

(def num-pages (count pages))

(defn content-for
  [page]
  (get pages page))

(defn excercises-for
  [page]
  (get page-excercises page))

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

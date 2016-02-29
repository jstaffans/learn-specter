(ns learn-specter.handlers
  (:require [learn-specter.excercises :refer [page-excercises]]
            [re-frame.core :refer [register-handler subscribe]]
            [learn-specter.editor :refer [set-editor-value!]]))

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
   ;; pretty ugly to clear the editor content here.
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
   ;; TODO: input is task-dependent -- there is no such thing as a global "current input"
   (assoc db :current-input input)))

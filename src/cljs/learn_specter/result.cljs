(ns learn-specter.result
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [cljs.js :refer [empty-state eval js-eval]]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :refer [register-handler register-sub subscribe]]
            [com.rpl.specter :as s]))

(defn eval-str
  [s]
  (eval (empty-state)
        (read-string s)
        {:eval    js-eval
         :ns      'learn-specter.result
         :context :expr}
        (fn [result] result)))

(register-handler
  :eval-clicked
  (fn [db _]
    (when-let [input (:current-input db)]
      (assoc db :current-result (eval-str input)))))

(register-sub
  :result
  (fn [db _]
    (let [result (reaction (:current-result @db))]
      (reaction
        (if-let [error (:error @result)]
          (str "Error: " error)
          (:value @result))))))

(defn result
  []
  (let [result (subscribe [:result])]
    (fn []
      (when @result
        [:pre @result]))))



(ns learn-specter.result
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [cljs.js :refer [empty-state eval js-eval]]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :refer [register-handler register-sub subscribe]]))

(defn eval-str
  [s]
  (eval (empty-state)
        (read-string s)
        {:eval       js-eval
         :context    :expr}
        (fn [result] result)))

(register-handler
  :eval-clicked
  (fn [db _]
    (assoc db :current-result (eval-str (:current-input db)))))

(register-sub
  :result
  (fn [db _]
    (let [result (reaction (:current-result @db))]
      (println @result)
      (reaction
        (if-let [value (:value @result)]
          value
          (str "Error:\n" (:error @result)))))))

(defn result
  []
  (let [result (subscribe [:result])]
    (fn result-renderer
      []
      [:pre @result])))



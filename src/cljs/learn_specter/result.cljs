(ns learn-specter.result
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [cljs.js :refer [empty-state eval js-eval]]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :refer [register-sub subscribe]]))

(defn eval-str
  [s]
  (eval (empty-state)
        (read-string s)
        {:eval       js-eval
         :context    :expr}
        (fn [result] result)))

(register-sub
  :eval-result
  (fn [db _]
    (let [input (reaction (:current-input @db))]
      (reaction (:value (eval-str @input))))))

(defn result
  []
  (let [eval-result (subscribe [:eval-result])]
    (fn result-renderer
      []
      [:pre @eval-result])))



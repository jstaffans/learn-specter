(ns learn-specter.result
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [cljs.js :as cljs]
            [cljs.pprint :refer [pprint]]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :refer [register-handler register-sub subscribe]]))

(def ns-str "(ns learn-specter.eval (:require [com.rpl.specter :as s]))")

(defonce compiler-state (cljs/empty-state))

(defn eval-str
  [s]
  (cljs/eval-str
    compiler-state
    s
    'learn-specter.eval
    {:eval cljs/js-eval
     :ns   'learn-specter.eval}
    (fn [result] result)))

(register-handler
  :eval-clicked
  (fn [db]
    (if-let [input (:current-input db)]
      (assoc db :eval-input input)
      db)))

(register-sub
  :result
  (fn [db _]
    (let [eval-input (reaction (:eval-input @db))
          current-excercise (subscribe [:current-excercise])
          result (reaction (do (eval-str ns-str)
                               (eval-str (str "(let [ds " (str (:dataset @current-excercise)) "] " @eval-input ")"))))]
      (reaction
        (if-let [error (:error @result)]
          (str "Error: " error)
          (:value @result))))))

(defn result
  []
  (let [result (subscribe [:result])]
    (fn []
      (when @result
        [:pre (-> @result pprint with-out-str)]))))



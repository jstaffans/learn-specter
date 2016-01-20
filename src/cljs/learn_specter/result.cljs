(ns learn-specter.result
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [cljs.js :as cljs]
            [cljs.tools.reader :refer [read-string]]
            [re-frame.core :refer [register-handler register-sub subscribe]]))

(def ns-str "(ns learn-specter.eval (:require [com.rpl.specter :as s]))")

(defonce compiler-state (cljs/empty-state))

(defn eval-str
  [s]
  #_(.log js/console (str "Evaluating " s))
  (cljs/eval-str
    compiler-state
    s
    'learn-specter.eval
    {:eval    cljs/js-eval
     :ns      'learn-specter.eval}
    (fn [result] result)))

(register-handler
  :eval-clicked
  (fn [db [_ dataset]]
    (if-let [input (:current-input db)]
      (assoc
        db
        :current-result
        (do (eval-str ns-str)
            (eval-str (str "(let [ds " (str dataset) "] " input ")"))))
      db)))

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
        [:pre (-> @result cljs.pprint/pprint with-out-str)]))))



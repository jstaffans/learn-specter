(ns learn-specter.editor
  (:require [reagent.core :as reagent]
            [cljs.js :refer [empty-state eval js-eval]]
            [cljs.tools.reader :refer [read-string]]
            [cljsjs.codemirror]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.addon.edit.matchbrackets]
            [cljsjs.codemirror.addon.fold.foldgutter]
            [cljsjs.codemirror.addon.edit.closebrackets]
            [learn-specter.util :refer [debounce]]))

(def opts {:matchBrackets true
           :lineNumbers false
           :autoCloseBrackets true
           :theme "neat"
           :mode "clojure"
           :lineWrapping true})

(defn eval-str
  [s]
  (eval (empty-state)
        (read-string s)
        {:eval       js-eval
         :context    :expr}
        (fn [result]
          (do
            (.log js/console (:value result))
            result))))

(defn editor
  []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [namespace "learn-specter.user"
             dom-node (reagent/dom-node this)
             opts (clj->js opts)
             editor (.fromTextArea js/CodeMirror dom-node opts)]

         (.on editor "change" (debounce #(eval-str (.getValue editor))))))

     :reagent-render
     (fn [_]
       [:textarea {:default-value "(+ 1 1)"}])}))

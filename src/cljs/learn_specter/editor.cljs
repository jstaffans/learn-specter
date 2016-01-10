(ns learn-specter.editor
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch]]
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

(defn editor
  []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [namespace "learn-specter.user"
             dom-node (reagent/dom-node this)
             opts (clj->js opts)
             editor (.fromTextArea js/CodeMirror dom-node opts)]

         (.on editor "change" #(dispatch [:input-changed (.getValue editor)]))))

     :reagent-render
     (fn [_]
       [:textarea {:default-value "(+ 1 1)"}])}))

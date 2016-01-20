(ns learn-specter.editor
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch]]
            [cljsjs.codemirror]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.addon.edit.matchbrackets]
            [cljsjs.codemirror.addon.fold.foldgutter]
            [cljsjs.codemirror.addon.edit.closebrackets]
            [learn-specter.util :refer [debounce]]
            ))


(def opts {:matchBrackets true
           :lineNumbers false
           :autoCloseBrackets true
           :theme "neat"
           :mode "clojure"
           :lineWrapping true})

(defn editor
  []
  (fn [editor-content]
    (reagent/create-class
      {:component-did-mount
       (fn [this]
         (let [dom-node (reagent/dom-node this)
               opts (clj->js opts)
               editor (.fromTextArea js/CodeMirror dom-node opts)]
           (.on editor "change" #(dispatch [:input-changed (.getValue editor)]))))

       :reagent-render
       (fn [_]
         [:textarea {:default-value @editor-content}])})))

(ns learn-specter.editor
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch subscribe]]
            [cljsjs.codemirror]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.addon.edit.matchbrackets]
            [cljsjs.codemirror.addon.fold.foldgutter]
            [cljsjs.codemirror.addon.edit.closebrackets]))


(def opts {:matchBrackets true
           :lineNumbers false
           :autoCloseBrackets true
           :theme "neat"
           :mode "clojure"
           :lineWrapping true})

;; Hook to editor to be able to reset value manually when showing a new page
(def editor-ref (atom nil))

(defn set-editor-value!
  [value]
  (when-let [editor @editor-ref]
    (.setValue editor value)))

(defn editor
  []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [dom-node (reagent/dom-node this)
             opts (clj->js opts)
             editor (.fromTextArea js/CodeMirror dom-node opts)]
         (reset! editor-ref editor)
         (.on editor "change" #(dispatch [:input-changed (.getValue editor)]))))

     :reagent-render
     (fn []
       [:textarea {:default-value input}])}))

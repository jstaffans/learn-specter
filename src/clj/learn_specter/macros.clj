(ns learn-specter.macros
  (:require [clojure.java.io :refer [resource]]
            [clojure.edn :as edn]
            [markdown.core :as markdown]))

(defmacro defedn
  "Read edn file from resources/edn."
  [symbol-name edn-name]
  (let [content (edn/read-string (slurp (resource (str "edn/" edn-name))))]
    `(def ~symbol-name ~content)))

(defmacro defmd
  "Read markdown from a file, converts it a to a html string."
  [symbol-name md-name]
  (let [content (markdown/md-to-html-string (slurp (resource (str "md/" md-name))))]
    `(def ~symbol-name ~content)))
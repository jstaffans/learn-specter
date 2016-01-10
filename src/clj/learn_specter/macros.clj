(ns learn-specter.macros
  (:require [clojure.java.io :refer [resource file]]
            [clojure.edn :as edn]
            [markdown.core :as markdown]))

(defmacro defedn
  "Read edn file from resources/edn."
  [symbol-name edn-name]
  (let [content (edn/read-string (slurp (resource (str "edn/" edn-name))))]
    `(def ~symbol-name ~content)))

(defmacro defcontent
  "Reads in the markdown files containing the tutorial content."
  [symbol-name path]
  (let [files (file-seq (file path))
        html-pages (->> files
                        (filter #(.isFile %))
                        (mapv (comp markdown/md-to-html-string slurp)))]
    `(def ~symbol-name ~html-pages)))
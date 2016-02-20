(ns learn-specter.excercises
  (:require-macros [learn-specter.macros :refer [defedn]]))

(defedn movies "movies.edn")

(def page-excercises
  [{:dataset    movies
    :preview-fn (fn [ds] (subvec ds 0 2))
    :tasks      [{:text     "Convert all names to upper case"
                  :hint     "(s/transform ...)"
                  :solution "(s/transform [s/ALL :name] clojure.string/upper-case movies)"}
                 {:text     "Retrieve all movies with rating greater than 8.0"
                  :hint     "(s/select ...)"
                  :solution "(s/select [(s/filterer #(= (:director %) \"James Cameron\"))] movies)"}]}

   {:dataset    movies
    :preview-fn (fn [ds] (subvec ds 0 2))
    :tasks      [{:text     "Convert all names to upper case"
                  :hint     "(s/transform ...)"
                  :solution "(s/transform [s/ALL :name] clojure.string/upper-case movies)"}
                 {:text     "Retrieve all movies with rating greater than 8.0"
                  :hint     "(s/select ...)"
                  :solution "(s/select [(s/filterer #(= (:director %) \"James Cameron\"))] movies)"}]}])

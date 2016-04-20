(set-env!
  :source-paths #{"sass" "src/clj" "src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[[adzerk/boot-cljs "1.7.228-1" :scope "test"]
                  [adzerk/boot-cljs-repl "0.3.0" :scope "test"]
                  [com.cemerick/piggieback "0.2.1" :scope "test"]
                  [weasel                  "0.7.0"  :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                  [adzerk/boot-reload "0.4.7" :scope "test"]
                  [pandeiro/boot-http "0.6.3" :scope "test"]
                  [mathias/boot-sassc "0.1.1" :scope "test"]
                  [org.clojure/clojurescript "1.7.228"]
                  [reagent "0.5.0"]
                  [re-frame "0.7.0-alpha"]
                  [bidi "1.25.0"]
                  [secretary "1.2.3"]
                  [kibu/pushy "0.3.6"]
                  [com.rpl/specter "0.9.1"]
                  [cljsjs/codemirror "5.8.0-0"]
                  [markdown-clj "0.9.85"]])

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[pandeiro.boot-http :refer [serve]]
  '[mathias.boot-sassc :refer [sass]])

(deftask build []
         (comp (cljs)
            (sass :output-dir "css")))

(deftask run []
  (comp (serve)
     (watch)
     (cljs-repl)
     (reload)
     (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced}
                 sass {:output-style "compressed"})
  identity)

(deftask development []
  ;; TODO: install devtools automatically
  (set-env! :source-paths #(conj % "dev/cljs")
            :dependencies #(conj % '[binaryage/devtools "0.5.2"]))
  (task-options! cljs {:optimizations :none :source-map true}
                 reload {:on-jsload 'learn-specter.app/init}
                 sass {:line-numbers true
                       :source-maps  true})
  identity)

(deftask dev
         "Simple alias to run application in development mode"
         []
         (comp (development)
            (run)))

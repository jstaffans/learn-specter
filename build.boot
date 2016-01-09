(set-env!
  :source-paths #{"sass" "src/clj" "src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[[adzerk/boot-cljs "1.7.48-6" :scope "test"]
                  [adzerk/boot-cljs-repl "0.2.0" :scope "test"]
                  [adzerk/boot-reload "0.4.1" :scope "test"]
                  [pandeiro/boot-http "0.6.3" :scope "test"]
                  [mathias/boot-sassc "0.1.1" :scope "test"]
                  [org.clojure/clojurescript "1.7.228"]
                  [reagent "0.5.0"]
                  [re-frame "0.7.0-alpha"]
                  [com.rpl/specter "0.9.1"]])

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


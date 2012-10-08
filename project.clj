(defproject tetris "0.1-SNAPSHOT"
  :description "Tetris in Clojurescript"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [enfocus "1.0.0-alpha3"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [compojure "1.1.3"]]
  :dev-dependencies [[clojure-source "1.4.0"]
                     [midje "1.4.0"]]
  :min-lein-version "2.0.0"
  :hooks [leiningen.cljsbuild]
  :plugins [[lein-cljsbuild "0.2.7"]]
  :cljsbuild {:crossovers [tetris.core tetris.scoring]
              :crossover-path "crossover-cljs"
              :builds {:dev {:source-path "src"
                             :compiler {:output-to "public/out/main.js"
                                        :optimizations :advanced
                                        :pretty-print false}}
                       :whyowhyisdevstandard {:source-path "src"
                                    :compiler {:output-to "public/out/main.js"
                                               :optimizations :whitespace
                                               :pretty-print true}}}})

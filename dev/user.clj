(ns user
  (:require [compliment.core]
            [com.github.lxbr.cljs-complete]
            [cljs.repl :as repl]
            [cljs.repl.node :as node]))

(defn node-repl
  []
  (repl/repl (node/repl-env)))

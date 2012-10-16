;; ## Settle the score
;; Rules extracted from Colin Fahey's nice writeup on Tetris:
;; [Colin's page](http://www.colinfahey.com/tetris/tetris.html)

(ns libtetris.scoring)

(defn calc-level
  "Calculates level from total number of `lines` removed.  
  Max level is 10 and starts at 91 lines."
  [lines]
  (cond
   (> lines 91) 10
   (pos? lines) (inc (quot (dec lines) 10))
   :default 0))

(defn score-lines
  "Calculate score based on number of `lines` removed at once and current `level`.  
  More lines removed at once give better score."
  [lines level]
  (let [base-score {1 40 2 100 3 300 4 1200}]
    (* (base-score lines level) (inc level))))

(defn- interval [level]
  (* 50 (- 11 level)))

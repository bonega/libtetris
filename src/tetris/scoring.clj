(ns tetris.scoring)

(defn calc-level [lines]
  (cond
   (> lines 91) 10
   (pos? lines) (inc (quot (dec lines) 10))
   :default 0))

(defn score-lines [lines level]
  (let [base-score {1 40 2 100 3 300 4 1200}]
    (* (base-score lines level) (inc level))))

(defn- interval [level]
  (* 50 (- 11 level)))

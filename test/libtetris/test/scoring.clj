(ns libtetris.test.scoring
  (:use midje.sweet libtetris.scoring))

(facts "score"
  (score-lines 1 0) => 40
  (score-lines 4 0) => 1200
  (score-lines 1 1) => 80
  (score-lines 4 1) => 2400
  (score-lines 1 2) => 120
  (score-lines 4 2) => 3600)


(facts "level"
  (calc-level -5) => 0
  (calc-level 0) => 0
  (calc-level 1) => 1
  (calc-level 10) => 1
  (calc-level 11) => 2
  (calc-level 20) => 2
  (calc-level 90) => 9
  (calc-level 91) => 10
  (calc-level 98) => 10
  (calc-level 1382353) => 10)


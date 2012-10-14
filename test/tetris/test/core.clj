(ns tetris.test.core
  (:use midje.sweet tetris.core))

(defn refer-private [ns]
  (doseq [[symbol var] (ns-interns ns)]
    (when (:private (meta var))
      (intern *ns* symbol var))))

(refer-private 'tetris.core)

(def state (assoc (build-state) :block t-block ))
(def drop-states (iterate drop-block state))

(facts "rotations"
       (-> state rot rot rot rot) => state
       (rot state) =not=> state)

(facts "moves"
       (-> state r l) => state
       (l state) =not=> state
       (r state) =not=> state
       (d state) =not=> state
       (nth (iterate drop-block state) 15) => #(:gameover? %))

(facts "inside board" (-> (assoc-in state [:block :x] 10) valid-state?) => falsey
  (-> (assoc-in state [:block :x] -1) valid-state?) => falsey
  (-> (assoc-in state [:block :y] 30) valid-state?) => falsey
  (-> (assoc-in state [:block :y] -1) valid-state?) => falsey
  (valid-state? state) => state)

(facts "events"
  (-> state drop-block :new-block?) => true)

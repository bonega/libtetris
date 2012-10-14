(ns tetris.core
  (:use [tetris.scoring :only [calc-level score-lines]]))

(def columns 10)
(def rows 20)

(defrecord Block [x y rotations current-rot])

(defn make-block [& rotations]
  (Block. 4 0 rotations 0))

(def i-block (make-block
              [[ 0 1 ]
               [ 0 1 ]
               [ 0 1 ]
               [ 0 1 ] ]

              [[ 0 0 0 0 ]
               [ 1 1 1 1 ] ]))

(def s-block (make-block
              [[ 0 2 2 ]
               [ 2 2 0 ] ]

              [[ 0 2 0 ]
               [ 0 2 2 ]
               [ 0 0 2 ] ]))

(def z-block (make-block
              [[ 3 3 0 ]
               [ 0 3 3 ] ]

              [[ 0 0 3 ]
               [ 0 3 3 ]
               [ 0 3 0 ] ]))

(def o-block (make-block
              [[ 0 0 0 ]
               [ 0 4 4 ]
               [ 0 4 4 ] ]))

(def t-block (make-block
              [[ 5 5 5 ]
               [ 0 5 0 ] ]

              [[ 0 5 0 ]
               [ 5 5 0 ]
               [ 0 5 0 ] ]

              [[ 0 5 0 ]
               [ 5 5 5 ] ]

              [[ 0 5 0 ]
               [ 0 5 5 ]
               [ 0 5 0 ] ]))

(def l-block (make-block
              [[ 6 0 0 ]
               [ 6 0 0 ]
               [ 6 6 0 ] ]

              [[ 0 0 0 ]
               [ 6 6 6 ]
               [ 6 0 0 ] ]

              [[ 6 6 0 ]
               [ 0 6 0 ]
               [ 0 6 0 ] ]

              [[ 0 0 0 ]
               [ 0 0 6 ]
               [ 6 6 6 ] ]))

(def j-block (make-block
              [[ 0 7 0 ]
               [ 0 7 0 ]
               [ 7 7 0 ] ]

              [[ 0 0 0 ]
               [ 7 0 0 ]
               [ 7 7 7 ] ]

              [[ 7 7 0 ]
               [ 7 0 0 ]
               [ 7 0 0 ] ]

              [[ 0 0 0 ]
               [ 7 7 7 ]
               [ 0 0 7 ] ]))

(def blocks [i-block j-block l-block o-block s-block t-block z-block])

(defrecord Square [x y color])
(defrecord State [grid block next-block score lines level])
(def empty-row (vec (repeat columns 0)))

(defn- row->squares [row y] (map-indexed (fn [x v] (Square. x y v)) row))

(defn grid->squares [grid]
  (let [ind-grid (map-indexed (fn [y row] (row->squares row y)) grid)
        occupied? (comp pos? :color)]
    (filter occupied? (flatten ind-grid))))

(defn- set-square [grid {:keys [x y color] :as square}]
  (when (and grid (< -1 x columns) (< -1 y rows)
	     (-> grid (get-in [y x]) pos? not))
    (assoc-in grid [y x] color)))

(defn- set-squares [g squares]
  (reduce set-square g squares))

(defn block->grid [{:keys [rotations current-rot]}]
  (nth rotations current-rot))

(defn commit-block [{:keys [grid block] :as state}]
  (let [{:keys [x y]} block
        squares (-> block block->grid grid->squares)
	offset  #(merge-with + % (Square. x y 0))
	offset-squares (map offset squares)
	new-grid (set-squares grid offset-squares)]
    (when new-grid (assoc state :grid new-grid))))

(defn- valid-state? [state] (when (commit-block state) state))

(defn build-state []
  (let [grid (vec (repeat rows empty-row))
	block (rand-nth blocks)
	next-block (rand-nth blocks)
	score 0
	lines 0
        level 0]
   (State. grid block next-block score lines level)))

(def row-full? (partial every? pos?))

(defn- assoc-event [state kw v]
  (with-meta state (merge (meta state) {kw v})))

(defn get-event [state kw]
  (-> state meta kw))

(defn new-block? [state]
  (get-event state :new-block))

(defn- clear-lines [{:keys [grid lines score level] :as state}]
  (if (not-any? row-full? grid)
    state
    (let [removed-lines (count (filter row-full? grid))
          removed-grid (remove row-full? grid)
          new-lines (repeat removed-lines empty-row)
          new-grid (vec (concat new-lines removed-grid))
          new-score (+ score (score-lines removed-lines level))
          completed-lines (+ lines removed-lines)]
      (-> state (assoc :lines completed-lines :grid new-grid
             :score new-score :level (calc-level completed-lines))
          (assoc-event :lines-cleared)))))


(defn- transform [f kw f-fail state]
  (if (:gameover state)
    state
    (or (valid-state? (-> state (update-in [:block kw] f) (with-meta {})))
        (f-fail state))))

(defn- take-block [state]
  (or (-> state (assoc :block (:next-block state) :next-block (rand-nth blocks))
          (assoc-event :new-block true) valid-state?)
      (assoc state :gameover true :block nil)))

(defn rot [state] (let [rot-nr (-> state :block :rotations count)]
                    (transform #(mod (inc %) rot-nr) :current-rot identity state)))
(defn l [state] (transform dec :x identity state))
(defn r [state] (transform inc :x identity state))
(defn d [state] (let [fail-f #(-> % commit-block clear-lines take-block)]
		  (transform inc :y fail-f state)))

(defn drop-block [state]
  (let [state (with-meta state {})
        drop-states (iterate d state)
        drop-done (fn [s] (when (or (new-block? s)
                                 (:gameover s)) s))]
    (some drop-done drop-states)))

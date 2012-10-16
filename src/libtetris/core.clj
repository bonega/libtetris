;; ## A functional library for Tetris
;; To start with an immutable state do: `(build-state)`.  
;; 'Modify' a state by transforming it with one of the
;; move-functions:
;;
;;     [rot d l r drop-block]
;;
;; Example ```(-> (build-state) l l l drop-block) ```  
;; Moves the block three units to the left and then
;; drops the block down until it reaches the bottom.
(ns libtetris.core
  (:use [libtetris.scoring :only [calc-level score-lines]]))

(def columns 10)
(def rows 20)

;; ## Grid representation
;; A grid is a 2d vector of numbers.  
;; All positive numbers correspond to a occupied cell.  
;; The number usually represents a color in a draw-function  
;; A non-positive value is considered empty.
;; Example:  
;;
;;     [[ 0 1 ]
;;      [ 0 1 ]
;;      [ 0 1 ]
;;      [ 0 1 ]]

(defn grid->str
  "Returns `grid` converted to a nicely formatted string."
  [grid]
  (apply str (mapcat #(conj % \newline) grid)))

;; ## Block record
;; Every block have a `x` and `y` position.  
;; `rotations` are defined as a vector of `grid`.  
;; `current-rot` is the offset to the `rotations`-vector.
(defrecord Block [x y rotations current-rot])

(defn make-block
  "Creates a block with a default position.  
  Rotations are a vector of grids."
  [& rotations] (Block. 4 0 rotations 0))

;; ## Definition of blocks
(def i-block (make-block
              [[ 0 1 ]
               [ 0 1 ]
               [ 0 1 ]
               [ 0 1 ]]

              [[ 0 0 0 0 ]
               [ 1 1 1 1 ]]))

(def s-block (make-block
              [[ 0 2 2 ]
               [ 2 2 0 ]]

              [[ 0 2 0 ]
               [ 0 2 2 ]
               [ 0 0 2 ]]))

(def z-block (make-block
              [[ 3 3 0 ]
               [ 0 3 3 ]]

              [[ 0 0 3 ]
               [ 0 3 3 ]
               [ 0 3 0 ]]))

(def o-block (make-block
              [[ 0 0 0 ]
               [ 0 4 4 ]
               [ 0 4 4 ]]))

(def t-block (make-block
              [[ 5 5 5 ]
               [ 0 5 0 ]]

              [[ 0 5 0 ]
               [ 5 5 0 ]
               [ 0 5 0 ]]

              [[ 0 5 0 ]
               [ 5 5 5 ]]

              [[ 0 5 0 ]
               [ 0 5 5 ]
               [ 0 5 0 ]]))

(def l-block (make-block
              [[ 6 0 0 ]
               [ 6 0 0 ]
               [ 6 6 0 ]]

              [[ 0 0 0 ]
               [ 6 6 6 ]
               [ 6 0 0 ]]

              [[ 6 6 0 ]
               [ 0 6 0 ]
               [ 0 6 0 ]]

              [[ 0 0 0 ]
               [ 0 0 6 ]
               [ 6 6 6 ]]))

(def j-block (make-block
              [[ 0 7 0 ]
               [ 0 7 0 ]
               [ 7 7 0 ]]

              [[ 0 0 0 ]
               [ 7 0 0 ]
               [ 7 7 7 ]]

              [[ 7 7 0 ]
               [ 7 0 0 ]
               [ 7 0 0 ]]

              [[ 0 0 0 ]
               [ 7 7 7 ]
               [ 0 0 7 ]]))

(def blocks [i-block j-block l-block o-block s-block t-block z-block])

(defrecord Square [x y color])

;; ## State record
;; State keeps track of all parts necessary to drive a simple
;; Tetris-game.
;; Implements `toString`, `str` returns a nicely formatted string.
(defrecord State [grid block next-block score lines level]
  Object
  (toString [_] (grid->str grid)))

(defn print-state
  "Prints a nicely formatted representation of the state"
  [state]
  (-> state str print))

;; ### Events
;; Events only live for one transformation.  
;; All events are forgotten after the next transformation.  
;; At the moment only `:new-block?` and `:removed-lines` are used.

(def events [:removed-lines :new-block?])

(defn reset-events
  "Returns `state` with all `events` as false"
  [state]
  (apply dissoc state events))

(def empty-row (vec (repeat columns 0)))

(defn build-state
  "Factory for initial state."
  []
  (let [grid (vec (repeat rows empty-row))
        block (rand-nth blocks)
        next-block (rand-nth blocks)
        score 0
        lines 0
        level 0]
   (State. grid block next-block score lines level)))

;; ## Grid-related conversions
;; Used both for transformation of the current state and eventual
;; visual representation.

(defn- row->squares
  "Converts `row` to a vector of Square-records."
  [row y] (map-indexed (fn [x v] (Square. x y v)) row))

(defn grid->squares
  "Converts `grid` to a vector of Square-records."
  [grid]
  (let [ind-grid (map-indexed (fn [y row] (row->squares row y)) grid)
        occupied? (comp pos? :color)]
    (filter occupied? (flatten ind-grid))))

(defn block->grid
  "Converts `block` to `grid`."
  [{:keys [rotations current-rot]}]
  (nth rotations current-rot))

;; ## Grid manipulating

(defn- set-square
  "Returns a new `grid` or nil if the `square` placement is invalid."
  [grid {:keys [x y color] :as square}]
  (when (and grid (< -1 x columns) (< -1 y rows)
             (-> grid (get-in [y x]) pos? not))
    (assoc-in grid [y x] color)))

(defn- set-squares
  "Returns a new `grid` updated by a vector of Square-records.  
  Or `nil` if the new grid is invalid."
  [grid squares]
  (reduce set-square grid squares))

(defn commit-block
  "Returns a new `state` with the current `block` merged.  
  Or `nil` if the new state is invalid."
  [{:keys [grid block] :as state}]
  (let [{:keys [x y]} block
        squares (-> block block->grid grid->squares)
        offset  #(merge-with + % (Square. x y 0))
        offset-squares (map offset squares)
        new-grid (set-squares grid offset-squares)]
    (when new-grid (assoc state :grid new-grid))))

(def row-full? (partial every? pos?))

(defn- clear-lines
  "Returns new state with any full rows removed.  
  Also does some score-keeping.  
  Associates a `:removed-lines` with `state`."
  [{:keys [grid lines score level] :as state}]
  (if (not-any? row-full? grid)
    state
    (let [removed-lines (count (filter row-full? grid))
          removed-grid (remove row-full? grid)
          new-lines (repeat removed-lines empty-row)
          new-grid (vec (concat new-lines removed-grid))
          new-score (+ score (score-lines removed-lines level))
          completed-lines (+ lines removed-lines)]
      (assoc state
        :lines completed-lines
        :grid new-grid
        :score new-score
        :level (calc-level completed-lines)
        :removed-lines removed-lines))))

;; ## Transformations
;; Every transform of a state should happen here.  
;; `transform` is the main concern here.  
;; It is used to ensure that we newer see an invalid state.  
;; Don`t try to manipulate the grid directly.

(defn- valid-state?
  "Returns merged `state` if it's valid.  
  Else just return the original `state`."
  [state] (when (commit-block state) state))

(defn- transform
  "Applies `f` to `kw` if resulting state is valid return it.  
  Else return `f-fail` applied to `state`)."
  [f kw f-fail state]
  (if (:gameover? state)
    state
    (or (valid-state? (-> state (update-in [:block kw] f) reset-events))
        (f-fail state))))

(defn- take-block
  "Returns new `state` updated with a new `block`"
  [state]
  (or (-> state (assoc :block (:next-block state)
                       :next-block (rand-nth blocks)
                       :new-block? true)
          valid-state?)
      (assoc state :gameover? true :block nil)))

(defn rot [state] (let [rot-nr (-> state :block :rotations count)]
                    (transform #(mod (inc %) rot-nr) :current-rot identity state)))
(defn l [state] (transform dec :x identity state))
(defn r [state] (transform inc :x identity state))
(defn d [state] (let [fail-f #(-> % commit-block clear-lines take-block)]
                  (transform inc :y fail-f state)))

(defn drop-block
  "Moves current block down until a `:new-block` event is seen,  
  or if the state changes to :gameover?."
  [state]
  (let [drop-states (iterate d (reset-events state))
        drop-done (fn [s] (when (or (:new-block? s)
                                 (:gameover? s)) s))]
    (some drop-done drop-states)))

(ns tetris.core)

(def columns 10)
(def rows 20)

(defrecord Block [x y rotations current-rot type])

(defmacro defblock [name & rotations]
  `(def ~name (Block. 4 0 [~@rotations] 0 ~(keyword name))))

(defblock i-block 
  [[ 0 2 ]
   [ 0 2 ]
   [ 0 2 ]
   [ 0 2 ] ]    
    
  [[ 0 0 0 0 ]
   [ 2 2 2 2 ] ])

(defblock s-block 
  [[ 0 3 3 ]
   [ 3 3 0 ] ] 
   
  [[ 0 3 0 ]
   [ 0 3 3 ]
   [ 0 0 3 ] ])

(defblock z-block 
  [[ 1 1 0 ]
   [ 0 1 1 ] ]  
  
  [[ 0 0 1 ]
   [ 0 1 1 ]
   [ 0 1 0 ] ])

(defblock o-block 
  [[ 0 0 0 ]
   [ 0 1 1 ]
   [ 0 1 1 ] ])

(defblock t-block 
  [[ 1 1 1 ]
   [ 0 1 0 ] ]
    
  [[ 0 1 0 ]
   [ 1 1 0 ]
   [ 0 1 0 ] ]
    
  [[ 0 1 0 ]
   [ 1 1 1 ] ]
    
  [[ 0 1 0 ]
   [ 0 1 1 ]
   [ 0 1 0 ] ])

(defblock l-block 
  [[ 1 0 0 ]
   [ 1 0 0 ]
   [ 1 1 0 ] ]
    
  [[ 0 0 0 ]
   [ 1 1 1 ]
   [ 1 0 0 ] ]
    
  [[ 1 1 0 ]
   [ 0 1 0 ]
   [ 0 1 0 ] ]
    
  [[ 0 0 0 ]
   [ 0 0 1 ]
   [ 1 1 1 ] ])

(defblock j-block 
  [[ 0 1 0 ]
   [ 0 1 0 ]
   [ 1 1 0 ] ]
      
  [[ 0 0 0 ]
   [ 1 0 0 ]
   [ 1 1 1 ] ]

  [[ 1 1 0 ]
   [ 1 0 0 ]
   [ 1 0 0 ] ]

  [[ 0 0 0 ]
   [ 1 1 1 ]
   [ 0 0 1 ] ])

(def blocks [i-block j-block l-block o-block s-block t-block z-block])

(defrecord Square [x y color])
(defrecord State [grid block next-block score lines])
(def empty-row (vec (repeat columns 0)))

(defn- occupied? [square] (-> square :color pos?))

(defn- row->squares [row y] (map-indexed (fn [x v] (Square. x y v)) row))

(defn grid->squares [grid]
  (let [ind-grid (map-indexed (fn [y row] (row->squares row y)) grid)]
    (filter occupied? (flatten ind-grid))))

(defn- set-square [grid {:keys [x y color] :as square}]
  (when (and grid (< -1 x columns) (< -1 y rows)
	     (-> grid (get-in [y x]) pos? not))
    (assoc-in grid [y x] color)))

(defn- set-squares [g squares]
  (reduce set-square g squares))

(defn commit-block [{:keys [grid block] :as state}] 
  (let [{:keys [rotations current-rot x y]} block
        squares (grid->squares (get rotations current-rot))
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
	lines 0]
   (State. grid block next-block score lines)))

(defn- clear-lines [{:keys [grid lines] :as state}]
  (let [row-full? (fn [row] (every? pos? row))
	removed-lines (count (filter row-full? grid))
        removed-grid (remove row-full? grid)
	new-lines (repeat removed-lines empty-row)
	new-grid (vec (concat new-lines removed-grid))]
    (assoc state :lines (+ lines removed-lines) :grid new-grid)))

(defn- transform [f kw f-fail state]
  (or (when (:gameover state) state)
      (valid-state? (update-in state [:block kw] f))
      (f-fail state)))

(defn- draw-block [state] 
  (or (valid-state? (assoc state :block (:next-block state) :next-block (rand-nth blocks)))
      (assoc state :gameover true :block nil)))



(defn rot [state] (transform #(mod (inc %) (-> state :block :rotations count)) :current-rot identity state))
(defn l [state] (transform dec :x identity state))
(defn r [state] (transform inc :x identity state))
(defn d [state] (let [fail-f #(-> % commit-block clear-lines draw-block)]
		  (transform inc :y fail-f state)))

(ns tetris.interface
  (:use [tetris.core :only [build-state commit-block block->grid grid->squares
                            r l d rot drop-block]])
  (:require-macros [enfocus.macros :as em])
  (:require [enfocus.core :as ef] 
            [goog.Timer :as timer]
            [goog.events :as events]
            [goog.events.KeyHandler :as keyhandler]
            [goog.events.KeyCodes :as keycodes]
            [goog.dom :as dom]))

(def xsize 200)
(def ysize 400)

(def colormap {1 "red"
 	       2 "cyan"
 	       3 "orange"
               4 "blue"
               5 "green"
               6 "magenta"
               7 "yellow"})

(def movemap {keycodes/LEFT l
	      keycodes/UP rot
	      keycodes/RIGHT r
	      keycodes/DOWN d
              keycodes/SPACE drop-block})

(def animation-fps 30)

(defn handle-key [key state]
  (swap! state (movemap key/keyCode)))

(defn draw-square [{:keys [x y color]} surface]
  (let [cell-size 20
        x (* x cell-size)
        y (* y cell-size)]
    (fill-rect surface x y cell-size cell-size (colormap color))
    (stroke-rect surface x y cell-size cell-size "black")))

(defn draw-grid [grid surface]
  (let [squares (-> grid grid->squares)]
    (.clearRect surface 0 0 xsize ysize)
    (doseq [square squares]
      (draw-square square surface))))

(defn draw-state [state surface]
  (draw-grid (-> state commit-block :grid) surface))

(defn draw-gameover [surface]
  (set! (. surface -font) "bold 30px sans-serif")
  (set! (. surface -fillStyle) "black")
  (.fillText surface "Gameover!" 20 190))

(defn reset-state [state] (reset! state (build-state)))

(em/defaction restart-button [state]
  ["#restart-button"] (em/listen :click #(reset-state state)))

(em/defaction refresh-info [state]
  ["#score"] (em/content (-> state :score str))
  ["#lines"] (em/content (-> state :lines str))
  ["#level"] (em/content (-> state :level str)))

(defn surface [selector]
  (let [surface (first (dom/query selector))]
    (.getContext surface "2d")))

(defn stroke-rect [surface x y width height color]
  (set! (. surface -fillStyle) color)
  (.strokeRect surface x y width height))

(defn fill-rect [surface x y width height color]
  (set! (. surface -fillStyle) color)
  (.fillRect surface x y width height))

(defn- interval [level]
  (* 50 (- 11 level)))

(defn gravity [state gravitator]
  (swap! state d)
  (.setInterval gravitator (-> @state :level interval)))

(defn animate [{:keys [state board next-block gravitator]}]
  (draw-state @state board)
  (draw-grid (-> @state :next-block :rotations first) next-block)
  (refresh-info @state)
  (when (:gameover @state)
    (draw-gameover board)))

(defn pre-setup []
  (let [state (atom (build-state))
        board (surface "div canvas#board")
        next-block (surface "div canvas#next-block")
        gravitator (goog.Timer. 400)
        animator (goog.Timer. (/ 1000 animation-fps))
        keyhandler (goog.events.KeyHandler. js/window)
        app-state {:board board :next-block next-block :gravitator gravitator
                   :animator animator :state state}]
    (restart-button state)

    (events/listen animator timer/TICK #(animate app-state))
    (events/listen gravitator timer/TICK #(gravity state gravitator))
    (events/listen keyhandler "key" #(handle-key %1 state))

    (.start gravitator)
    (.start animator)))

(set! (.-onload js/window) pre-setup)

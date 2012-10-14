(ns tetris.interface
  (:use [tetris.core :only (build-state commit-block grid->squares r l d rot)]
        seesaw.core
        seesaw.dev
        seesaw.graphics
        seesaw.border
        seesaw.color)
  (:require [seesaw.bind :as b]))

(def xsize 200)
(def ysize 400)

(def colormap {1 :red
 	       2 :cyan
 	       3 :orange
               4 :blue
               5 :green
               6 :magenta
               7 :yellow})

(def animation-sleep-ms 15)
(def gravity-sleep-ms 400)

(def running true)
(def gravity-running true)

(defn print-state [state]
  (doseq [row (-> state commit-block :grid)]
    (println (apply str row))))

(defn draw-square [{:keys [x y color]} g]
  (let [size 20
        x (* x size)
        y (* y size)]
    (draw g
          (rect x y size size)
          (style :background (colormap color)))))

(defn draw-squares [g squares]
  (doseq [square squares]
    (draw-square square g)))

(defn draw-grid [g grid]
  (draw-squares g (-> grid grid->squares )))

(defn draw-state [g state]
  (draw-grid g (-> state commit-block :grid)))

(defn make-canvas [state]
  (canvas :id :board :paint #(draw-state %2 @state) :preferred-size [xsize :by ysize]
          :focusable? true :maximum-size [xsize :by ysize]
          :minimum-size [xsize :by ysize]
          :border (line-border :thickness 2 :color :black)))

(defn make-next-block [state]
  (canvas :id :next-block
          :paint #(draw-grid %2 (-> @state :next-block :rotations first))
          :preferred-size [60 :by 80]))

(defn make-control-panel [state]
  (border-panel :id :control-panel :hgap 10
                :north (flow-panel :items [(label :text "next-block:") (make-next-block state)])
                :south (flow-panel :items [(label :text "lines:")
                        (label :text 0 :id :lines)
                        (label :text "score:")
                        (label :text 0 :id :score)])))

(defn make-frame [state]
  (frame :title "Clojure Tetris"
         :visible? true
         :on-close :dispose
         :content (border-panel :id :bord
                                :hgap 10 :vgap 10
                                :east (make-control-panel state)
                                :west (make-canvas state))))

(def movemap {37 l
	      38 rot
	      39 r
	      40 d})

(defn handle-key [state key]
   (let [k (.getKeyCode key)] (when (movemap k)
       (swap! state (movemap k)))))

(defn animation [{:keys [state f] :as appstate}]
  (when running
    (send-off *agent* animation))
  (Thread/sleep animation-sleep-ms)
  (config! (select f [:#lines]) :text (:lines @state))
  (repaint! (select f [:#board]))
  (repaint! (select f [:#next-block]))
  appstate)

(defn gravity [state]
  (when gravity-running
    (send-off *agent* gravity))
  (if (:gameover @state)
     (reset! state (build-state))
     (swap! state d))
  (Thread/sleep gravity-sleep-ms)
  state)

(defn setup []
  (let [state (atom (build-state))
        f (make-frame state)
        app-state {:f f :state state}
        animator (agent app-state)
        gravitator (agent state)
        board (select f [:#board])]
    (native!)
    (-> f pack! show!)
    (listen board :key-pressed (partial #'handle-key state))
    (request-focus! board)
    (send-off animator animation)
    (send-off gravitator gravity)
    (merge app-state {:animator animator :gravitator gravitator})))

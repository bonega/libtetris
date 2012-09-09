(ns tetris.interface
  (:use [tetris.core :only (build-state commit-block grid->squares r l d rot)]
        seesaw.core
        seesaw.dev
        seesaw.graphics
        seesaw.border
        seesaw.color)
  (:require [seesaw.bind :as b]))

(def xsize 215)
(def ysize 440)

(def colormap {1 (to-color :black)
 	       2 (to-color :red)
 	       3 (to-color :blue)})

(def animation-sleep-ms 15)
(def gravity-sleep-ms 400)

(def running true)
(def gravity-running true)

(defn print-state [state]
  (doseq [row (-> state commit-block :grid)]
    (println (apply str row))))

(defn clear [g] (.clearRect g 0 0 xsize ysize))

(defn draw-square [{:keys [x y color]} g]
  (let [x (* x 20)
	y (* y 20)]
    (doto g
      (.setColor (colormap color))
      (.fillRect x y 20 20))))

(defn draw-state [c g state]
  (let [squares (-> state commit-block :grid grid->squares)]
    (do (clear g)
        (doseq [square squares] (draw-square square g)))))


(defn make-panel [state]
  (canvas :id :board :paint #(draw-state %1 %2 @state) :preferred-size [400 :by 400]))

(defn make-frame [state]
  (let [f (frame :title "Clojure Tetris"
                 :visible? true
                 :on-close :dispose
                 :content (border-panel :id :bord
                                        :hgap 10 :vgap 10
                                        :east   (text :text 0 :id :lines)
                                        :west (make-panel state)))]
    f))

(def movemap {37 l
	      38 rot
	      39 r
	      40 d})

(defn handle-key [key state]
  (when (movemap key)
      (swap! state (movemap key))))

(defn animation [{:keys [state f] :as appstate}]
  (when running
    (send-off *agent* animation))
  (Thread/sleep animation-sleep-ms)
  (config! (select f [:#lines]) :text (:lines @state))
  (repaint! (select f [:#board]))
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
        gravitator (agent state)]
    (native!)
    (-> f pack! show!)
    (listen (select f [:#board]) :key-pressed (partial handle-key state))
    (send-off animator animation)
    (send-off gravitator gravity)
    (merge app-state {:animator animator :gravitator gravitator})))

(ns tetris.interface
  (:import
   (java.awt Color Dimension)
   (java.awt.event KeyListener)
   (javax.swing JFrame JOptionPane JPanel))
  (:use [tetris.core :only (build-state commit-block grid->squares r l d rot)]))

(def xsize 215)
(def ysize 440)

(def colormap {1 Color/BLACK
	       2 Color/RED
	       3 Color/BLUE})

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

(defn draw-state [state g]
  (let [squares (-> state commit-block :grid grid->squares)]
    (do (clear g)
	(doseq [square squares] (draw-square square g)))))

(def state (atom nil))
(def panel (atom nil))
(def frame (atom nil))

(def movemap {37 l
	      38 rot
	      39 r
	      40 d})

(defn handle-key [k]
  (let [key (.getKeyCode k)]
    (when (movemap key)
      (swap! state (movemap key)))))

(defn make-panel []
  (doto
    (proxy [JPanel KeyListener] []
      (paintComponent [g]
        (proxy-super paintComponent g)
	(draw-state @state g))
      (keyPressed [e] (handle-key e))
      (keyReleased [e])
      (keyTyped [e]))
    (.setFocusable true)))

(defn make-frame []
  (doto (JFrame. "Tetris")
    (.add @panel)
    (.pack)
    (.setVisible true)
    (.setSize (java.awt.Dimension. xsize ysize))
    (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)))

(def animator (agent nil))

(defn animation [x]
  (when running
    (send-off *agent* animation))
  (Thread/sleep animation-sleep-ms)
  (.repaint @panel)
  nil)

(def gravitator (agent nil))

(defn gravity [x]
  (when gravity-running
    (send-off *agent* gravity))
  (if (:gameover @state)
     (reset! state (build-state))
     (swap! state d))
  (. Thread (sleep gravity-sleep-ms))
  nil)

(defn setup []
  (do
    (reset! state (build-state))
    (reset! panel (make-panel))
    (reset! frame (make-frame))
    (.addKeyListener @panel @panel)
    (send-off animator animation)
    (send-off gravitator gravity)))

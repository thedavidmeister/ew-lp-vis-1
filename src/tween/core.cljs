(ns tween.core
 (:require [javelin.core :as j]
           [hoplon.core :as h]
           [thi.ng.ndarray.core :as nd]))

(def animation-speed 3000)
(def fps (* (/ animation-speed 1000) 10))

; Not actually 0 but performance.now() returns values larger than the first
; frame of RAF, even before we've called RAF.
; https://stackoverflow.com/questions/38360250/requestanimationframe-now-vs-performance-now-time-discrepancy
(def t (j/cell 1))
(def t-a (j/cell 1))
(def t-b (j/cell 2))

(defn update-ts
 [duration]
 (j/dosync (reset! t-a @t) (reset! t-b (+ @t duration))))

(letfn [(frame [tick]
         (.requestAnimationFrame js/window frame)
         (reset! t tick))]
 (.requestAnimationFrame js/window frame))

(def p (j/cell= (-> (/ (- t t-a) (- t-b t-a))
                    (min 1)
                    (max 0))))
                    ; (* fps)
                    ; int
                    ; (/ fps))))

(j/cell= (when (= 1 p) (update-ts animation-speed)))

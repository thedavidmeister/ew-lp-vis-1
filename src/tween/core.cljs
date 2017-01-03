(ns tween.core
 (:require [javelin.core :as j]
           [hoplon.core :as h]
           [thi.ng.ndarray.core :as nd]))

(def animation-speed 3000)
(def fps (/ 1000 30))

; Not actually 0 but performance.now() returns values larger than the first
; frame of RAF, even before we've called RAF.
; https://stackoverflow.com/questions/38360250/requestanimationframe-now-vs-performance-now-time-discrepancy
(def t (j/cell 1))
(def t-a (j/cell 1))
(def t-b (j/cell 2))

(defn update-ts
 [duration]
 (j/dosync (reset! t-a @t) (reset! t-b (+ @t duration))))

(letfn [(frame []
         (h/with-timeout fps (frame))
         (reset! t (-> js/window .-performance .now)))]
 (h/with-timeout fps (frame)))

(def dirty (j/cell true))
(defn dirty! [] (reset! dirty true))
(def p (j/cell= (-> (/ (- t t-a) (- t-b t-a))
                    (min 1)
                    (max 0))))
                    ; (* fps)
                    ; int
                    ; (/ fps))))
(j/cell= (when p (dirty!)))

(j/cell= (when (= 1 p) (update-ts animation-speed)))

(ns tween.core
 (:require [javelin.core :as j]
           [hoplon.core :as h]))

; Not actually 0 but performance.now() returns values larger than the first
; frame of RAF, even before we've called RAF.
; https://stackoverflow.com/questions/38360250/requestanimationframe-now-vs-performance-now-time-discrepancy
(def t (j/cell 0))
(letfn [(frame [tick]
         (.requestAnimationFrame js/window frame)
         (reset! t tick))]
 (.requestAnimationFrame js/window frame))

(defn tween-cell
 [{:keys [from to duration cb]}]
 (let [precision 10
       v-a (j/cell from)
       v-b (j/cell to)

       t-a (j/cell @t)
       t-b (j/cell (+ @t-a duration))

       p (j/cell= (if (> t t-a)
                      (if (> t-b t)
                          (quot (* precision (/ (- t t-a) (- t-b t-a)))
                                precision)
                          1)
                      0))

       ; Current position, [0, 1]
       p (j/cell= (-> (/ (- t t-a) (- t-b t-a))
                      (min 1)
                      (max 0)))

       ; Current val [v-a, v-b]
       v (j/cell= (+ v-a (* p (- v-b v-a))))]
  ; (j/cell= (prn p))

  (j/with-let [c (j/cell= v (fn [to]
                             (j/dosync
                              (reset! v-a @v)
                              (reset! v-b to)
                              (reset! t-a @t)
                              (reset! t-b (+ @t-a duration)))))]
   ; Hook in a callback to receive the cell after the stack has cleared.
   (add-watch p :cb (fn [_ _ _ n] (when (= n 1) (h/with-timeout 0 (cb c))))))))

(def tween (tween-cell {:from 0
                        :to 500
                        :duration 5000
                        :cb (fn [c] (reset! c (if (= 500 @c) 0 500)))}))

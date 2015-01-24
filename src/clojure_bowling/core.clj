(ns clojure-bowling.core
  (:use midje.sweet))

(def initial-state {:score 0 :bonus [0 0] :frame-number 0})

(defn new-score [score roll bonus main-frame-factor]
  (+ score (* roll (+ main-frame-factor bonus))))

(defn roll-type [first-roll roll]
  (let [first-roll? (nil? first-roll)
        spare (and (not first-roll?) (= 10 (+ first-roll roll)))
        strike (and first-roll? (= 10 roll))]
    (cond
      strike :strike
      spare :spare
      first-roll? :first
      :else :second)))

(defn new-bonus [next-bonus
                 roll-type
                 main-frame-factor]
  (let [new-bonus (* main-frame-factor (if (contains? #{:strike :spare} roll-type) 1 0))
        new-next-bonus (* main-frame-factor (if (= :strike roll-type) 1 0))]
    [(+ next-bonus new-bonus) new-next-bonus]))

(defn next-frame-number [frame-number roll-type]
  (+ frame-number (case roll-type
                    :first 0
                    1)))

(defn main-frame-factor [frame-number]
  (if (< frame-number 10) 1 0))

(defn next-state [state roll]
  (let [roll-type (roll-type (:first-roll state) roll)
        main-frame-factor (main-frame-factor (:frame-number state))
        [current-bonus next-bonus] (:bonus state)]
    {:score (new-score (:score state) roll current-bonus main-frame-factor)
     :bonus (new-bonus next-bonus roll-type main-frame-factor)
     :frame-number (next-frame-number (:frame-number state) roll-type)}))

(defn bowling-score [rolls]
  (:score (reduce next-state initial-state rolls)))

(ns bowling-score.core
  (:use midje.sweet))

(declare next-state new-score new-bonus roll-type first-roll)

(def initial-state
  {:score 0
   :bonus [0 0]
   :first-roll nil})

(defn bowling-score [rolls]
  (->> rolls
    (reduce next-state initial-state)
    :score))

(defn next-state [state roll]
  (let [[bonus next-bonus] (:bonus state)]
    {:score (-> state :score (new-score bonus roll))
     :bonus (new-bonus next-bonus (roll-type (:first-roll state) roll))
     :first-roll (first-roll (:first-roll state) roll)}))

(defn new-score [score bonus roll]
  (->> roll
    (* (+ 1 bonus))
    (+ score)))

(defn roll-type [first-roll roll]
  (let [is-first (nil? first-roll)
        is-spare (and (not is-first) (= 10 (+ first-roll roll)))
        is-strike (and is-first (= 10 roll))]
    (cond
      is-strike :strike
      is-spare :spare
      :else :simple)))

(defn new-bonus [bonus roll-type]
  (let [new-bonus (if (contains? #{:strike :spare} roll-type) 1 0)
        next-bonus (if (= :strike roll-type) 1 0)]
    [(+ bonus new-bonus) next-bonus]))

(defn first-roll [previous roll]
  (cond
    (= 10 roll) nil
    (nil? previous) roll
    :else nil))

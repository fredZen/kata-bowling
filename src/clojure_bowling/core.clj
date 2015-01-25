(ns clojure-bowling.core
  (:use midje.sweet))

(declare initial-state next-state new-score roll-type new-bonus next-frame bonus-round?)

(defn bowling-score [rolls] 
	(->> rolls 
		(reduce next-state initial-state)
		:score))

(def initial-state {
	:score 0
	:bonus [0 0]
	:frame 0})

(defn next-state [state roll]
	(let [
		roll-type (roll-type (:first-roll-of-frame state) roll)
		[bonus next-bonus] (:bonus state)] (conj
		{:frame 0
			:score (new-score (:score state) roll bonus (bonus-round? (:frame state)))
			:bonus (new-bonus roll-type next-bonus) }
		(next-frame roll roll-type))))

(defn new-score [old-score roll bonus bonus-round?]
	(+ old-score (* roll (+ (if bonus-round? 0 1) bonus))))

(defn roll-type [previous roll] 
	(let [
		is-first (nil? previous)
		spare (and
			(not is-first)
			(= 10 (+ previous roll)))
		strike (and
			is-first
			(= 10 roll))]
	(cond
		spare :spare
		strike :strike
		is-first :first
		:else :second)))

(defn new-bonus [roll-type remaining-bonus] 
	(let [
		next-bonus (if (= :strike roll-type) 1 0)
		new-bonus (if (contains? #{:strike :spare} roll-type) 1 0)]
	[(+ remaining-bonus new-bonus) next-bonus]))

(defn next-frame [roll roll-type]
	(if (= roll-type :first)
		{:first-roll-of-frame roll}
		{}))

(defn bonus-round? [frame] 
	(>= frame 10))

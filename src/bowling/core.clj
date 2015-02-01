(ns bowling.core)

(declare initial-state next-state new-score new-bonus roll-type first-roll in-game setup-frame)

(def initial-state
  {:score 0
   :bonus [0 0]
   :frame 0})

(defn score [rolls]
  (:score (reduce next-state initial-state rolls)))

(defn next-state [state roll]
  (let [roll-type (roll-type (:first-roll state) roll)
        [bonus next-bonus] (:bonus state)
        frame (:frame state)
        in-game (in-game frame)]
    (conj
      (setup-frame roll roll-type frame)
      {:score (new-score (:score state) roll bonus in-game)
       :bonus (new-bonus roll-type next-bonus in-game)})))

(defn new-score [score roll bonus in-game]
  (+ score (* roll (+ (if in-game 1 0) bonus))))

(defn roll-type [first-roll roll]
  (let [is-first (nil? first-roll)
        is-strike (and is-first (= 10 roll))
        is-spare (and (not is-first) (= 10 (+ first-roll roll)))]
    (cond
      is-spare :spare
      is-strike :strike
      is-first :first
      :else :second)))

(defn new-bonus [roll-type next-bonus in-game]
  (if (not in-game)
    [next-bonus 0]
    (case roll-type
      :spare [1 0]
      :strike [(+ 1 next-bonus) 1]
      [next-bonus 0])))

(defn setup-frame [roll roll-type frame]
  (if (= :first roll-type)
    {:first-roll roll :frame frame}
    {:frame (inc frame)}))

(defn in-game [frame]
  (< frame 10))

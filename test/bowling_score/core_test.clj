(ns bowling-score.core-test
  (:use midje.sweet)
  (:use [bowling-score.core]))

(facts "about bowling scores"
  (fact "If you roll all gutters, your score will be zero"
    (bowling-score (repeat 20 0)) => 0)
  (fact "If you never roll a strike or a spare, then your score is just the sum of your rolls"
    (-> [1 2 3 4]
      (concat (repeat 16 0))
      bowling-score) => 10)
  (fact "The two rolls after a strike are doubled"
    (-> [10 3 4]
      (concat (repeat 16 0))
      bowling-score) => 24)
  (future "The roll after a spare is doubled"
    (-> [0 10 3 4]
      (concat (repeat 16 0))
      bowling-score) => 20))

(fact "`bowling-score` extracts the score from the last state of the game"
  (bowling-score [..roll-1.. ..roll-2..]) => ..final-score..
  (provided
    (next-state (next-state initial-state ..roll-1..) ..roll-2..) => {:score ..final-score..}))

(fact "`next-state` computes the new score"
  (next-state ..state.. ..roll..) => (contains {:score ..new-score.. :bonus ..new-bonus.. :first-roll ..new-first-roll..})
  (provided
    ..state.. =contains=> {:score ..score.. :bonus [..bonus.. ..next-bonus..] :first-roll ..first-roll..}
    (new-score ..score.. ..bonus.. ..roll..) => ..new-score..
    (roll-type ..first-roll.. ..roll..) => ..roll-type..
    (new-bonus ..next-bonus.. ..roll-type..) => ..new-bonus..
    (first-roll ..first-roll.. ..roll..) => ..new-first-roll..))

(facts "about `new-score`"
  (fact "in the general case, the roll is just added to the score"
    (new-score 40 0 7) => 47)
  (fact "with a simple bonus, the roll is doubled"
    (new-score 40 1 7) => 54))

(facts "about `new-bonus`"
  (fact "after a simple roll, you do not get an extra bonus"
    (new-bonus 0 :simple) => [0 0])
  (fact "after a simple roll, you keep a remaining strike bonus"
    (new-bonus 1 :simple) => [1 0])
  (fact "after a spare, you get a bonus for the next roll"
    (new-bonus 0 :spare) => [1 0])
  (fact "after a strike, you get a bonus for two rolls"
    (new-bonus 0 :strike) => [1 1])
  (fact "if you roll a second strike in a row, you get a double bonus"
    (new-bonus 1 :strike) => [2 1]))

(facts "about `roll-type`"
  (fact "in general you roll a simple roll"
    (roll-type nil 4) => :simple
    (roll-type 4 3) => :simple)
  (fact "if you topple all pins on the second roll, then that's a spare"
    (roll-type 6 4) => :spare)
  (fact "if the first roll in the frame is a 10, then that's a strike"
    (roll-type nil 10) => :strike))

(facts "about `first-roll`"
  (fact "if there already was a first roll, then this roll is the second, so a new frame begins, thus there is no first roll (yet)"
    (first-roll 3 5) => nil)
  (fact "if you roll a strike, then a new frame begins, thus there is no first roll (yet)"
    (first-roll nil 10) => nil)
  (fact "otherwise, this roll is the first roll"
    (first-roll nil 8) => 8))

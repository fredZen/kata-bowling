(ns clojure-bowling.core-test
  (:use midje.sweet)
  (:use [clojure-bowling.core]))

(fact "`bowling-score` computes the score from the last state"
  (bowling-score [..roll-1.. ..roll-2..]) => ..score..
  (provided
    (next-state (next-state initial-state ..roll-1..) ..roll-2..) => {:score ..score..}))

(facts "about `next-state"
  (against-background [(main-frame-factor anything) => irrelevant
                       (roll-type anything anything) => irrelevant
                       (new-score anything anything anything anything) => irrelevant
                       (new-bonus anything anything anything) => irrelevant
                       (next-frame-number anything anything) => irrelevant])
  (fact "it computes the new score"
    (next-state ..state.. ..roll..) => (contains {:score ..new-score..})
    (provided
      ..state.. =contains=> {:score ..score.. :bonus [..bonus..] :frame-number ..frame-number..}
      (new-score ..score.. ..roll.. ..bonus.. (main-frame-factor ..frame-number..)) => ..new-score..))
  (fact "it computes the new bonus"
    (next-state ..state.. ..roll..) => (contains {:bonus ..new-bonus..})
    (provided
      ..state.. =contains=> {:bonus [..current-bonus.. ..next-bonus..] :first-roll ..first-roll.. :frame-number ..frame-number..}
      (new-bonus ..next-bonus.. (roll-type ..first-roll.. ..roll..) (main-frame-factor ..frame-number..)) => ..new-bonus..))
  (fact "it computes the new frame number"
    (next-state ..state.. ..roll..) => (contains {:frame-number ..new-frame-number..})
    (provided
      ..state.. =contains=> {:frame-number ..frame-number.. :first-roll ..first-roll..}
      (next-frame-number ..frame-number.. (roll-type ..first-roll.. ..roll..)) => ..new-frame-number..)))

(facts "about `new-score`"
  (fact "For a plain roll, it adds the roll's value to the current score"
    (new-score 100 6 0 1) => 106)
  (fact "When there is a bonus, the rolls is counted twice"
    (new-score 100 6 1 1) => 112)
  (fact "On the last frame, you only get the bonus (not the main roll)"
    (new-score 100 6 1 0) => 106))

(facts "about `new-bonus`"
  (fact "for a spare, the bonus is 1"
    (new-bonus 0 :spare 1) => [1 0])
  (fact "for a strike, the bonus is 1, then 1"
    (new-bonus 0 :strike 1) => [1 1])
  (fact "for a first, the bonus is 0"
    (new-bonus 0 :first 1) => [0 0])
  (fact "for a second, the bonus is 0"
    (new-bonus 0 :second 1) => [0 0])
  (fact "for a double, the bonus is 2, then 1"
    (new-bonus 1 :strike 1) => [2 1])
  (fact "a first does not erase a strike bonus"
    (new-bonus 1 :first 1) => [1 0])
  (fact "you don't get any extra bonus during the bonus roll"
    (new-bonus 1 :strike 0) => [1 0]))

(facts "about `roll-type`"
  (fact "a 10 as the first roll of the frame is a strike"
    (roll-type nil 10) => :strike)
  (fact "a second roll that sums to 10 with the first roll of the frame is a spare"
    (roll-type 8 2) => :spare)
  (fact "if there was a first roll, then this is the second roll of the frame"
    (roll-type 3 5) => :second)
  (fact "if there was no first roll, then this is the first roll of the frame"
    (roll-type nil 5) => :first))

(facts "about `next-frame-number`"
  (fact "after a first roll, you stay in the same frame for the second roll"
    (next-frame-number 4 :first) => 4)
  (fact "in all other cases, you go to the next frame"
    (next-frame-number 3 :spare) => 4
    (next-frame-number 4 :strike) => 5
    (next-frame-number 5 :second) => 6))

(fact "frames number 10 and up are bonus frames"
  (main-frame-factor 8) => 1
  (main-frame-factor 10) => 0
  (main-frame-factor 11) => 0)

(fact "a perfect is worth 300 points"
  (bowling-score (repeat 12 10)) => 300)

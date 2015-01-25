(ns clojure-bowling.core-test
  (:use midje.sweet)
  (:use [clojure-bowling.core]))

(facts "about bowling score"
  (fact "you get no points for a full game of gutters"
    (bowling-score (repeat 20 0)) => 0)
  (fact "if you roll no strike or spare, the score is just the sum of the rolls"
    (bowling-score (concat [1 2 3 4] (repeat 16 0))) => 10)
  (fact "the roll after a spare is doubled"
    (bowling-score (concat [1 9 3 4] (repeat 16 0))) => 20)
  (fact "the two rolls after a strike are doubled"
    (bowling-score (concat [10 3 4] (repeat 16 0))) => 24)
  (future-fact "a perfect is worth 300 points"
    (bowling-score (repeat 12 10)) => 300))

(fact "the `bowling-score` is the score of the last state of the game"
  (bowling-score [..roll-1.. ..roll-2..]) => ..score..
  (provided
    (next-state (next-state initial-state ..roll-1..) ..roll-2..) => {:score ..score..}))

(fact "`next-state` computes the new game state"
  (next-state ..state.. ..roll..) => (contains {:score ..score.. :bonus ..bonus.. :first-roll-of-frame ..first-roll-of-frame..})
  (provided
    ..state.. =contains=> {:score ..old-score.. :bonus [..old-bonus.. ..next-bonus..] :first-roll-of-frame ..old-first-roll-of-frame.. :frame ..frame..}
    (new-score ..old-score.. ..roll.. ..old-bonus.. (bonus-round? ..frame..)) => ..score..
    (roll-type ..old-first-roll-of-frame.. ..roll..) => ..roll-type..
    (new-bonus ..roll-type.. ..next-bonus..) => ..bonus..
    (next-frame ..roll.. ..roll-type..) => {:first-roll-of-frame ..first-roll-of-frame..}))

(fact "about `new-score`"
  (fact "generally, the roll is added to the score"
    (new-score 100 6 0 false) => 106)
  (fact "the roll is added twice when there is a simple bonus"
    (new-score 100 6 1 false) => 112)
  (fact "the roll is added three times when there is a double bonus"
    (new-score 100 6 2 false) => 118)
  (fact "after the 10th frame, you can get bonus points but no main points"
    (new-score 100 6 2 true) => 112))

(facts "about `roll-type`"
  (fact "If this is the first roll of the frame, and I didn't knock down all pins, then this is a first"
    (roll-type nil 6) => :first)
  (fact "If this is the second roll of the frame, and I knocked down all pins, then this is a strike"
    (roll-type nil 10) => :strike)
  (fact "If this is the second roll of the frame, and I didn't knock down all pins, then this is a first"
    (roll-type 1 6) => :second)
  (fact "If this is the second roll of the frame, and I knocked down all pins, then this is a spare"
    (roll-type 6 4) => :spare))

(facts "about `new-bonus`"
  (fact "if I didn't knock down all pins in the previous frame, then there is no bonus"
    (new-bonus :first 0) => [0 0]
    (new-bonus :second 0) => [0 0])
  (fact "after a spare, I get a bonus"
    (new-bonus :spare 0) => [1 0])
  (fact "after a strike, I get a bonus for two rounds"
    (new-bonus :strike 0) => [1 1])
  (fact "a first does not erase the previous strike bonus"
    (new-bonus :first 1) => [1 0])
  (fact "after two strikes you get a double bonus"
    (new-bonus :strike 1) => [2 1]))

(fact "about `next-frame`"
  (fact "after a first roll, the first roll of the frame is that roll"
    (next-frame 5 :first) => (contains {:first-roll-of-frame 5}))
  (fact "after anything else, a new frame begins that has no first roll yet"
    (next-frame 5 :second) =not=> (contains :first-roll-of-frame)
    (next-frame 5 :spare) =not=> (contains :first-roll-of-frame)))

(fact "The first 10 frames are actual game frames, the rest are bonus rounds"
  (bonus-round? 0) => falsey
  (bonus-round? 9) => falsey
  (bonus-round? 10) => truthy
  (bonus-round? 11) => truthy)
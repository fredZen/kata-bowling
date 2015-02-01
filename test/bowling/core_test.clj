(ns bowling.core-test
  (:use midje.sweet)
  (:use [bowling.core]))

(facts "about bowling"
  (fact "if you only roll gutters, your final score is 0"
    (score (repeat 20 0)) => 0)
  (fact "as long as you never knock down all the pins in a frame, your score is just the sum of your rolls"
    (score (concat [1 2 3 4] (repeat 16 0))) => 10)
  (fact "the roll after a spare is doubled"
    (score (concat [1 9 3 4] (repeat 16 0))) => 20)
  (fact "the two rolls after a strike are doubled"
    (score (concat [10 3 4] (repeat 16 0))) => 24)
  (fact "a perfect is worth 300"
    (score (repeat 12 10)) => 300))

(facts "about `score`"
  (fact "the score is extracted from the last game state"
    (score [..roll-1.. ..roll-2..]) => ..final-score..
    (provided
      (next-state (next-state initial-state ..roll-1..) ..roll-2..) => {:score ..final-score..})))

(facts "about `next-state`"
  (fact "it computes the new state"
    (next-state ..state.. ..roll..) => (contains {:score ..new-score..
                                                  :bonus ..new-bonus..
                                                  :first-roll ..new-first-roll..
                                                  :frame ..new-frame..})
    (provided
      ..state.. =contains=> {:score ..old-score..
                             :bonus [..old-bonus.. ..next-bonus..]
                             :first-roll ..first-roll..
                             :frame ..frame..}
      (in-game ..frame..) => ..in-game..
      (new-score ..old-score.. ..roll.. ..old-bonus.. ..in-game..) => ..new-score..
      (roll-type ..first-roll.. ..roll..) => ..roll-type..
      (new-bonus ..roll-type.. ..next-bonus.. ..in-game..) => ..new-bonus..
      (setup-frame ..roll.. ..roll-type.. ..frame..) => {:first-roll ..new-first-roll.. :frame ..new-frame..})))

(facts "about `new-score`"
  (fact "in general, your roll is added to the score"
    (new-score 0 0 0 true) => 0
    (new-score 0 2 0 true) => 2
    (new-score 100 5 0 true) => 105)
  (fact "if you have a bonus multiplier, your score is added multiple times"
    (new-score 100 5 1 true) => 110)
  (fact "if you are in a bonus round, then you get only the bonus points"
    (new-score 100 5 1 false) => 105))

(facts "about `roll-type`"
  (facts "first roll of frame"
    (fact "if you don't knock down all pins then this is a first roll"
      (roll-type nil 7) => :first)
    (fact "if you knock down all pins then this is a strike"
      (roll-type nil 10) => :strike))
  (facts "second roll of frame"
    (fact "if you don't knock down all pins then this is a second roll"
      (roll-type 1 7) => :second)
    (fact "if you knowck down all pins then this is a spare"
      (roll-type 3 7) => :spare)))

(facts "about `new-bonus`"
  (fact "for a plain roll you get no bonus"
    (new-bonus :first 0 true) => [0 0]
    (new-bonus :second 0 true) => [0 0])
  (fact "your previous bonus is not erased by a new bonus or lack thereof"
    (new-bonus :first 1 true) => [1 0]
    (new-bonus :strike 1 true) => [2 1])
  (fact "for a spare you get a one-time bonus"
    (new-bonus :spare 0 true) => [1 0])
  (fact "for a strike you get two boni"
    (new-bonus :strike 0 true) => [1 1])
  (fact "if you are in a bonus round, then you cannot accumulate new boni"
    (new-bonus :strike 1 false) => [1 0]))

(facts "about `setup-frame`"
  (fact "after a second roll (including a spare) a new frame starts, which has no first roll yet"
    (setup-frame 4 :second 1) => {:frame 2}
    (setup-frame 4 :spare 3) => {:frame 4})
  (fact "after a first roll, that roll becomes the new first roll of the current frame"
    (setup-frame 5 :first 4) => {:first-roll 5 :frame 4}))

(facts "about `in-game`"
  (fact "you are in the game on frames 0 to 9"
    (in-game 0) => truthy
    (in-game 9) => truthy)
  (fact "after that, you are in the bonus rounds"
    (in-game 10) => falsey
    (in-game 11) => falsey))

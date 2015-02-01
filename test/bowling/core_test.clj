(ns bowling.core-test
  (:use midje.sweet)
  (:use [bowling.core]))

(future-facts "about bowling"
  (fact "when you roll all gutters, you final score is zero"
    (bowling-score (repeat 20 0)) => 0)
  (fact "if you never topple all pins in a frame, then your score is just the sum of your rolls"
    (bowling-score (concat [1 2 3 4] (repeat 16 0))) => 10)
  (fact "the points are doubled for the roll after a spare"
    (bowling-score (concat [1 9 3 4] (repeat 16 0))) => 20)
  (fact "the points are doubled for two rolls after a strike"
    (bowling-score (concat [10 3 4] (repeat 16 0))) => 24)
  (fact "in the roll after a double, points are tripled"
    (bowling-score (concat [10 10 1 1] (repeat 14 0))) => 35)
  (fact "a perfect is worth 300 points"
    (bowling-score (repeat 12 10)) => 300))

(fact "`bowling-score` extracts the score from the final state of the game"
  (bowling-score [..roll-1.. ..roll-2..]) => ..final-score..
  (provided
    (next-state (next-state initial-state ..roll-1..) ..roll-2..) => {:score ..final-score..}))

(fact "`next-state` computes the new score"
  (next-state ..state.. ..roll..) => (contains {:score ..new-score..
                                                :bonus ..new-bonus..
                                                :first-roll ..new-first-roll..
                                                :frame ..new-frame..})
  (provided
    ..state.. =contains=> {:score ..old-score..
                           :bonus [..bonus.. ..next-bonus..]
                           :first-roll ..first-roll..
                           :frame ..frame..}
    (roll-type ..first-roll.. ..roll..) => ..roll-type..
    (new-frame ..frame.. ..roll.. ..roll-type..) => {:first-roll ..new-first-roll..
                                                     :frame ..new-frame..}
    (in-game? ..frame..) => ..in-game..
    (new-bonus ..roll-type.. ..next-bonus.. ..in-game..) => ..new-bonus..
    (new-score ..old-score.. ..roll.. ..bonus.. ..in-game..) => ..new-score..))

(facts "about `new-score`"
  (facts "during the main game"
    (fact "generally, the roll is added to the score"
      (new-score 0 0 0 true) => 0
      (new-score 100 0 0 true) => 100
      (new-score 100 7 0 true) => 107)
    (fact "when you have a bonus, the roll is added twice"
      (new-score 100 5 1 true) => 110))
  (fact "during the bonus rounds after the main game, you only get the bonus points"
    (new-score 100 5 1 false) => 105))

(facts "about `roll-type`"
  (facts "if there was no first roll"
    (fact "it is the first roll of the frame..."
      (roll-type nil 7) => :first)
    (fact "... except if all pins were knocked down, in which case it is a strike"
      (roll-type nil 10) => :strike))
  (facts "if there was a first roll"
    (fact "it is the second roll of the frame..."
      (roll-type 3 5) => :second)
    (fact "... except if you knocked down all pins, in which case it is a spare"
      (roll-type 3 7) => :spare)))

(facts "about `new-bonus`"
  (facts "during the game"
    (fact "for a strike the bonus is 1 for two rounds"
      (new-bonus :strike 0 true) => [1 1])
    (fact "on the second round after a strike, I still have 1 bonus"
      (new-bonus :first 1 true) => [1 0]
      (new-bonus :strike 1 true) => [2 1])
    (fact "for a spare the bonus is 1"
      (new-bonus :spare 0 true) => [1 0])
    (fact "for plain old rolls you get no bonus"
      (new-bonus :first 0 true) => [0 0]
      (new-bonus :second 0 true) => [0 0]))
  (fact "after the game, you don't earn new bonuses"
    (new-bonus :strike 0 false) => [0 0]
    (new-bonus :strike 1 false) => [1 0]))

(facts "about `new-frame`"
  (fact "after a second roll or a spare, a new frame begins so there is no first roll"
    (new-frame 5 7 :second) => {:frame 6}
    (new-frame 5 7 :spare) => {:frame 6})
  (fact "after a first roll, the current roll becomes the new first roll of the frame"
    (new-frame 5 8 :first) => {:first-roll 8 :frame 5}))

(fact "you are in the game for 10 frames (0 to 9)"
  (in-game? 0) => truthy
  (in-game? 9) => truthy
  (in-game? 10) => falsey
  (in-game? 11) => falsey)

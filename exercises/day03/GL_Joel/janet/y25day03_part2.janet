#!/usr/bin/env janet

# Return array of strings with file contents
(defn get-lines [filename]
  (def lines @[])
  (with [f (file/open filename)]
        (each line (file/lines f)
          (array/push lines (string/trim line))))
  lines)

(defn parse-power-bank [line]
  (map (fn [char] (scan-number (buffer/from-bytes char))) line))

(defn find-largest-batteries [power-bank &opt n-batteries batteries index-of-largest]
  (default n-batteries 2) # find n amount of batteries
  (default batteries @[]) # batteries found
  (default index-of-largest 0) # place where the last battery was found
  (if (= (length batteries) n-batteries)
    batteries # if n-batteries found return them
    (do
      (var largest 0) # largest battery found
      (var largest-index 0) # place where it was found
      # max index to search for batteries
      (def limit (- (length power-bank) (- n-batteries (length batteries))))
      (loop [i :range-to [index-of-largest limit]]
        (let [battery-at-index (i power-bank)]
          (when (> battery-at-index largest) # keep the largest found so far
            (set largest battery-at-index)
            (set largest-index i))))
      (array/push batteries largest)
      (array/remove power-bank largest-index) # remove largest battery to not repeat it
      # continue search
      (find-largest-batteries power-bank n-batteries batteries largest-index))))

(defn main [& args]
  (def lines (get-lines "y25day03_input.txt"))
  # (def lines (get-lines "y25day03_demo.txt"))
  (def power-banks (map parse-power-bank lines))

  (var joltage-sum 0)
  (each power-bank power-banks
    (pp power-bank)
    (def largest (find-largest-batteries power-bank 12))
    (pp largest)
    (def joltage (scan-number (string ;largest)))
    (+= joltage-sum joltage))

  (print joltage-sum))

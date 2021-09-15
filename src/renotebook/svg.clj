(ns renotebook.svg
  (:require
   [clojure.pprint :as pp]
   [clojure.string :as str])
  (:gen-class))

;; Library for writing notebook data in SVG file format.

;; Utility functions
(defn get-strokes
  "Retrieve the strokes from a page layer data."
  [pagedata]
  (-> pagedata
      :layers
      first
      :strokes))

(defn get-stroke
  "Retrieve segment from stroke data."
  [pagedata]
  (-> pagedata
      :layers
      first
      :strokes
      first
      :segments))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; stroke  - Clojure vector of points as :xpos :ypos values
;;           corresponding to a single pen stroke
;; strokes - Clojure vector of strokes
;; path    - Clojure vector of nodes stored as [x y] pairs.

(defn convert-stroke-to-path
  "Convert stroke (with segments) to path points. Returns a vector."
  [stroke]
  (mapv (fn [point] [(:xpos point) (:ypos point)]) stroke))

(defn convert-strokes [strokes]
  "Convert list of strokes into vector of vectors of points."
  (mapv (fn [stroke]
          (convert-stroke-to-path (:segments stroke)))
        strokes))

;; Convert array or point to string.
(defn format-line
  "Format line data into an SVG line string."
  [line]
  (str "M "
       (str/join " "
                 (map (fn [[x y]] (str x "," y)) line))))

(defn format-style
  "Create an SVG string containing the line format data."
  [style]
  (str/join
   ";"
   (map (fn [[key value]] (str (name key) ":" value)) style)))

(defn path-string
  "Create an SVG string with path data, including style information."
  [line style]
  (str "<path style=\"" (format-style style) "\" "
       "d=\"" (format-line line) "\" />"))

;; SVG Line format
;; Default style
(def style
  {:opacity           "1"
   :vector-effect     "none"
   :fill              "none"
   :fill-opacity      "0"
   :stroke            "#000000"
   :stroke-width      "5.00"
   :stroke-linecap    "round"
   :stroke-linejoin   "miter"
   :stroke-miterlimit "4"
   :stroke-dasharray  "none"
   :stroke-dashoffset "0"
   :stroke-opacity    "1"})

;; TODO Convert to a Clojure struccture
;; SVG header
(def header
  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>
<svg
   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"
        xmlns:cc=\"http://creativecommons.org/ns#\"
   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"
   xmlns:svg=\"http://www.w3.org/2000/svg\"
   xmlns=\"http://www.w3.org/2000/svg\"
   width=\"1404px\"
   height=\"1872px\"
   viewBox=\"0 0 1404 1872\"
   style=\"background-color:white\"
   version=\"1.1\"
   id=\"svg8\">
  <defs
     id=\"defs2\" />
  <metadata
     id=\"metadata5\">
    <rdf:RDF>
      <cc:Work
         rdf:about=\"\">
        <dc:format>image/svg+xml</dc:format>
        <dc:type
           rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\" />
        <dc:title></dc:title>
      </cc:Work>
    </rdf:RDF>
  </metadata>
  <g
     id=\"layer1\">
")

;; TODO Convert to a Clojure struccture
;;SVG footer
(def footer " </g>
</svg>
")

;; TODO Set default filename
(defn write
  "Write notebook page data to an SVG file."
  [& {:keys [pagedata filename]
      :or {filename "resources/default.svg"}}]
  (let [content (apply str
                       (mapv path-string
                             (convert-strokes (get-strokes pagedata))
                             (repeat style)))]
    (spit filename (str header content footer))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def font
  {:a [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 3]]
       [[3 4]
        [3 0]]
       [[4 0]]]
   :b [[[0 7]
        [0 0]]
       [[0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 3]
        [2 4]
        [1 4]
        [0 3]]]
   :c [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]]
   :d [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]
       [[3 7]
        [3 0]]]
   :e [[[0 2]
        [3 2]
        [3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]]
   :f [[[3 7]
        [2 7]
        [1 6]
        [1 0]]
       [[3 4]
        [0 4]]]
   :g [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]
       [[3 4]
        [3 -2]
        [2 -3]
        [1 -3]
        [0 -2]]]
   :h [[[0 7]
        [0 0]]
       [[0 3]
        [1 4]
        [2 4]
        [3 3]
        [3 0]]]
   :i [[[1 4]
        [2 4]
        [2 0]]
       [[2 5]
        [1 5]]]
   :j [[[1 4]
        [2 4]
        [2 -2]
        [1 -3]
        [0 -3]]
       [[2 5]
        [1 5]]]
   :k [[[0 7]
        [0 0]]
       [[3 4]
        [0 1]]
       [[1 2]
        [3 0]]]
   :l [[[1 7]
        [1 0]
        [2 0]]]
   :m [[[0 4]
        [0 0]]
       [[0 4]
        [1 4]
        [1 0]]
       [[1 4]
        [2 4]
        [3 3]
        [3 0]]]
   :n [[[0 4]
        [0 0]]
       [[0 3]
        [1 4]
        [2 4]
        [3 3]
        [3 0]]]
   :o [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 3]]]
   :p [[[0 4]
        [0 -3]]
       [[0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 3]
        [2 4]
        [1 4]
        [0 3]]]
   :q [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]
       [[3 4]
        [3 -3]]]
   :r [[[0 4]
        [0 0]]
       [[0 3]
        [1 4]
        [2 4]
        [3 3]]]
   :s [[[3 3]
        [2 4]
        [1 4]
        [0 3]
        [1 2]
        [2 2]
        [3 1]
        [2 0]
        [1 0]
        [0 1]]]
   :t [[[1 5]
        [1 1]
        [2 0]
        [3 1]]
       [[0 4]
        [3 4]]]
   :u [[[0 4]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]
       [[3 4]
        [3 0]]]
   :v [[[0 4]
        [0 1]
        [1 0]
        [3 2]
        [3 4]]]
   :w [[[0 4]
        [0 0]
        [1 0]
        [1 4]
        [1 0]
        [2 0]
        [3 1]
        [3 4]]]
   :x [[[0 4]
        [0 3]
        [1 2]
        [2 2]
        [3 1]
        [3 0]]
       [[3 4]
        [3 3]
        [2 2]
        [1 2]
        [0 1]
        [0 0]]]
   :y [[[0 4]
        [0 1]
        [1 0]
        [2 0]
        [3 1]]
       [[3 4]
        [3 -2]
        [2 -3]
        [1 -3]
        [0 -2]]]
   :z [[[0 4]
        [3 4]
        [0 1]
        [0 0]
        [3 0]]]
   :A [[[0 0]
        [0 6]
        [1 7]
        [2 7]
        [3 6]
        [3 0]]
       [[0 4]
        [3 4]]]
   :B [[[0 0]
        [0 7]
        [2 7]
        [3 6]
        [3 5]
        [2 4]
        [0 4]
        [2 4]
        [3 3]
        [3 1]
        [2 0]
        [0 0]]]
   :C [[[3 5]
        [3 6]
        [2 7]
        [1 7]
        [0 6]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 2]]]
   :D [[[0 7]
        [0 0]]
       [[0 7]
        [2 7]
        [3 6]
        [3 1]
        [2 0]
        [0 0]]]
   :E [[[0 7]
        [0 0]]
       [[3 7]
        [0 7]]
       [[2 4]
        [0 4]]
       [[3 0]
        [0 0]]]
   :F [[[0 7]
        [0 0]]
       [[3 7]
        [0 7]]
       [[2 4]
        [0 4]]]
   :G [[[3 6]
        [2 7]
        [1 7]
        [0 6]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 4]]
       [[2 4]
        [3 4]]]
   :H [[[0 7]
        [0 0]]
       [[3 7]
        [3 0]]
       [[3 4]
        [0 4]]]
   :I [[[2 7]
        [2 0]]
       [[3 7]
        [0 7]]
       [[3 0]
        [0 0]]]
   :J [[[2 7]
        [2 1]
        [1 0]
        [0 0]]
       [[3 7]
        [0 7]]]
   :K [[[0 7]
        [0 0]]
       [[3 7]
        [0 4]
        [3 1]
        [3 0]]]
   :L [[[0 7]
        [0 0]
        [3 0]]]
   :M [[[0 0]
        [0 7]
        [1 7]
        [1 4]
        [1 7]
        [3 7]
        [3 0]]]
   :N [[[0 7]
        [0 0]]
       [[0 7]
        [1 7]
        [1 0]
        [3 0]]
       [[3 7]
        [3 0]]]
   :O [[[3 6]
        [2 7]
        [1 7]
        [0 6]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 6]]]
   :P [[[0 7]
        [0 0]]
       [[0 7]
        [2 7]
        [3 6]
        [3 5]
        [2 4]
        [0 4]]]
   :Q [[[3 6]
        [2 7]
        [1 7]
        [0 6]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 6]]
       [[1 1]
        [2 0]
        [3 0]]]
   :R [[[0 7]
        [0 0]]
       [[0 7]
        [2 7]
        [3 6]
        [3 5]
        [2 4]
        [0 4]
        [2 4]
        [3 3]
        [3 0]]]
   :S [[[3 7]
        [1 7]
        [0 6]
        [0 5]
        [1 4]
        [2 4]
        [3 3]
        [3 1]
        [2 0]
        [0 0]]]
   :T [[[1 7]
        [1 0]]
       [[3 7]
        [0 7]]]
   :U [[[0 7]
        [0 1]
        [1 0]
        [2 0]
        [3 1]
        [3 7]]]
   :V [[[0 7]
        [0 1]
        [1 0]
        [3 2]
        [3 7]]]
   :W [[[0 7]
        [0 0]
        [1 0]
        [1 4]
        [1 0]
        [2 0]
        [3 1]
        [3 7]]]
   :X [[[0 7]
        [0 5]
        [1 4]
        [2 4]
        [3 3]
        [3 0]]
       [[3 7]
        [3 5]
        [2 4]
        [1 4]
        [0 3]
        [0 0]]]
   :Y [[[0 7]
        [0 6]
        [2 4]
        [3 5]
        [3 7]
        [3 5]
        [2 4]
        [2 0]]]
   :Z [[[0 7]
        [3 7]
        [3 6]
        [2 5]
        [2 4]
        [0 2]
        [0 0]
        [3 0]]]})

(defn render-character
  "Render font path data into strokes."
  [character [xpos ypos] scale]
  {:strokes
   (mapv (fn [stroke]
           {:segments (mapv (fn [[x y]] {:xpos (+ xpos (* x scale))
                                         :ypos (+ ypos (* y scale -1))}) stroke)})
         character)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn help
  "Library help"
  []
  (println "  ;; Testing functions")
  (println "  (resvg/write-test)"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def test-pagedata
  {:layers
   [{:strokes
     (concat []
             [{:segments [{:xpos 100 :ypos 100}
                          {:xpos 200 :ypos 100}]}]
             (:strokes (render-character (font :V) [(+ 100 (* 40 0)) 200] 10))
             (:strokes (render-character (font :e) [(+ 100 (* 40 1)) 200] 10))
             (:strokes (render-character (font :c) [(+ 100 (* 40 2)) 200] 10))
             (:strokes (render-character (font :t) [(+ 100 (* 40 3)) 200] 10))
             (:strokes (render-character (font :o) [(+ 100 (* 40 4)) 200] 10))
             (:strokes (render-character (font :r) [(+ 100 (* 40 5)) 200] 10))
             (:strokes (render-character (font :F) [(+ 100 (* 40 7)) 200] 10))
             (:strokes (render-character (font :o) [(+ 100 (* 40 8)) 200] 10))
             (:strokes (render-character (font :n) [(+ 100 (* 40 9)) 200] 10))
             (:strokes (render-character (font :t) [(+ 100 (* 40 10)) 200] 10))
             (:strokes (render-character (font :E) [(+ 100 (* 40 12)) 200] 10))
             (:strokes (render-character (font :x) [(+ 100 (* 40 13)) 200] 10))
             (:strokes (render-character (font :a) [(+ 100 (* 40 14)) 200] 10))
             (:strokes (render-character (font :m) [(+ 100 (* 40 15)) 200] 10))
             (:strokes (render-character (font :p) [(+ 100 (* 40 16)) 200] 10))
             (:strokes (render-character (font :l) [(+ 100 (* 40 17)) 200] 10))
             (:strokes (render-character (font :e) [(+ 100 (* 40 18)) 200] 10))
             ;;
             (:strokes (render-character (font :A) [100 500] 10))
             (:strokes (render-character (font :B) [140 500] 10))
             (:strokes (render-character (font :C) [180 500] 10))
             (:strokes (render-character (font :D) [220 500] 10))
             (:strokes (render-character (font :E) [260 500] 10))
             (:strokes (render-character (font :F) [300 500] 10))
             (:strokes (render-character (font :G) [340 500] 10))
             (:strokes (render-character (font :H) [380 500] 10))
             (:strokes (render-character (font :I) [420 500] 10))
             (:strokes (render-character (font :J) [460 500] 10))
             (:strokes (render-character (font :K) [500 500] 10))
             (:strokes (render-character (font :L) [540 500] 10))
             (:strokes (render-character (font :M) [580 500] 10))
             (:strokes (render-character (font :N) [620 500] 10))
             (:strokes (render-character (font :O) [660 500] 10))
             (:strokes (render-character (font :P) [700 500] 10))
             (:strokes (render-character (font :Q) [740 500] 10))
             (:strokes (render-character (font :R) [780 500] 10))
             (:strokes (render-character (font :S) [820 500] 10))
             (:strokes (render-character (font :T) [860 500] 10))
             (:strokes (render-character (font :U) [900 500] 10))
             (:strokes (render-character (font :V) [940 500] 10))
             (:strokes (render-character (font :W) [980 500] 10))
             (:strokes (render-character (font :X) [1020 500] 10))
             (:strokes (render-character (font :Y) [1060 500] 10))
             (:strokes (render-character (font :Z) [1100 500] 10))
             ;;
             (:strokes (render-character (font :a) [100 620] 10))
             (:strokes (render-character (font :b) [140 620] 10))
             (:strokes (render-character (font :c) [180 620] 10))
             (:strokes (render-character (font :d) [220 620] 10))
             (:strokes (render-character (font :e) [260 620] 10))
             (:strokes (render-character (font :f) [300 620] 10))
             (:strokes (render-character (font :g) [340 620] 10))
             (:strokes (render-character (font :h) [380 620] 10))
             (:strokes (render-character (font :i) [420 620] 10))
             (:strokes (render-character (font :j) [460 620] 10))
             (:strokes (render-character (font :k) [500 620] 10))
             (:strokes (render-character (font :l) [540 620] 10))
             (:strokes (render-character (font :m) [580 620] 10))
             (:strokes (render-character (font :n) [620 620] 10))
             (:strokes (render-character (font :o) [660 620] 10))
             (:strokes (render-character (font :p) [700 620] 10))
             (:strokes (render-character (font :q) [740 620] 10))
             (:strokes (render-character (font :r) [780 620] 10))
             (:strokes (render-character (font :s) [820 620] 10))
             (:strokes (render-character (font :t) [860 620] 10))
             (:strokes (render-character (font :u) [900 620] 10))
             (:strokes (render-character (font :v) [940 620] 10))
             (:strokes (render-character (font :w) [980 620] 10))
             (:strokes (render-character (font :x) [1020 620] 10))
             (:strokes (render-character (font :y) [1060 620] 10))
             (:strokes (render-character (font :z) [1100 620] 10))
             ;; end of strokes
             )}]})

(defn write-test []
  (pp/pprint test-pagedata)
  (write :pagedata test-pagedata))

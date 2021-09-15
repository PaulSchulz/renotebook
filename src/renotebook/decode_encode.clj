;; Decode/Encode Remarkable Notebook  File format
(ns renotebook.decode-encode
  ;; Parsing binary data
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.pprint :as pp]
   [org.clojars.smee.binary.core :as b])
  (:require
   [clojure.string :as str])
  ;; Testing
  (:require [clojure.test :refer :all])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; File formats
;; .metadata - JSON Format
;; .content  - JSON Format
;; .pagedata - Line format - List of templates used on each page

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn notebook-metadata?
  "Check that notebook metadata exists "
  [path id]
  (let [filename (str path id ".metadata")]
    (println "Filenanme: " filename)
    (if (.exists (io/as-file filename))
      true
      false)))

(defn notebook-content?
  "Check that notebook content file exists "
  [path id]
  (let [filename (str path id ".content")]
    (println "Filenanme: " filename)
    (if (.exists (io/as-file filename))
      true
      false)))

(defn notebook-pagedata?
  "Check that notebook pagedata file exists "
  [path id]
  (let [filename (str path id ".pagedata")]
    (println "Filenanme: " filename)
    (if (.exists (io/as-file filename))
      true
      false)))

(defn notebook-directory?
  "Check that notebook directory exists "
  [path id]
  (let [filename (str path id)]
    (println "Filenanme: " filename)
    (if (.exists (io/as-file filename))
      true
      false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; reMarkable notebook page file - .lines

(def header-string "reMarkable .lines file, version=5          ")
(defn fixed-string [len]
  (b/string header-string :length len))

;; Codec Details
;; https://plasma.ninja/blog/devices/remarkable/binary/format/2017/12/26/reMarkable-lines-file-format.html
;; https://remarkablewiki.com/tech/filesystem#lines_file_format

;; TODO: Write test- require a simple notebook file to test this codec

(def codec-segment
  (b/ordered-map
   :xpos     :float-le
   :ypos     :float-le
   :pressure :float-le
   :tilt     :float-le
   :speed    :float-le
   :u        :float-le))

(def codec-stroke
  (b/ordered-map
   :pen      :uint-le
   :color    :uint-le
   :unknown  :uint-le
   :width    :float-le
   :unknown2 :uint-le
   :segments (b/repeated codec-segment :prefix :uint-le)))

(def codec-layer
  (b/ordered-map :strokes (b/repeated codec-stroke :prefix :uint-le)))

(def codec-notebook
  (b/ordered-map
   :version (b/string "ISO-8859-1" :length 43)
   :layers  (b/repeated codec-layer :prefix :uint-le)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn rm-decode [path id]
  "Read notebook file data, decode and return in a data structure."
  (println "Path: " path  "Id: " id)
  {})

(defn rm-encode [data path id]
  "Take data structure with notebook data and save to notebook file."
  (println "Path: " path  "Id: " id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn encode-notebook-data [data filename]
  (let [out (io/output-stream filename)]
    (b/encode codec-notebook out data)))

;; Read notebook page data
(defn decode-notebook-data [filename]
  (let [in (io/input-stream filename)
        data (b/decode codec-notebook in)]
    data))

(defn decode-notebook-page [path notebook page]
  (let [datafile (str path notebook "/" page ".rm")
        metafile (str path notebook "/" page "-metadata.json")
        d (.exists (io/as-file datafile))
        m (.exists (io/as-file metafile))]
    ;; Check if metadata for page exists
    (println "Page" page)
    (if m
      (let [metadata (json/read-str (slurp metafile))]
        (print "  Metadata: ")
        (pp/pprint metadata))
      (println "  *** metadata does NOT exist"))
    (if (not d)
      (let []
        (println "  *** no data file")
        [])
      (decode-notebook-data datafile))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def header-string "reMarkable .lines file, version=5          ")
(defn fixed-string [len]
  (b/string header-string :length len))

;; Codec Details
;; https://plasma.ninja/blog/devices/remarkable/binary/format/2017/12/26/reMarkable-lines-file-format.html
;; https://remarkablewiki.com/tech/filesystem#lines_file_format

;; TODO: Write test- require a simple notebook file to test this codec

(def codec-segment
  (b/ordered-map
   :xpos     :float-le
   :ypos     :float-le
   :pressure :float-le
   :tilt     :float-le
   :speed    :float-le
   :u        :float-le))

(def codec-stroke
  (b/ordered-map
   :pen      :uint-le
   :color    :uint-le
   :unknown  :uint-le
   :width    :float-le
   :unknown2 :uint-le
   :segments (b/repeated codec-segment :prefix :uint-le)))

(def codec-layer
  (b/ordered-map :strokes (b/repeated codec-stroke :prefix :uint-le)))

(def codec-notebook
  (b/ordered-map
   :version (b/string "ISO-8859-1" :length 43)
   :layers  (b/repeated codec-layer :prefix :uint-le)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def color-lookup
  {0 "Black"
   1 "Gray"
   2 "White"})

(def width-lookup
  {1.875 "Thin"
   2.0   "Medium"
   2.125 "Thick"})

(def pen-lookup
  {;; 0x0  "Fineliner"
   ;; 0x0  "Mechanical pencil"
   ;; 0x0  "Highlighter"
   ;; 0x0  "Calligraphy pen"
   0x0C "Paintbrush"
   0x0E "Pencil"
   0x0F "Ballpoint pen"
   0x10 "Marker"})
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn format-segment [segment]
  (format "  %10.6f  %10.6f  |  %10.6f %10.6f %10.6f %10.6f "
          (segment :xpos)
          (segment :ypos)
          (segment :pressure)
          (segment :tilt)
          (segment :speed)
          (segment :u)))

(defn format-stroke [stroke]
  (format "-- %d %d %10.6f %d : %s %s %s"
          (stroke :pen)
          (stroke :color)
          (stroke :width)
          (stroke :segments)
          (pen-lookup (stroke :pen))
          (color-lookup (stroke :color))
          (width-lookup (stroke :width))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn encode-notebook-data [data filename]
  (let [out (io/output-stream filename)]
    (b/encode codec-notebook out data)))

;; Read notebook page data
(defn decode-notebook-data [filename]
  (let [in (io/input-stream filename)
        data (b/decode codec-notebook in)]
    data))

(defn decode-notebook-page [dir-notebooks notebook page]
  (let [datafile (str dir-notebooks notebook "/" page ".rm")
        metafile (str dir-notebooks notebook "/" page "-metadata.json")
        d (.exists (io/as-file datafile))
        m (.exists (io/as-file metafile))]
    ;; Check if metadata for page exists
    (println "Page" page)
    (if m
      (let [metadata (json/read-str (slurp metafile))]
        (print "  Metadata: ")
        (pp/pprint metadata))
      (println "  *** metadata does NOT exist"))
    (if (not d)
      (let []
        (println "  *** no data file")
        [])
      (decode-notebook-data datafile))))

(defn decode-notebook [dir-notebooks notebook]
  (let [metadata (json/read-str (slurp (str dir-notebooks notebook ".metadata")))
        content  (json/read-str (slurp (str dir-notebooks notebook ".content")))
        pages    (content "pages")
        pagedata (str/split (slurp (str dir-notebooks notebook ".pagedata")) #"\n")]

    (println)
    (println "Metadata")
    (pp/pprint metadata)
    (println)

    (println "Content")
    (pp/pprint content)
    (println)

    (println "Pagedata")
    (pp/pprint pagedata)
    (println)

    (println "Pages")
    (dorun (map (fn [p] (println "  " p)) pages))
    ;; (pp/pprint pages)
    (println)

    (dorun (map (fn [p] (decode-notebook-page notebook p)) pages))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;
;; To run tests in this file:
;;   (ns renotebook.decode-encode)
;;   (use 'renotebook.decode-encode :reload-all)
;;   (run-tests)

;; Test data

;; Tests
(deftest notebook-metatdata-test
  (let [dir-notebooks  "resources/notebooks/"
        selected-notebook "7cb44c92-78c7-449e-9db4-936663596e74"]
    (testing "Check for metadata"
      (is (notebook-metadata? dir-notebooks selected-notebook)))
    (testing "Check for metadata (negative)"
      (is (not (notebook-metadata? dir-notebooks (str selected-notebook "x")))))))

(deftest notebook-content-test
  (let [dir-notebooks  "resources/notebooks/"
        selected-notebook "7cb44c92-78c7-449e-9db4-936663596e74"]
    (testing "Check for content"
      (is (notebook-content? dir-notebooks selected-notebook)))
    (testing "Check for content (negative)"
      (is (not (notebook-content? dir-notebooks (str selected-notebook "x")))))))

;; (deftest decode-test
;;  (testing "Decode (and load) a reMarkable notebook"
;;    (is (= (rm-decode "" "") {}))))
(def notebooks-test
  {:test
   {:notebook "7cb44c92-78c7-449e-9db4-936663596e74"
    :page     "642a631f-fed7-49c8-a43e-69790345ea20"}
   :diagram
   {:notebook "4d37af63-e017-4fed-a06e-1fa37e62bbc2"
    :page     "5fac703c-9701-4df3-a00b-f666ae014453"}})

(def notebook (notebooks-test :diagram))

;; Page data
(defn reload []
  (println ";; Reload namespace")
  (use 'renotebook.decode-encode :reload-all))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn decode-notebook-data-dev []
  (let [dir-notebooks  "resources/notebooks/"
        notebook       "4d37af63-e017-4fed-a06e-1fa37e62bbc2"
        page           "5fac703c-9701-4df3-a00b-f666ae014453"
        datafile       (str dir-notebooks notebook "/" page ".rm")]
    (decode-notebook-data datafile)))

(defn get-strokes []
  (-> (decode-notebook-data-dev)
      :layers
      first
      :strokes))

(defn get-stroke []
  (-> (decode-notebook-data-dev)
      :layers
      first
      :strokes
      first
      :segments))

;; Convert stroke (segments) to path points
(defn convert-stroke-to-path [stroke]
  (mapv (fn [point] [(:xpos point) (:ypos point)]) stroke))

;;  ?/?
(defn convert-strokes [strokes]
  (mapv (fn [stroke]
          (convert-stroke-to-path (:segments stroke)))
        strokes))

;; Load Data
;;(def notebook-data (decode-notebook-data-dev))

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

(defn format-style [style]
  (str/join
   ";"
   (map (fn [[key value]] (str (name key) ":" value)) style)))

(def line
  [[68.282847 104.995078]
   [119.69633 52.53259]
   [154.84839,116.83706]])

;; Convert array or point to string.
(defn format-line [line]
  (str "M "
       (str/join " "
                 (map (fn [[x y]] (str x "," y)) line))))

;; Convert string of points to array.
(defn decode-line [string]
  (mapv (fn [point] (str/split point #","))
        (drop 1 (str/split string #" "))))

(defn path-string [line style]
  (str "<path style=\"" (format-style style) "\" "
       "d=\"" (format-line line) "\" />"))

(defn output-svg-dev []
  (let [filename "resources/drawing2.svg"
        header   "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>
<svg
   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"
        xmlns:cc=\"http://creativecommons.org/ns#\"
   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"
   xmlns:svg=\"http://www.w3.org/2000/svg\"
   xmlns=\"http://www.w3.org/2000/svg\"
   width=\"1404px\"
   height=\"1872px\"
   viewBox=\"0 0 1404 1872\"
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
"
        footer " </g>
</svg>
"
        content (apply str
                       (mapv path-string
                             (convert-strokes (get-strokes))
                             (repeat style)))]

    (spit filename (str header content footer))))

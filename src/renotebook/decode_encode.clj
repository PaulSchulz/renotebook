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
(def dir-notebooks     "resources/notebooks/")
(def selected-notebook "7cb44c92-78c7-449e-9db4-936663596e74")

;; Tests
(deftest notebook-metatdata-test
  (testing "Check for metadata"
    (is (notebook-metadata? dir-notebooks selected-notebook)))
  (testing "Check for metadata (negative)"
    (is (not (notebook-metadata? dir-notebooks (str selected-notebook "x"))))))

(deftest notebook-content-test
  (testing "Check for content"
    (is (notebook-content? dir-notebooks selected-notebook)))
  (testing "Check for content (negative)"
    (is (not (notebook-content? dir-notebooks (str selected-notebook "x"))))))

                                        ;(deftest decode-test
                                        ;  (testing "Decode (and load) a reMarkable notebook"
                                        ;    (is (= (rm-decode "" "") {}))))

;; Decode/Encode Remarkable Notebook  File format

(ns renotebook.decode-encode
  ;; Parsing binary data
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.pprint :as pp]
   [org.clojars.smee.binary.core :as b])
  (:gen-class))

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
(defn decode [path id]
  "Read notebook file data, decode and return in a data structure."
  (println "Path: " path  "Id: " id)
  {})

(defn eecode [data path id]
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

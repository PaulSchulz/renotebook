(ns renotebook.core
  (:require [clojure.data.json :as json]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.java.io :as io]
            ;; Local shell commands
            [clojure.java.shell :as sh]
            ;; Remote commands on Re2
            [clj-ssh.cli :as cli]
            [clj-ssh.ssh :as ssh]
            )
  ;; Creation of PDF
  (:require [clj-pdf.core :as pdf])
  ;; Parsing binary data
  (:require [org.clojars.smee.binary.core :as b]
            [clojure.java.io :as io]
            [clojure.core.match :as m])
  ;; WIP  (:require [renotebook.hershey :as h])
  (:import org.clojars.smee.binary.core.BinaryIO
           java.io.DataInput)
  ;; Used to hold local preferences/configuration
  (:import (java.util.prefs Preferences))
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Developer Preferences are read from the
(use 'renotebook.secrets)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; User Preferences
(def pref-node (.node (Preferences/userRoot) "renotebook"))

;; API
;; (.get pref-node "remarkable" nil)
;; (.put pref-node "password" password)

;; Default user is 'root'
;; Assumes that SSH keys have been configured to allow passwordless access.

(defn list-prefs []
  (map (fn [key]
         (print key " ")
         (println (.get pref-node key nil)))
       (.keys pref-node))
  )

(def debug true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REPL Commands
(defn help []
  (println "Useful commands:")
  (println "  (help)")
  (println "  (reload)")
  (println "  (re-ssh string)")
  (println "  (list-prefs)")
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utilities / Scriptlets
(defn cmd-download []
  [["scp" "-r" re-notebooks dir-notebooks ]])

(defn cmd-rsync []
"Implemented as a function which can create an updated string at runtime."
[["rsync"
  "-r" "--rsync-path=/opt/bin/rsync"
  (str "root@" remarkable ":" re2-notebooks )
  dir-notebooks]]
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;; Testing how tagged arguments are handles
(defn re-test [string {:keys [username] :as options}]
  (println string)
  (println username)
  (pp/pprint options)
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Shell Commands
(defn sh-rsync []
(let [cmd ["rsync"
           "-r"
           "--rsync-path=/opt/bin/rsync"
           (str "root@" remarkable ":/home/root/" re2-notebooks)
           dir-notebooks]
      ]
  (if debug
    (let []
      (println "Command")
      (pp/pprint cmd)
      ))
  (pp/pprint (apply sh/sh cmd))
  )
)

(defn sh-git-commit []
(let [dir dir-notebooks
      cmd ["sh" "-c"
           (str/join " "
                     ["cd"
                      dir-notebooks
                      ";"
                      "git"
                      "commit"
                      "-a"
                      "-m"
                      "Backup"])
           ]]
  (pp/pprint cmd)
  (pp/pprint (apply sh/sh cmd))
  )
)

;; SSH Command Tunnel;
(defn re-ssh [cmd]
(let [agent (ssh/ssh-agent {})]
  (println "cmd: " cmd)
  (let [session (ssh/session agent remarkable
                             {:username "root" :strict-host-key-checking :no})]
    (ssh/with-connection session
      (println (:out (ssh/ssh session {:cmd cmd})))
      )
    )))

(defn re-ssh-tunnel [cmds]
"Takes an array of bash commands, stored as an array."
(let [agent (ssh/ssh-agent {})]
  (let [session (ssh/session agent remarkable
                             {:username "root" :strict-host-key-checking :no})]
    (ssh/with-connection session
      (map (fn [cmd]
             (let [cmd-str (str/join " " cmd)
                   result (ssh/ssh session {:cmd cmd-str})]
               (println cmd)
               ))
           cmds)
      )))
)

(defn re-restart-xochitl []
(re-ssh "systemctl restart xochitl"))

(defn re-grep-metadata [string]
(re-ssh (str/join " " ["cd .local/share/remarkable/xochitl;" "grep" "-r" string "."]))
  )
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PDF Generation
;; - anchors don't appear to work in remarkable, but do work in evince.
(defn pdf-create []
  (pdf/pdf
   [{:header "A PDF Document"
     :size   :a4
     :footer {:text "A PDF Document"}
     :font   {:size 11 :family :helvetica}
     } ;; Metadata
    [:paragraph
     [:anchor {:id "year"} "2022"] "/"
     [:anchor {:target "#jan"} "January"] "/"
     [:anchor {:target "#feb"} "February"] "/"
     [:anchor {:target "#mar"} "March"] "/"
     [:anchor {:target "#apr"} "April"] "/"
     ]
    [:clear-double-page]
    [:paragraph
     [:anchor {:target "#year"} "2022"] "/"
     [:anchor {:id "jan" :style :bold} "January"] "/"
     [:anchor {:target "#feb"} "February"] "/"
     [:anchor {:target "#mar"} "March"] "/"
     [:anchor {:target "#apr"} "April"] "/"
     ]
    [:clear-double-page]
    [:anchor {:id "feb"} [:heading "February"]]
    [:clear-double-page]
    [:anchor {:id "mar"} [:heading "March"]]
    [:clear-double-page]
    [:anchor {:id "apr"} [:heading "April"]]
    [:clear-double-page]
    [:anchor {:id "may"} [:heading "May"]]
    ]
   "doc.pdf")
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn reload []
  (use 'renotebook.core :reload-all)
  (println ";; namespace reloaded")
  )

;; CLI Commands for 'getting things done'
(def build-notebook
  [["cd" dir-notebooks]
   ["tar" "cf" (str "../" notebook ".rmn") (str notebook "*")]])

(def copy-notebook-from-re2
  [["cd" dir-notebooks]
   ["scp" "-r"
    (str "root@" remarkable ":/home/root/.local/share/remarkable/xochitl/" notebook "*")
    "."]
   ])

(def copy-notebook-to-re2
  [["cd" dir-notebooks]
   ["scp" "-r"
    (str "root@" remarkable ":/home/root/.local/share/remarkable/xochitl/" notebook "*")
    "."]
   ])

;; If notebook is downloaded via RCU then in is in a 'tar' archive.
(defn extract-notebook [filename]
  [["cd" dir-notebooks]
   ["tar" "xf" filename]])

(defn display-shell [cmds]
  (dorun (map (fn [cmd] (println (str "  " (str/join " " cmd)))) cmds))
  )

(defn status []
  (println "---")
  (println "Notebook: " notebook)
  (println "---")
  (println "To build:")
  (display-shell build-notebook)
  (println "")
  (println "To extract:")
  (display-shell (extract-notebook "$FILENAME"))
  )

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
   :u        :float-le
   ))

(def codec-stroke
  (b/ordered-map
   :pen      :uint-le
   :color    :uint-le
   :unknown  :uint-le
   :width    :float-le
   :unknown2 :uint-le
   :segments (b/repeated codec-segment :prefix :uint-le)
   )
  )

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
  {
   ;; 0x0  "Fineliner"
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
          (segment :u))
  )

(defn format-stroke [stroke]
  (format "-- %d %d %10.6f %d : %s %s %s"
          (stroke :pen)
          (stroke :color)
          (stroke :width)
          (stroke :segments)
          (pen-lookup (stroke :pen))
          (color-lookup (stroke :color))
          (width-lookup (stroke :width))
          ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn encode-notebook-data [data filename]
  (let [out (io/output-stream filename)]
    (b/encode codec-notebook out data)
    )
  )

;; Read notebook page data
(defn decode-notebook-data [filename]
  (let [in (io/input-stream filename)
        data (b/decode codec-notebook in)]
    data
    ))

(defn decode-notebook-page [notebook page]
  (let [datafile (str dir-notebooks notebook "/" page ".rm")
        metafile (str dir-notebooks notebook "/" page "-metadata.json")
        d (.exists (io/as-file datafile))
        m (.exists (io/as-file metafile))
        ]
    ;; Check if metadata for page exists
    (println "Page" page)
    (if m
      (let [metadata (json/read-str (slurp metafile))]
        (print "  Metadata: ")
        (pp/pprint metadata))
      (println "  *** metadata does NOT exist")
      )
    (if (not d)
      (let []
        (println "  *** no data file")
        [])
      (decode-notebook-data datafile)
      )
    ))

;; File formats
;; .metadata - JSON Format
;; .content  - JSON Format
;; .pagedata - Line format - List of template used on each page
(defn decode-notebook [notebook]
  (let [metadata (json/read-str (slurp (str dir-notebooks notebook ".metadata" )))
        content  (json/read-str (slurp (str dir-notebooks notebook ".content" )))
        pages    (content "pages")
        pagedata (str/split (slurp (str dir-notebooks notebook ".pagedata" )) #"\n")
        ]

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

    (dorun (map (fn [p] (decode-notebook-page notebook p)) pages))
    ))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; (help)
  (decode-notebook notebook)

  )

(ns renotebook.git
  (:require
   [clojure.java.shell :as sh]
   [clojure.pprint :as pp])
  (:gen-class))

(def debug true)

(defn rsync [remarkable user re-notebooks dir-notebooks]
  "Use rsync to replicate notebooks from Remarkable tablet"
  (let [cmd ["rsync"
             "--info=STATS" "--human-readable"
             "-r"
             "--rsync-path=/opt/bin/rsync"
             (str user "@" remarkable ":" re-notebooks)
             dir-notebooks]]
    (if debug
      (let []
        (println "Command")
        (pp/pprint cmd)))
    (pp/pprint (apply sh/sh cmd))))

(defn git-status [dir-notebooks]
  "Display git status on current notebook repository"
  (println "TODO: Put something here."))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Use and test
;; To use
;;  (ns renotebook.git
;;  (use 'renotebook.git :reload-all)

(use 'renotebook.secrets)

(defn rsync-run []
  (rsync remarkable user re-notebooks dir-notebooks))

(defn status []
  (let [settings
        {:remarkable    remarkable
         :user          user
         :re-notebooks  re-notebooks
         :dir-notebooks dir-notebooks}]
    (pp/pprint settings)))

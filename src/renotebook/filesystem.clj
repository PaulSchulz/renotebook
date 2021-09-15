(ns renotebook.filesystem
  (:require
   [clojure.data.json :as json]
   [clojure.java.shell :as sh]
   [clojure.pprint :as pp]
   [clojure.string :as str])
  (:gen-class))

;; Size of Notebook storage
(defn du
  [dir]
  (let [cmd ["du"
             "-sh"
             dir]]
    (apply sh/sh cmd)))

;; Retrieve reMarkable metadata
(defn metadata [dir file]
  (json/read-str (slurp (str/join [dir file ".metadata"]))))

;; Content of reMarkable folder
(defn ls
  [dir folder]
  (let [cmd ["ls"
             dir]]
    (pp/pprint
     (filter (fn [x] (= (:parent x) folder))
             (map (fn [x] {:notebook x
                           :parent (get (metadata dir x) "parent")})
                  (map (fn [x] (re-find #"^[^.]*" x))
                       (filter
                        (fn [x] (re-matches #"^.*\.metadata$" x))
                        (str/split (:out (apply sh/sh cmd)) #"\n"))))))
    ;;(apply sh/sh cmd)
    ))

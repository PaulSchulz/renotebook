;; renotebook.options.clj
;;
;; Customizable options in renotebook

(ns renotebook.options
  (:gen-class))

(def user          "root")
(def re-notebooks  "/home/root/.local/share/remarkable/xochitl/")
(def dir-notebooks "~/Documents/remarkable/")

;; Default Notebook
(def quick-sheets      "b7b1827c-28a5-4f29-a6b8-9ce7fe523270")
(def selected-notebook quick-sheets)

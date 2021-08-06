;; This file should never be committed
;; In Git, to allow this file to be ignored during commits, add the following
;; to .git/config,
;;
;;   [alias]
;;   ignore = update-index --assume-unchanged
;;   unignore = update-index --no-assume-unchanged
;;   ignored = !git ls-files -v | grep "^[[:lower:]]"
;;
;; Use the following to mark 'secret.clj' as ignored.
;;
;;   git ignore secret.clj

(ns renotebook.secrets
  (:gen-class) )

(def remarkable    "")
(def user          "")
(def password      "")
(def re-notebooks  ".local/share/remarkable/xochitl/")
(def dir-notebooks "resources/notebooks/")

;; Default Notebook
(def quick-sheets      "b7b1827c-28a5-4f29-a6b8-9ce7fe523270")
(def selected-notebook quick-sheets)

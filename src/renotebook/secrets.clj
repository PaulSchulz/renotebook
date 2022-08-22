;; renotebook.secrets.clj
;;
;; This file should never be committed with details for
;; 'remarkable' (IP Address) or 'password'
;; In Git, to allow this file to be ignored during commits, add the following
;; to .git/config,
;;
;;   [alias]
;;   ignore = update-index --assume-unchanged
;;   unignore = update-index --no-assume-unchanged
;;   ignored = !git ls-files -v | grep "^[[:lower:]]"
;;
;; Use the following to mark 'secrets.clj' as ignored.
;;
;;   git ignore src/renotebook/secrets.clj

(ns renotebook.secrets
  (:gen-class))

(def remarkable    "")
(def password      "")

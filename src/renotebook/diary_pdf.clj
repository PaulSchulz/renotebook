;; Produce a Diary in PDF Format for use on the reMarkable
(ns renotebook.diary-pdf
  (:require
   [clj-pdf.core :as pdf])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PDF Generation
;; - anchors don't appear to work in remarkable,
;;   but do work in evince (Ubuntu PDF System Viewer).
(def year 2022)

(defn pdf-create []
  (pdf/pdf
   [{:header year
     :size   :a4
     :footer {:text "A PDF Document"}
     :font   {:size 11 :family :helvetica}} ;; Metadata
    [:paragraph
     [:anchor {:id "year"} "2022"] "/"
     [:anchor {:target "#jan"} "January"] "/"
     [:anchor {:target "#feb"} "February"] "/"
     [:anchor {:target "#mar"} "March"] "/"
     [:anchor {:target "#apr"} "April"] "/"]
    [:clear-double-page]
    [:paragraph
     [:anchor {:target "#year"} "2022"] "/"
     [:anchor {:id "jan" :style :bold} "January"] "/"
     [:anchor {:target "#feb"} "February"] "/"
     [:anchor {:target "#mar"} "March"] "/"
     [:anchor {:target "#apr"} "April"] "/"]
    [:clear-double-page]
    [:anchor {:id "feb"} [:heading "February"]]
    [:clear-double-page]
    [:anchor {:id "mar"} [:heading "March"]]
    [:clear-double-page]
    [:anchor {:id "apr"} [:heading "April"]]
    [:clear-double-page]
    [:anchor {:id "may"} [:heading "May"]]]
   "doc.pdf"))

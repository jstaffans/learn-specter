(ns learn-specter.dev
  (:require [devtools.core :as devtools]))

;; note: unsure how to include this in boot build process

(comment
  (require '[devtools.core :as devtools])
  (devtools/enable-feature! :sanity-hints :dirac)
  (devtools/install!)
  )

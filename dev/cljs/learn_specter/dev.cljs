(ns learn-specter.dev
  (:require [devtools.core :as devtools]))

;; note: unsure how to include this in boot build process

(devtools/enable-feature! :sanity-hints :dirac)
(devtools/install!)

(ns com.github.lxbr.decoder.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::type (s/or :primitive #{:byte  :ubyte
                                 :int16 :uint16
                                 :int32 :uint32}
                    :custom keyword?))

(s/def ::count (s/or :number   nat-int?
                     :keyword  keyword?
                     :function fn?))

(s/def ::as keyword?)

(s/def ::f keyword?)

(s/def ::parse-step
  (s/keys :req-un [::type] :opt-un [::count ::as ::f]))

(s/def ::frame (s/coll-of ::parse-step :kind vector?))

(s/def ::frames (s/map-of keyword? ::frame))

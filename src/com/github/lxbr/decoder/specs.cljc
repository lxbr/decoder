(ns com.github.lxbr.decoder.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::type (s/or :primitive #{:int8  :uint8
                                 :int16 :uint16
                                 :int32 :uint32
                                 :int64 :uint64}
                    :custom keyword?))

(s/def ::count (s/or :number   nat-int?
                     :keyword  keyword?
                     :vector   (s/coll-of keyword? :kind vector?)
                     :function fn?))

(s/def ::as keyword?)

(s/def ::f keyword?)

(s/def ::parse-step
  (s/keys :req-un [::type] :opt-un [::count ::as ::f]))

(s/def ::frame (s/coll-of ::parse-step :kind vector?))

(s/def ::frames (s/map-of keyword? ::frame))

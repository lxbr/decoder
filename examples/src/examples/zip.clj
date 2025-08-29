(ns examples.zip
  (:require [com.github.lxbr.decoder :as dc]
            [com.github.lxbr.decoder.platform.jvm.byte-buffer :as bb]
            [clojure.java.io :as io])
  (:import java.io.ByteArrayOutputStream))

(def buffer
  (let [baos (ByteArrayOutputStream.)]
    (-> "https://repo1.maven.org/maven2/org/clojure/clojure/1.10.1/clojure-1.10.1-sources.jar"
        (io/input-stream)
        (io/copy baos))
    (bb/wrap-bytes (.toByteArray baos) :le)))

(def xforms
  {:string (fn [^bytes bytes] (String. bytes))})

(def frames
  {:zip/file
   [

    {:type :zip/local-file-header
     :as   :header}

    {:type  :uint8
     :count [:header :compressed-size]
     :as    :data}

    ;; data descriptor
    #_{:type  :int32
       :count 3}

    ]
   :zip/local-file-header
   [

    ;; local file header signature     4 bytes  (0x04034b50)
    {:type :int32}
    ;; version needed to extract       2 bytes
    {:type :int16}
    ;; general purpose bit flag        2 bytes
    {:type :int16}
    ;; compression method              2 bytes
    {:type :int16}
    ;; last mod file time              2 bytes
    {:type :int16}
    ;; last mod file date              2 bytes
    {:type :int16}
    ;; crc-32                          4 bytes
    {:type :int32}
    ;; compressed size                 4 bytes
    {:type :int32
     :as   :compressed-size}
    ;; uncompressed size               4 bytes
    {:type :int32}
    ;; file name length                2 bytes
    {:type :int16
     :as   :file-name-length}
    ;; extra field length              2 bytes
    {:type :int16
     :as   :extra-field-length}

    ;; file name (variable size)
    {:type  :uint8
     :count :file-name-length
     :as    :file-name
     :f     :string}
    ;; extra field (variable size)
    {:type  :uint8
     :count :extra-field-length}

    ]})

(comment

  ;; print all file names in zip archive
  (loop [offset 0
         result []]
    (if (== 0x04034b50 (bb/get-uint32 buffer offset))
      (let [buf (bb/slice buffer offset)
            data (dc/decode buf frames :zip/file xforms)]
        (recur (+ offset (::dc/byte-count (meta data)))
               (conj result data)))
      (run! (comp prn :file-name :header) result)))

  )

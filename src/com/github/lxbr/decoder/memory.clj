(ns com.github.lxbr.decoder.memory
  (:require [com.github.lxbr.decoder.platform.jvm.memory-segment :as mem]))

(set! *warn-on-reflection* true)

(defn decode-at
  ([position buffer frames root-frame]
   (decode-at position buffer frames root-frame {} []))
  ([position buffer frames root-frame transforms]
   (decode-at position buffer frames root-frame transforms []))
  ([position buffer frames root-frame transforms context]
   (let [frame (get frames root-frame)]
     (loop [pos    (long position)
            frame  frame
            result {}]
       (if-some [{:keys [as f]
                  tag   :type
                  cnt   :count} (first frame)]
         (let [cnt' (cond
                      (keyword? cnt) (some cnt (rseq (conj context result)))
                      (vector? cnt)  (some #(get-in % cnt) (rseq (conj context result)))
                      (fn? cnt)      (cnt (conj context result))
                      :else          cnt)
               xf   (get transforms f identity)]
           (if (some? cnt')
             (let [cnt' (long cnt')
                   values (case tag
                            :int8
                            (when (some? as)
                              (mem/int8-array (mem/slice buffer pos cnt')))
                            :uint8
                            (when (some? as)
                              (mem/uint8-array (mem/slice buffer pos cnt')))
                            :int16
                            (when (some? as)
                              (mem/int16-array (mem/slice buffer pos (* 2 cnt'))))
                            :uint16
                            (when (some? as)
                              (mem/uint16-array (mem/slice buffer pos (* 2 cnt'))))
                            :int32
                            (when (some? as)
                              (mem/int32-array (mem/slice buffer pos (* 4 cnt'))))
                            :uint32
                            (when (some? as)
                              (mem/uint32-array (mem/slice buffer pos (* 4 cnt'))))
                            :int64
                            (when (some? as)
                              (mem/int64-array (mem/slice buffer pos (* 8 cnt'))))
                            :uint64
                            (when (some? as)
                              (mem/uint64-array (mem/slice buffer pos (* 8 cnt'))))
                            (let [ctx (conj context result)]
                              (loop [i    0
                                     pos' pos
                                     ret  []]
                                (if (< i cnt')
                                  (let [value (decode-at pos' buffer frames
                                                         tag transforms ctx)
                                        len   (long (:com.github.lxbr.decoder/byte-count (meta value)))]
                                    (recur (inc i) (+ pos' len) (conj ret value)))
                                  ret))))
                   length (case tag
                            (:int8  :uint8)  (* 1 cnt')
                            (:int16 :uint16) (* 2 cnt')
                            (:int32 :uint32) (* 4 cnt')
                            (:int64 :uint64) (* 8 cnt')
                            (transduce (map (comp :com.github.lxbr.decoder/byte-count meta)) + 0 values))]
               (->> (cond-> result
                      (some? as) (assoc as (xf values)))
                    (recur (+ pos (long length)) (next frame))))
             (let [value  (case tag
                            :int8   (mem/get-int8   buffer pos)
                            :uint8  (mem/get-uint8  buffer pos)
                            :int16  (mem/get-int16  buffer pos)
                            :uint16 (mem/get-uint16 buffer pos)
                            :int32  (mem/get-int32  buffer pos)
                            :uint32 (mem/get-uint32 buffer pos)
                            :int64  (mem/get-int64  buffer pos)
                            :uint64 (mem/get-uint64 buffer pos)
                            (->> (conj context result)
                                 (decode-at pos buffer frames tag transforms)))
                   length (case tag
                            (:int8  :uint8)  1
                            (:int16 :uint16) 2
                            (:int32 :uint32) 4
                            (:int64 :uint64) 8
                            (:com.github.lxbr.decoder/byte-count (meta value)))]
               (->> (cond-> result
                      (some? as) (assoc as (xf value)))
                    (recur (+ pos (long length)) (next frame))))))
         (with-meta result {:com.github.lxbr.decoder/byte-count (- pos (long position))}))))))

(defn decode
  ([buffer frames root-frame]
   (decode buffer frames root-frame {} []))
  ([buffer frames root-frame transforms]
   (decode buffer frames root-frame transforms []))
  ([buffer frames root-frame transforms context]
   (decode-at 0 buffer frames root-frame transforms context)))

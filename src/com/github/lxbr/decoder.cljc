(ns com.github.lxbr.decoder
  (:require [#?(:clj  com.github.lxbr.decoder.platform.jvm.byte-buffer
                :cljs com.github.lxbr.decoder.platform.js.byte-buffer)
             :as bb]))

#?(:clj (set! *warn-on-reflection* true))

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
                      (fn? cnt)      (cnt (conj context result))
                      :else          cnt)
               xf   (get transforms f identity)]
           (if (some? cnt')
             (let [values (case tag
                            (:byte  :ubyte)
                            (when (some? as)
                              (->> (range pos (+ pos cnt'))
                                   (map #(bb/get-byte buffer %))
                                   #?(:clj  (byte-array)
                                      :cljs (into-array))
                                   #?(:cljs (js/Uint8Array.))))
                            (:int16  :uint16)
                            (when (some? as)
                              (->> (range pos (+ pos (* 2 cnt')) 2)
                                   (map #(bb/get-int16 buffer %))
                                   #?(:clj  (short-array)
                                      :cljs (into-array))
                                   #?(:cljs (js/Uint16Array.))))
                            (:int32  :uint32)
                            (when (some? as)
                              (->> (range pos (+ pos (* 4 cnt')) 4)
                                   (map #(bb/get-int32 buffer %))
                                   #?(:clj  (int-array)
                                      :cljs (into-array))
                                   #?(:cljs (js/Uint32Array.))))
                            (let [ctx (conj context result)]
                              (loop [i    0
                                     pos' pos
                                     ret  []]
                                (if (< i cnt')
                                  (let [value (decode-at pos' buffer frames
                                                         tag transforms ctx)
                                        len   (long (::byte-count (meta value)))]
                                    (recur (inc i) (+ pos' len) (conj ret value)))
                                  ret))))
                   length (case tag
                            (:byte  :ubyte)  (* 1 cnt')
                            (:int16 :uint16) (* 2 cnt')
                            (:int32 :uint32) (* 4 cnt')
                            (transduce (map (comp ::byte-count meta)) + 0 values))]
               (->> (cond-> result
                      (some? as) (assoc as (xf values)))
                    (recur (+ pos (long length)) (next frame))))
             (let [value  (case tag
                            :byte   (bb/get-byte   buffer pos)
                            :ubyte  (bb/get-ubyte  buffer pos)
                            :int16  (bb/get-int16  buffer pos)
                            :uint16 (bb/get-uint16 buffer pos)
                            :int32  (bb/get-int32  buffer pos)
                            :uint32 (bb/get-uint32 buffer pos)
                            (->> (conj context result)
                                 (decode-at pos buffer frames tag transforms)))
                   length (case tag
                            (:byte  :ubyte)  1
                            (:int16 :uint16) 2
                            (:int32 :uint32) 4
                            (::byte-count (meta value)))]
               (->> (cond-> result
                      (some? as) (assoc as (xf value)))
                    (recur (+ pos (long length)) (next frame))))))
         (with-meta result {::byte-count (- pos position)}))))))

(defn decode
  ([buffer frames root-frame]
   (decode buffer frames root-frame {} []))
  ([buffer frames root-frame transforms]
   (decode buffer frames root-frame transforms []))
  ([buffer frames root-frame transforms context]
   (decode-at 0 buffer frames root-frame transforms context)))

(ns com.github.lxbr.decoder.platform.jvm.byte-buffer
  (:import java.nio.ByteBuffer
           java.nio.ByteOrder))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn byte-buffer
  ^ByteBuffer
  [^long size endianness]
  (doto (ByteBuffer/allocate size)
    (.order (case endianness
              :le ByteOrder/LITTLE_ENDIAN
              :be ByteOrder/BIG_ENDIAN))))

(defn unwrap-bytes
  ^bytes
  [^ByteBuffer buffer]
  (.array buffer))

(defn wrap-bytes
  [bytes endianness]
  (doto (ByteBuffer/wrap bytes)
    (.order (case endianness
              :le ByteOrder/LITTLE_ENDIAN
              :be ByteOrder/BIG_ENDIAN))))

(defn slice
  ^ByteBuffer
  ([^ByteBuffer buffer ^long position]
   (slice buffer position (- (.capacity buffer) position)))
  ([^ByteBuffer buffer ^long position ^long length]
   (-> (doto (.duplicate buffer)
         (.position position)
         (.limit (+ position length)))
       (.slice)
       (.order (.order buffer)))))

(defn get-byte
  ^long
  [^ByteBuffer buffer ^long position]
  (long (.get buffer position)))

(defn get-ubyte
  ^long
  [buffer position]
  (bit-and 0xff (get-byte buffer position)))

(defn get-int16
  ^long
  [^ByteBuffer buffer ^long position]
  (long (.getShort buffer position)))

(defn get-uint16
  ^long
  [buffer position]
  (bit-and 0xffff (get-int16 buffer position)))

(defn get-int32
  ^long
  [^ByteBuffer buffer ^long position]
  (.getInt buffer position))

(defn get-uint32
  ^long
  [buffer position]
  (bit-and 0xffffffff (get-int32 buffer position)))

(defn put-byte
  [^ByteBuffer buffer ^long position ^long value]
  (.put buffer position value))

(defn put-int16
  [^ByteBuffer buffer ^long position ^long value]
  (.putShort buffer position value))

(defn put-int32
  [^ByteBuffer buffer ^long position ^long value]
  (.putInt buffer position value))

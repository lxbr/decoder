(ns com.github.lxbr.decoder.platform.js.byte-buffer)

(deftype ByteBuffer [data-view little-endian])

(defn byte-buffer
  [size endianness]
  (ByteBuffer. (js/DataView. (js/ArrayBuffer. size))
               (case endianness
                 :le true
                 :be false)))

(defn unwrap-bytes
  [buffer]
  (js/Uint8Array. (.-buffer (.-data-view buffer))))

(defn wrap-bytes
  [bytes endianness]
  (ByteBuffer.
   (js/DataView.
    (if (or (instance? js/Uint8Array bytes)
            (instance? js/Int8Array bytes)
            (instance? js/Uint8ClampedArray bytes))
      (.-buffer bytes)
      (.-buffer (js/Uint8Array. bytes))))
   (case endianness
     :le true
     :be false)))

(defn slice
  ([buffer position]
   (slice buffer position (- (.-length (.-buffer buffer)) position)))
  ([buffer position length]
   (let [offset (.-byteOffset (.-data-view buffer))]
     (ByteBuffer.
      (js/DataView. (.-buffer (.-data-view buffer)) (+ offset position) length)
      (.-little-endian buffer)))))

(defn get-byte
  [buffer position]
  (.getInt8 (.-data-view buffer) position))

(defn get-ubyte
  [buffer position]
  (.getUint8 (.-data-view buffer) position))

(defn get-int16
  [buffer position]
  (.getInt16 (.-data-view buffer) position (.-little-endian buffer)))

(defn get-uint16
  [buffer position]
  (.getUint16 (.-data-view buffer) position (.-little-endian buffer)))

(defn get-int32
  [buffer position]
  (.getInt32 (.-data-view buffer) position (.-little-endian buffer)))

(defn get-uint32
  [buffer position]
  (.getUint32 (.-data-view buffer) position (.-little-endian buffer)))

(defn put-byte
  [buffer position value]
  (.setUint8 (.-data-view buffer) position (bit-and 0xff value)))

(defn put-int16
  [buffer position value]
  (.setUint16 (.-data-view buffer) position (bit-and 0xffff value) (.-little-endian buffer)))

(defn put-int32
  [buffer position value]
  (.setUint32 (.-data-view buffer) position (bit-and 0xffffffff value) (.-little-endian buffer)))

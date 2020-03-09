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

(defn get-int8
  [buffer position]
  (.getInt8 (.-data-view buffer) position))

(defn get-uint8
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

(defn get-int64
  [buffer position]
  (.getBigInt64 (.-data-view buffer) position (.-little-endian buffer)))

(defn get-uint64
  [buffer position]
  (.getBigUint64 (.-data-view buffer) position (.-little-endian buffer)))

(defn put-int8
  [buffer position value]
  (.setUint8 (.-data-view buffer) position (bit-and 0xff value)))

(defn put-int16
  [buffer position value]
  (.setUint16 (.-data-view buffer) position (bit-and 0xffff value) (.-little-endian buffer)))

(defn put-int32
  [buffer position value]
  (.setUint32 (.-data-view buffer) position (bit-and 0xffffffff value) (.-little-endian buffer)))

(defn put-int64
  [buffer position value]
  (.setBigUint64 (.-data-view buffer) position (bit-and 0xffffffffffffffff value) (.-little-endian buffer)))

(defn int8-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/Int8Array. (.-buffer view) (.-byteOffset view))))

(defn uint8-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/Uint8Array. (.-buffer view) (.-byteOffset view))))

(defn int16-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/Int16Array. (.-buffer view) (.-byteOffset view))))

(defn uint16-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/Uint16Array. (.-buffer view) (.-byteOffset view))))

(defn int32-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/Int32Array. (.-buffer view) (.-byteOffset view))))

(defn uint32-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/Uint32Array. (.-buffer view) (.-byteOffset view))))

(defn int64-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/BigInt64Array. (.-buffer view) (.-byteOffset view))))

(defn uint64-array
  [buffer]
  (let [view (.-data-view buffer)]
    (js/BigUint64Array. (.-buffer view) (.-byteOffset view))))

(ns com.github.lxbr.decoder.platform.jvm.memory-segment
  (:import java.lang.foreign.MemorySegment
           java.lang.foreign.ValueLayout
           java.lang.foreign.ValueLayout$OfShort
           java.lang.foreign.ValueLayout$OfInt
           java.lang.foreign.ValueLayout$OfLong))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defonce ^ValueLayout$OfShort value-layout-short
  (ValueLayout$OfShort/.withByteAlignment ValueLayout/JAVA_SHORT 1))

(defonce ^ValueLayout$OfInt value-layout-int
  (ValueLayout$OfInt/.withByteAlignment ValueLayout/JAVA_INT 1))

(defonce ^ValueLayout$OfLong value-layout-long
  (ValueLayout$OfLong/.withByteAlignment ValueLayout/JAVA_LONG 1))

(defn slice
  ^MemorySegment
  ([^MemorySegment memory ^long position]
   (MemorySegment/.asSlice memory position))
  ^MemorySegment
  ([^MemorySegment memory ^long position ^long length]
   (MemorySegment/.asSlice memory position length)))

(defn get-int8
  ^long
  [^MemorySegment memory ^long position]
  (long (MemorySegment/.get memory ValueLayout/JAVA_BYTE position)))

(defn get-uint8
  ^long
  [^MemorySegment memory ^long position]
  (bit-and 0xff (get-int8 memory position)))

(defn get-int16
  ^long
  [^MemorySegment memory ^long position]
  (long (MemorySegment/.get memory value-layout-short position)))

(defn get-uint16
  ^long
  [^MemorySegment memory ^long position]
  (bit-and 0xffff (get-int16 memory position)))

(defn get-int32
  ^long
  [^MemorySegment memory ^long position]
  (long (MemorySegment/.get memory value-layout-int position)))

(defn get-uint32
  ^long
  [^MemorySegment memory ^long position]
  (bit-and 0xffffffff (get-int32 memory position)))

(defn get-int64
  ^long
  [^MemorySegment memory ^long position]
  (MemorySegment/.get memory value-layout-long position))

(defn get-uint64
  ^long
  [^MemorySegment memory ^long position]
  (get-int64 memory position))

(defn put-int8
  [^MemorySegment memory ^long position ^long value]
  (MemorySegment/.set memory ValueLayout/JAVA_BYTE position (byte value)))

(defn put-int16
  [^MemorySegment memory ^long position ^long value]
  (MemorySegment/.set memory value-layout-short position (short value)))

(defn put-int32
  [^MemorySegment memory ^long position ^long value]
  (MemorySegment/.set memory value-layout-int position (int value)))

(defn put-int64
  [^MemorySegment memory ^long position ^long value]
  (MemorySegment/.set memory value-layout-long position value))

(defn int8-array
  [^MemorySegment memory]
  (MemorySegment/.toArray memory ValueLayout/JAVA_BYTE))

(defn uint8-array
  [^MemorySegment memory]
  (int8-array memory))

(defn int16-array
  [^MemorySegment memory]
  (MemorySegment/.toArray memory value-layout-short))

(defn uint16-array
  [^MemorySegment memory]
  (int16-array memory))

(defn int32-array
  [^MemorySegment memory]
  (MemorySegment/.toArray memory value-layout-int))

(defn uint32-array
  [^MemorySegment memory]
  (int32-array memory))

(defn int64-array
  [^MemorySegment memory]
  (MemorySegment/.toArray memory value-layout-long))

(defn uint64-array
  [^MemorySegment memory]
  (int64-array memory))

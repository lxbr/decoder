(ns com.github.lxbr.decoder.test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [clojure.walk]
            [com.github.lxbr.decoder :as dec]
            [#?(:clj  com.github.lxbr.decoder.platform.jvm.byte-buffer
                :cljs com.github.lxbr.decoder.platform.js.byte-buffer)
             :as bb]))

(deftest decode-var-length-string
  (let [string "Hello, world!"
        bytes  #?(:clj  (.getBytes string "UTF-8")
                  :cljs (->> (for [i (range (count string))]
                               (.charCodeAt string i))
                             (into-array)))
        length (alength bytes)
        buffer (bb/byte-buffer (inc length) :le)]
    (bb/put-int8 buffer 0 length)
    (dotimes [i (alength bytes)]
      (bb/put-int8 buffer (inc i) (aget bytes i)))
    (is (= {:length length :string string}
           (dec/decode
            buffer
            {:var-length-string
             [

              {:type :uint8
               :as   :length}
              
              {:type  :int8
               :count :length
               :as    :string
               :f     :to-string}

              ]}
            :var-length-string
            {:to-string
             #?(:clj  #(String. % "UTF-8")
                :cljs #(js/String.fromCharCode.apply nil %))})))))

(deftest decode-recursive-structure
  (let [tree   {:value 0
                :children
                [{:value 1
                  :children []}
                 {:value 2
                  :children
                  [{:value 4
                    :children
                    [{:value 7
                      :children []}
                     {:value 8
                      :children
                      [{:value 9
                        :children []}]}]}
                   {:value 5
                    :children []}
                   {:value 6
                    :children []}]}
                 {:value 3
                  :children []}]}
        bytes  (->> (tree-seq :children :children tree)
                    (mapcat (fn [{:keys [value children]}]
                              [value (count children)]))
                    #?(:clj  (byte-array)
                       :cljs (into-array)))
        buffer (bb/wrap-bytes bytes :le)
        result (dec/decode
                buffer
                {:tree
                 [

                  {:type :int8
                   :as   :value}

                  {:type :int8
                   :as   :child-count}

                  {:type  :tree
                   :count :child-count
                   :as    :children}

                  ]}
                :tree)]
    (is (= tree
           (clojure.walk/postwalk
            (fn [x] (cond-> x (map? x) (dissoc :child-count)))
            result)))
    (is (= (count bytes) (::dec/byte-count (meta result))))
    (doseq [node (tree-seq :children :children result)]
      (is (= (::dec/byte-count (meta node))
             (->> (:children node)
                  (map (comp ::dec/byte-count meta))
                  (reduce + 2)))))))

(deftest endianness
  (testing "int64"
    (let [value     (unchecked-long 0xdeadbeefcafebabe)
          buffer-le (bb/byte-buffer 8 :le)
          buffer-be (bb/byte-buffer 8 :be)]
      (bb/put-int64 buffer-le 0 value)
      (bb/put-int64 buffer-be 0 value)
      (is (= value
             (bb/get-uint64 buffer-le 0)
             (bb/get-uint64 buffer-be 0)))
      (is (= (#?(:clj unchecked-long :cljs long) value)
             (bb/get-int64 buffer-le 0)
             (bb/get-int64 buffer-be 0)))))

  (testing "int32"
    (let [value     0xdeadbeef
          buffer-le (bb/byte-buffer 4 :le)
          buffer-be (bb/byte-buffer 4 :be)]
      (bb/put-int32 buffer-le 0 value)
      (bb/put-int32 buffer-be 0 value)
      (is (= value
             (bb/get-uint32 buffer-le 0)
             (bb/get-uint32 buffer-be 0)))
      (is (= (#?(:clj unchecked-int :cljs int) value)
             (bb/get-int32 buffer-le 0)
             (bb/get-int32 buffer-be 0)))))

  (testing "int16"
    (let [buffer-le (bb/byte-buffer 4 :le)
          buffer-be (bb/byte-buffer 4 :be)]
      (bb/put-int16 buffer-le 0 0xdead)
      (bb/put-int16 buffer-le 2 0xbeef)
      (bb/put-int16 buffer-be 0 0xdead)
      (bb/put-int16 buffer-be 2 0xbeef)
      (is (= [0xdead 0xbeef]
             [(bb/get-uint16 buffer-le 0)
              (bb/get-uint16 buffer-le 2)]
             [(bb/get-uint16 buffer-be 0)
              (bb/get-uint16 buffer-be 2)]))
      (is (= 0xbeefdead (bb/get-uint32 buffer-le 0)))
      (is (= 0xdeadbeef (bb/get-uint32 buffer-be 0))))))

(deftest decode-at-offset
  (let [buffer (bb/byte-buffer 10 :le)
        high   1074340347
        low    1413754136
        frames {:test
                [

                 {:type :pair
                  :as   :PI
                  :f    :to-double}

                 ]

                :pair
                [

                 {:type :int32
                  :as   :low}
                 
                 {:type :int32
                  :as   :high}

                 ]}
        xforms {:to-double
                (fn [{:keys [low high]}]
                  #?(:clj  (Double/longBitsToDouble
                            (bit-or low (bit-shift-left high 32)))
                     :cljs (-> (array low high)
                               (js/Int32Array.)
                               (.-buffer)
                               (js/DataView.)
                               (.getFloat64 0 true))))}]
    (bb/put-int32 buffer 1 low)
    (bb/put-int32 buffer 5 high)
    (is (= #?(:clj Math/PI :cljs js/Math.PI)
           (:PI (dec/decode-at 1 buffer frames :test xforms))))))

(deftest slicing
  (let [buffer (bb/byte-buffer 100 :be)
        data   [72 101 108 108 111 44 32 119 111 114 108 100 33]
        offset 37
        slice  (bb/slice buffer (inc offset) (count data))]
    (bb/put-int8 buffer 0 offset)
    (bb/put-int8 buffer offset (count data))
    (dotimes [i (count data)]
      (bb/put-int8 slice i (nth data i)))
    (let [{:keys [data]}
          (dec/decode
           buffer
           {:header
            [

             {:type :uint8
              :as   :offset}

             {:type  :int8
              :count (fn [context]
                       (dec (some :offset context)))}

             {:type :data
              :as   :data}
                        
             ]
            :data
            [

             {:type :int8
              :as   :length}

             {:type :int8
              :count :length
              :as :value
              :f :string}

             ]
            }
           :header
           {:string
            (fn [bytes]
              #?(:clj (String. bytes)
                 :cljs (js/String.fromCharCode.apply nil bytes)))})
          {:keys [value]} data]
      (is (= "Hello, world!" value)))))

(deftest arrays
  (let [frames {:root
                [

                 {:type  :int8
                  :count 10
                  :as    :bytes
                  :f     :vector}

                 {:type  :int16
                  :count 10
                  :as    :shorts
                  :f     :vector}

                 {:type  :int32
                  :count 10
                  :as    :ints
                  :f     :vector}

                 {:type  :int64
                  :count 10
                  :as    :longs
                  :f     :vector}

                 ]}
        xforms {:vector #?(:clj vec :cljs (comp vec array-seq))}
        buffer (bb/byte-buffer (+ 10 (* 2 10) (* 4 10) (* 8 10)) :be)]
    (dotimes [i 10]
      (bb/put-int8  buffer i i)
      (bb/put-int16 buffer (+ 10 (* 2 i)) i)
      (bb/put-int32 buffer (+ 10 (* 2 10) (* 4 i)) i)
      (bb/put-int64 buffer (+ 10 (* 2 10) (* 4 10) (* 8 i)) i))
    (is (= {:bytes  (range 10)
            :shorts (range 10)
            :ints   (range 10)
            :longs  (range 10)}
           (dec/decode buffer frames :root xforms)))))

(comment

  (test/run-tests)

  )

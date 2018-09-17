# Decoder

A library for parsing binary data via declarative specifications.

## Frames

A frame describes how a sequence of bytes is parsed. It is a vector
of maps where each map describes a parse step. Each parse step has
the following shape:

``` clojure
(s/def ::type (s/or :primitive #{:byte  :ubyte
                                 :int16 :uint16
                                 :int32 :uint32}
                    :custom keyword?))
				
(s/def ::count (s/or :number   nat-int?
                     :keyword  keyword?
                     :function fn?)

(s/def ::as keyword?)

(s/def ::f keyword?)

(s/def ::parse-step
  (s/keys :req-un [::type] :opt-un [::count ::as ::f]))
```

Individual frames are combined into a map indexed by name:

``` clojure
(s/def ::frame (s/coll-of ::parse-step :kind vector?))

(s/def ::frames (s/map-of keyword? ::frame))
```

## Buffers

Buffers wrap byte arrays and provide read/write functionality for 1, 2 and 4 byte values.
Existing byte arrays can be wrapped or new buffers allocated. The endianness is provided
at construction time, `:le` for little and `:be` for big endian order.

``` clojure
(let [bytes  (byte-array [1 2 3 4])
      buffer (wrap-bytes bytes :be)] ;; a buffer of 4 bytes in big endian order
  (identical? bytes (unwrap-bytes buffer)) ;; => true

(byte-buffer 100 :le) ;; a buffer of 100 bytes in little endian order
```

A buffer can be sliced to get a subrange of its contents. A slice is just a view over
an existing buffer and therefore write operations to either are visible in both.

``` clojure
(let [buffer  (byte-buffer 8 :be)
      slice-a (slice buffer 0 4)
      slice-b (slice buffer 4 4)]
  (dotimes [i 4]
    (put-byte slice-a i i)
    (put-byte slice-b i i))
  (vec (unwrap-bytes buffer))) ;; => [0 1 2 3 0 1 2 3]
```

## Decoding Data

Given a buffer, frames indexed by name and the name of the root frame, the `decode` function
returns a (potentially nested) map of decoded data. To automatically transform decoded
values another map can be passed that maps transformation names to functions.

``` clojure
(let [bytes  (byte-array [13 72 101 108 108 111 44 32 119 111 114 108 100 33])
      buffer (wrap-bytes bytes :be)
      root-frame [

                  {:type :byte
                   :as   :length}

                  {:type  :byte
                   :count :length
                   :as    :value
                   :f     :string}

                  ]]
  (decode buffer {:root root-frame} :root {:string #(String. %)})) 

;; => {:length 13, :value "Hello, world!"}
```

For more examples please see the tests in the `com.github.lxbr.decoder.test` namespace.

## License

This project is licensed under the terms of the Eclipse Public License 1.0 (EPL).

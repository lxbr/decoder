# Decoder Library - Compact Reference

Clojure/ClojureScript binary data parser using declarative frame specifications.

## Core API

**Main functions:**
- `(decode buffer frames root-frame [transforms] [context])` - Parse from start
- `(decode-at position buffer frames root-frame [transforms] [context])` - Parse from offset

**Buffer operations:** (ns: `com.github.lxbr.decoder.platform.{jvm|js}.byte-buffer`)
- `(byte-buffer size endianness)` `(wrap-bytes bytes endianness)` - Create buffers (:le/:be)
- `(slice buffer pos [len])` - Create view
- `(get-{int8|uint8|int16|uint16|int32|uint32|int64|uint64} buffer pos)` - Read values
- `(put-{int8|int16|int32|int64} buffer pos value)` - Write values
- `({int8|uint8|int16|uint16|int32|uint32|int64|uint64}-array buffer)` - Extract arrays

## Frame Specification

**Parse step format:**
```clojure
{:type primitive-or-frame-keyword  ; :int8 :uint8 :int16 :uint16 :int32 :uint32 :int64 :uint64 or custom
 :count number|keyword|[path]|fn   ; optional: fixed, field ref, nested path, or function  
 :as keyword                       ; optional: result map key
 :f keyword}                       ; optional: transform function name
```

**Frames map:** `{:frame-name [{parse-step} ...]}`

## Usage Examples

**Basic parsing:**
```clojure
(decode buffer {:root [{:type :int32 :as :value}]} :root)
;; => {:value 42}
```

**Variable length:**
```clojure
(decode buffer 
  {:msg [{:type :uint8 :as :len}
         {:type :int8 :count :len :as :data :f :string}]}
  :msg {:string #(String. %)})
;; => {:len 5, :data "hello"}
```

**Recursive/nested:**
```clojure
{:tree [{:type :int8 :as :value}
        {:type :int8 :as :count}
        {:type :tree :count :count :as :children}]}
```

**Count variations:**
- Fixed: `:count 10`
- Field ref: `:count :header-length`  
- Nested: `:count [:header :size]`
- Function: `:count (fn [ctx] (some :computed-size ctx))`

**Transforms:** Map transform names to functions that process parsed values.

**Metadata:** All results have `::byte-count` metadata indicating bytes consumed.

**Key patterns:** Use `:as` to name fields, `:f` to transform values, reference previous fields in `:count`.
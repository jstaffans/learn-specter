## Rationale 

* selection, transformation of nested data structures
* `get-in`, `update-in` on steroids

```
[{:city "Berlin" :country "DE" :temperature {:min 2 :max 7}}
 {:city "Helsinki" :country "FIN" :temperature {:min -12 :max -4}}
 {:city "New York" :country "US" :temperature {:min -7 :max -5}}
 ...]
```

#### "Convert US temperatures to Fahrenheit"

```clojure
;; Naïve version using standard library

;; Need to use mapv to preserve vector format
(mapv
  (fn [c]
    (if (= (:country c) "US")
      (-> c
          (update-in [:temperature :min] #(+ (* 1.8 %) 32))
          (update-in [:temperature :max] #(+ (* 1.8 %) 32)))
      c))
  cities)
  
=> [{:city "Berlin", :country "DE", :temperature {:min 2.0, :max 7.0}}
    {:city "Helsinki",
     :country "FIN",
     :temperature {:min -12.0, :max -4.0}}
    {:city "New York", :country "US", :temperature {:min 53.6, :max 59.0}}]
```

```clojure
;; Using Specter

(s/transform 
   [s/ALL                         ; navigate to all matching elements
   #(= (:country %) "US")         ; select map elements whose :country key is "US"
   :temperature                   ; select the :temperature map
   (s/multi-path :max :min)]      ; select the :min and :max keys
   #(+ (* 1.8 %) 32)              ; update values
   cities)

=> [{:city "Berlin", :country "DE", :temperature {:min 2.0, :max 7.0}}
    {:city "Helsinki",
     :country "FIN",
     :temperature {:min -12.0, :max -4.0}}
    {:city "New York", :country "US", :temperature {:min 53.6, :max 59.0}}]
```

#### "Find global min and max temperatures"

``` 
;; Map/reduce approach

(->> (mapv
       (fn [c]
         [(get-in c [:temperature :min])
          (get-in c [:temperature :max])])
       cities)
     (reduce 
       (fn [curr next]  
         [(min (nth curr 0) (nth next 0)) 
          (max (nth curr 1) (nth next 1))])))

=> [-12.0 15.0]
```

```
(def min-max (juxt (partial apply min) (partial apply max)))

(-> (s/select [s/ALL 
               :temperature 
               (s/multi-path :min :max)] 
              cities)
    min-max)

=> [-12.0 15.0]

;; If we know that the temperatures are the only floating-point values in the data structure:

(-> (s/select [(s/walker float?)] cities)
    min-max)
    
=> [-12 15.0]
```

* declarative
* preserves form of collection
* (eager)
 




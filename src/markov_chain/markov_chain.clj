(ns markov-chain.markov-chain
  (:require [clojure.tools.logging :as log]
            [clojure.math.numeric-tower :as math]))

(defn new-mc [] {})

(defn new-empty-item [item]
  {:item item
   :count 0
   :prob 0.0
   :next-mc (new-mc)})

(defn add-item [mc item]
  (if (mc item)
    mc
    (assoc mc item (new-empty-item item))))

(defn get-item-k [mc item k]
  (k (if (mc item) (mc item) ((add-item mc item) item))))

(defn update-item-k [mc item f k]
  (update-in (if (mc item)  mc (add-item mc item)) [item k] f))

(defn get-item-count [mc item]
  (get-item-k mc item :count))

(defn update-item-count [mc item f]
  (update-item-k mc item f :count))

(defn get-item-prob [mc item]
  (get-item-k mc item :prob))

(defn set-item-prob [mc item prob]
  (update-item-k mc item (fn [a] prob) :prob))

(defn set-item-next-mc [mc item next-mc]
  (update-item-k mc item (fn [a] next-mc) :next-mc))

(defn get-item-next-mc [mc item]
  (get-item-k mc item :next-mc))

(defn calc-count-sum [mc]
  (reduce + (map :count (vals mc))))

(defn update-probs [mc]
  (if-not (empty? mc)
    (let [count-sum (calc-count-sum mc)]
      (->> (map (fn [[k v]] {k (assoc v
                                :prob (if (> count-sum 0)
                                        (/ (:count v) count-sum)
                                        0)
                                :next-mc (update-probs (:next-mc v)))})
                mc)
           (apply merge)))
    mc))

(defn train-mc-chunk [mc chunk]
  (if (empty? chunk)
    mc
    (let [item (first chunk)
          mc (update-item-count mc item inc)
          next-mc (get-item-next-mc mc item)]
      (set-item-next-mc
       mc
       item
       (train-mc-chunk next-mc (rest chunk))))))

(defn train [mc mc-depth training-seq]
  (-> (loop [mc           mc
             training-seq training-seq]
        (if (empty? training-seq)
          mc
          (recur (train-mc-chunk mc (take mc-depth training-seq)) (rest training-seq))))
      update-probs))

(defn summation [n] (/ (* (+ n 1) n) 2))

(defn calc-prob-of-chunk [mc chunk]
  (loop [mc    mc
         prob  0
         level 1
         c     chunk]
    (if (empty? mc)
      (/ prob (summation (count chunk)) 1.0)
      (let [item (first c)]
        (recur (get-item-next-mc mc item)
               (+ prob (* level (get-item-prob mc item)))
               (inc level)
               (rest c))))))

(defn detect-likeness [mc mc-depth tested-seq]
  (loop [tseq     tested-seq
         prob-sum 0]
    (if (empty? tseq)
      (/ prob-sum (count tested-seq) 1.0)
      (recur (rest tseq)
             (+ prob-sum (calc-prob-of-chunk mc (take mc-depth tseq)))))))

(def selection-pressure 1)

(defn roulette-select [kvs]
  (let [vsum    (reduce + (map #(math/expt % selection-pressure) (vals kvs)))
        stop-at (rand-int vsum)]
    (loop [pairs    kvs
           counting 0]
      (let [[k v] (first pairs)
            v     (math/expt v selection-pressure)]
        (if (> (+ counting v) stop-at)
          k
          (recur (rest pairs) (+ counting v)))))))

(defn mc-to-kvs [mc]
  (->> mc
       (map (fn [[k v]] {k (:count v)}))
       (apply merge)))

(defn generate-likeness [mc likeness-length]
  (loop [len      0
         mc-route []
         result   []]
    (if (= len likeness-length)
      result
      (let [current-mc (loop [mc       mc
                              mc-route mc-route]
                         (if (empty? mc-route)
                           mc
                           (recur (get-item-next-mc mc (first mc-route))
                                  (rest mc-route))))]
        (if (empty? current-mc)
          (recur len (rest mc-route) result)
          (let [item (roulette-select (mc-to-kvs current-mc))]
            (recur (inc len) (concat mc-route [item]) (conj result item))))))))

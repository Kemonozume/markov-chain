(ns language
  (:require [markov-chain.core :as markov]))

(defn dickens-character-training-seq []
  (->> (slurp "resources/great_expectations_chapter_5.txt")
       seq))

(defn dickens-word-training-seq []
  (->> (-> (slurp "resources/great_expectations.txt")
           (clojure.string/replace #"[^\w]" " ")
           (clojure.string/lower-case)
           (clojure.string/split #"\s+"))))

(def mc-depth 3)

(comment

  ;; by characters ;;;;;;;
  ;; detect:
  (-> (markov/train (markov/new-mc) mc-depth (dickens-character-training-seq))
      (markov/detect-likeness mc-depth "I was walking on the road."))

  (-> (markov/train (markov/new-mc) mc-depth (dickens-character-training-seq))
      (markov/detect-likeness mc-depth "a;lsjdfj lkeflh ashoe iofj"))

  ;; generate:
  (->> (-> (markov/train (markov/new-mc) mc-depth (dickens-character-training-seq))
           (markov/generate-likeness 100))
       (reduce str ""))


  ;; by words ;;;;;;
  ;; detect:
  (-> (markov/train (markov/new-mc) mc-depth (dickens-word-training-seq))
      (markov/detect-likeness mc-depth
                              (-> "i went to the room with them"
                                  (clojure.string/split #"\s+"))))

  (-> (markov/train (markov/new-mc) mc-depth (dickens-word-training-seq))
      (markov/detect-likeness mc-depth
                              (-> "them room the i with to went"
                                  (clojure.string/split #"\s+"))))

  ;; generate:
  (->>
   (-> (markov/train (markov/new-mc) mc-depth (dickens-word-training-seq))
       (markov/generate-likeness 100))
   (reduce (fn [a b] (str a " " b)) ""))

  )

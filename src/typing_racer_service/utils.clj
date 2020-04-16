(ns typing-racer-service.utils)

(defn flip [fun] (fn [a b] (fun b a)))
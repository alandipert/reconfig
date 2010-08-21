(ns org.dipert.reconfig.test
  (:use clojure.test
        [org.dipert.reconfig :only (reconfig)])
  (:import (java.lang.management ManagementFactory)
           (java.io File)))

(defn tmp-file []
  (File/createTempFile "reconfig" ".clj"))

(defn jvm-pid []
  (let [pid-host (.getName (ManagementFactory/getRuntimeMXBean))]
    (first (.split pid-host "@"))))

(defn send-hup [pid]
  (.exec (Runtime/getRuntime) (str "kill -HUP " pid)))

(defn rand-str [len]
  (apply str (map #(char (+ 97 %))
                  (take len (repeatedly #(rand-int 26))))))

(deftest t-default-if-file-doesnt-exist
  (is (= [:l :o :l]
         @(reconfig (str "/bogus/path/" (rand-str 50))
                    [:l :o :l]))))

(deftest t-loads-config-first
  (let [tmp (tmp-file)]
    (spit tmp "[:r :o :f :l]")
    (is (= [:r :o :f :l]
           @(reconfig tmp
                      [:l :m :a :o])))))

(deftest t-sighup-reconfigs
  (let [tmp (tmp-file)
        start-config [:a :s :d {:pants true}]
        default [:a :s :d {:pants false}]
        new-config {:rofl "copter" :loller "skates"}]
    
    (spit tmp (with-out-str (prn start-config)))
    
    (let [config-atom (reconfig tmp
                                default)]
      (is (= start-config
             @config-atom))
      (is (not= default
                @config-atom))

      (spit tmp (with-out-str (prn new-config)))
      (send-hup (jvm-pid))

      (Thread/sleep 1000)               ;wait for HUP/file reading
      
      (is (= new-config
             @config-atom))
      (is (not= (or start-config default)
                @config-atom)))))

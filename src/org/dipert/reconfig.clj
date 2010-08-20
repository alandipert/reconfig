(ns org.dipert.reconfig
  "Reads a file of Clojure data into *config*
   when a SIGHUP is recieved.

   Useful for daemons that support the reloading of 
   configuration data while running."
  (:use [clojure.contrib.io :only (reader)]
        [clojure.contrib.logging :only (info warn)])
  (:import (sun.misc Signal SignalHandler)
           (java.io File PushbackReader)))

(def *config* (agent nil))

(defn load-config [f]
  (send-off *config*
    (constantly
      (let [config-file (File. f)]
        (if (.exists config-file)
          (do (info (format "[reconfig] Reading config file '%s'" config-file))
            (try
              (read (PushbackReader. (reader config-file)))
              (catch Exception e
                (warn (format "[reconfig] Reading config file '%s' failed with %s" config-file e)))))
          (do (warn (format "[reconfig] Config file '%s' doesn't exist"))))))))

(defn set-config [f]
  (Signal/handle (Signal. "HUP")
    (reify SignalHandler
      (handle [_ _] (load-config f)))))

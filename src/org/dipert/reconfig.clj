(ns org.dipert.reconfig
  "Configuration reloading by SIGHUP
  Only works on Mac OS X and Linux with the Sun JVM."
  (:use [clojure.contrib.io :only (file reader)]
        [clojure.contrib.logging :only (info warn)])
  (:import (sun.misc Signal SignalHandler)
           (java.io File PushbackReader)))

(defn- read-config [config-file]
  (if (.exists config-file)
    (try
      (info (format "[reconfig] Reading config file '%s'" config-file))
      (read (PushbackReader. (reader config-file)))
      (catch Exception e
        (warn (format "[reconfig] Reading config file '%s' failed with %s" config-file e))))
    (do (warn (format "[reconfig] Config file '%s' doesn't exist" config-file)))))

(defn reconfig
  "Reads filename f of Clojure data whenever a SIGHUP is caught by the JVM,
  and the file's contents are stored as a data structure in the atom this 
  function returns.

  If on first reading the config file isn't readable, default is used.

  If on subsequent readings the config file isn't readable, the
  atom's contents are not changed."
  [f default]
  {:pre [(or (string? f)
             (= java.io.File
                (class f)))]}
  (let [config-file (file f)
        config-atom (atom (if-let [config (read-config config-file)]
                            config
                            default))]
    (Signal/handle (Signal. "HUP")
      (reify SignalHandler
        (handle [_ _] (future
                        (do (info "[reconfig] SIGHUP caught")
                            (swap! config-atom
                                   (fn [old-config]
                                     (if-let [new-config (read-config config-file)]
                                       new-config
                                       old-config))))))))
    config-atom))

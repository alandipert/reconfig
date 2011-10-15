(ns org.dipert.reconfig
  "Configuration reloading by SIGHUP
  Only works on Mac OS X and Linux with the Sun JVM."
  (:use [clojure.java.io :only (file reader)]
        [clojure.tools.logging :only (info warn)])
  (:import (sun.misc Signal SignalHandler)
           (java.io File PushbackReader)))

(defn- read-config [config-file]
  (if (.exists config-file)
    (try
      (info (format "[reconfig] Reading config file '%s'" config-file))
      (binding [*read-eval* false]
        (read (PushbackReader. (reader config-file))))
      (catch Exception e
        (warn (format "[reconfig] Reading config file '%s' failed with %s" config-file e))))
    (do (warn (format "[reconfig] Config file '%s' doesn't exist" config-file)))))

(defn reconfig
  "Reads the contents of the Clojure file f, stores
  this is an agent, and returns the agent.

  If f cannot be read, the agent's content is the
  default argument.

  When the JVM receives a SIGHUP, f is read again
  and its contents are sent to the agent.  If
  reading f fails, the agent's contents are not modified."
  [f default]
  {:pre [(or (string? f)
             (= File (class f)))]}
  (let [config-file (file f)
        agt (agent (if-let [config (read-config config-file)]
                            config
                            default))]
    (Signal/handle (Signal. "HUP")
      (reify SignalHandler
        (handle [_ _] (do (info "[reconfig] SIGHUP caught")
                          (send-off agt
                                    (fn [old-config]
                                      (if-let [new-config (read-config config-file)]
                                        new-config
                                        old-config)))))))
    agt))

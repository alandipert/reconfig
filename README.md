# reconfig

Reload configuration files in Clojure daemons when the JVM
receives a SIGHUP.

Configuration files are Clojure code, and can contain 
any Clojure data structures.

I've only tested with Sun's JVM 6, and it only works on *nix.

## Usage

If you're using Leiningen, in your project.clj:

    (defproject my-project "1.0.0-SNAPSHOT"
      :dependencies [[reconfig "1.0.4"]])

If you're using Maven, in your pom.xml:

    <dependency>
      <groupId>org.dipert</groupId>
      <artifactId>reconfig</artifactId>
      <version>1.0.4</version>
    </dependency>

And in your code:

    (ns my-ns
      (:use [org.dipert.reconfig :only (reconfig)]))

To store an agent containing your configuration:

    (def config (reconfig "/tmp/lol.clj" {:pants "on"}))

`/tmp/lol.clj` is the path to your configuration file, and
`{:pants "on"}` is the value to use if reading the file fails.

To read configuration data, and assuming `lol.clj` was read
successfully and contains the string `{:pants "off"}`:

    (:pants @config) ;=> "off"

If you write the string `{:pants "on"}` to `lol.clj`
and send your application a SIGHUP:

    (:pants @config) ;=> "on" 
    
## Logging

`reconfig` logs SIGHUPS and the success and failure of file reading
with `tools.logging` at the `info` and `warn` levels.

## Acknowledgements

* The Stuarts for feedback

## License

Copyright (C) 2011 Alan Dipert

Distributed under the Eclipse Public License, the same as Clojure.

(defproject renotebook "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "2.3.1"]
                 ;; Binary parsing
                 [org.clojure/core.match "1.0.0"]
                 [smee/binary "0.5.5"]
                 ;; SSH Connection
                 [clj-ssh "0.5.14"]
                 ;; PDF Creation
                 [clj-pdf "2.5.7"]
                 ]
  :main ^:skip-aot renotebook.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

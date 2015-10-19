(defproject baotask
  "0.1.4-SNAPSHOT"
  :description "Platam of Baoliang"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
    [[org.clojure/clojure "1.6.0"]
     [enlive "1.1.4"]
     [commons-codec/commons-codec "1.9"]
     [commons-io/commons-io "2.4"]
     [commons-lang/commons-lang "2.6"]
     [com.draines/postal "1.11.1"]
     [clabango "0.6-b1"]
     [clj-btc "0.1.1"]
     [clj-time "0.6.0"]
     [compojure "1.1.8"]
     [com.taoensso/timbre "3.1.6"]
     [com.taoensso/carmine "2.6.0"]
     [pawnshop "0.1.0-SNAPSHOT"]
     [com.jolbox/bonecp "0.8.0.RELEASE"]
     [honeysql "0.5.4-SNAPSHOT"]
     [ring-server "0.3.1"]
     [http-kit "2.1.16"]
     [org.clojure/data.json "0.2.4"]
     [cheshire "5.3.1"]
     [org.clojure/java.jdbc "0.3.3"]
     [mysql/mysql-connector-java "5.1.25"]
     [org.postgresql/postgresql "9.4-1201-jdbc41"]
     [cheshire "5.1.1"]
     [lib-noir "0.7.9"]
     [digest "1.4.3"]
     [base64-clj "0.1.1"]
     [com.draines/postal "1.11.1"]
     [uri "1.1.0"]
     [clj-http "1.0.0"]
     [clj-stacktrace "0.2.7"] 
     [fleet "0.10.2"]
     [org.clojure/math.numeric-tower "0.0.4"]
     [hiccup "1.0.5"]
     [net.sf.jlue/jlue-core "1.3"]
     [migratus "0.6.0"]
     [crypto-password "0.1.1"]
     [com.mchange/c3p0 "0.9.2.1"]
     [http-kit "2.1.11"]
     [org.clojure/core.typed "0.2.13"]
     [clojure-csv/clojure-csv "2.0.1"]]
  :plugins
     [[speclj "2.5.0"]]
  :resource-paths ["resources/jar/*"])


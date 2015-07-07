(ns baotask.file
    (:require [digest]
              [clojure.string :as str]
              [clojure.java.io :as io]
              [rui.config :as config]))

(defn calculate-suffix [name]
      (let [p (.lastIndexOf name ".")
            l (count name)]
           (if (>= p 0)
             (if (< p (- l 1))
               (subs name (+ p 1)))
             name)))

(defn calculate-md5 [f]
      (digest/md5 f))

(defn calculate-path [storage md5 suffix]
      (str "/" storage "/" md5 "." (str/lower-case suffix)))

(defn parse-path [path]
      (when-let [[_ storage md5 suffix] (re-matches #"^/([^\/]+)\/([^\.]+)\.(\w+)" path)]
                {:storage storage
                 :md5 (str/replace md5 #"\/" "")
                 :suffix suffix
                 :path path}))

(defn file-by-path [path]
      (io/file (config/file-storage-path) (subs path 1)))

(defn file-exists-by-path [path]
      (let [f (file-by-path path)]
           (if (.exists f)
             f)))

(defn save-file-to-path [file path]
      (when-let [of (file-by-path path)]
                (let [pf (.getParentFile of)]
                     (if-not (.exists pf)
                             (.mkdirs pf)))
                (io/copy file of)
                path))

(defn save-file-to [file filename storage]
      (let [md5 (calculate-md5 file)
            suffix (calculate-suffix filename)
            path (calculate-path storage md5 suffix)]
           (save-file-to-path file path)))


(defn input-by-path [path]
      (when-let [file (file-exists-by-path path)]
                (io/input-stream file)))



(ns runbld.util.io
  (:require [clojure.java.io :as jio]
            [clojure.java.shell :as sh]))

(defn run [& args]
  (let [cmd (map str args)
        res (apply sh/sh cmd)]
    (assert (= 0 (:exit res)) (format "%s: %s"
                                      (pr-str cmd)
                                      (:err res)))))

(defn rmdir-r [dir]
  (run "rm" "-r" dir))

(defn rmdir-rf [dir]
  (run "rm" "-rf" dir))

(defn mkdir-p [dir]
  (run "mkdir" "-p" dir))

(defn abspath [f]
  (.getCanonicalPath (jio/as-file f)))

(defn abspath-file [f]
  (jio/file (abspath f)))

(defn file [& args]
  (apply jio/file args))

(defn spit-stream [^java.io.PrintWriter viewer
                   ^java.io.InputStream input
                   ^java.io.PrintWriter logfile]
  (let [bs (atom 0)]
    (doseq [line (line-seq (jio/reader input))]
      ;; write to the wrapper's inherited IO
      (binding [*out* viewer]
        (println line)
        (flush))

      ;; write to the logfile
      (.println logfile line)
      (.flush logfile)

      ;; update the stats
      (swap! bs + (count line)))
    @bs))

(defn spit-process [out-is out-wtr
                    err-is err-wtr]
  [(future (spit-stream *out* out-is out-wtr))
   (future (spit-stream *err* err-is err-wtr))])

(comment
  (let [pb (doto (ProcessBuilder. ["bash" "run.bash"]))
        proc (.start pb)
        [b1 b2] (future
                  (spit-process
                   (.getInputStream proc)
                   (java.io.PrintWriter. "stdout.txt")
                   (.getErrorStream proc)
                   (java.io.PrintWriter. "stderr.txt")))
        exit-code (.waitFor proc)]
    [exit-code @b1 @b2])


  )

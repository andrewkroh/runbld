(ns runbld.schema
  (:require [schema.core :as s]
            [runbld.scheduler :refer [Scheduler]]
            [runbld.schema.mapping :as m])
  (:import (elasticsearch.connection Connection)))

(def DocType :t)

(def VersionInfo
  {:string s/Str
   :hash   s/Str})

(def Env
  {s/Keyword s/Str})

(def JavaProperties
  {:home     s/Str
   :vendor   s/Str
   :version  s/Str
   :class
   {:path    s/Str}
   :runtime
   {:name    s/Str
    :version s/Str}
   :vm
   {:info    s/Str
    :name    s/Str
    :vendor  s/Str
    :version s/Str}})

(def OptsEmail
  {:from               s/Str
   :host               s/Str
   :max-failure-notify s/Num
   :pass               s/Str
   :port               (s/cond-pre s/Num s/Str)
   :template-html      (s/cond-pre s/Str java.io.File)
   :template-txt       (s/cond-pre s/Str java.io.File)
   :text-only          s/Bool
   :tls                s/Bool
   :to                 (s/cond-pre s/Str [s/Str])
   :user               s/Str
   :disable            s/Bool})

(def OptsSlack
  ;; hook is optional because the other values have defaults
  {(s/optional-key :hook) (s/cond-pre s/Str [s/Str])
   :success            s/Bool
   :failure            s/Bool
   :template           (s/cond-pre s/Str java.io.File)
   :disable            s/Bool})

(def OptsProcess
  {:program           s/Str
   :args              [s/Str]
   :cwd               s/Str
   :scriptfile        s/Str
   :inherit-exit-code s/Bool
   :inherit-env       s/Bool
   :stdout            s/Str
   :stderr            s/Str
   :output            s/Str
   :env               Env})

(def OptsElasticsearch
  {:build-index          s/Str
   :build-index-search   s/Str
   :build-index-write    s/Str
   :failure-index        s/Str
   :failure-index-search s/Str
   :failure-index-write  s/Str
   :log-index            s/Str
   :log-index-search     s/Str
   :log-index-write      s/Str
   :max-index-bytes      s/Num
   :bulk-timeout-ms      s/Num
   :bulk-size            s/Num
   :conn                 Connection
   (s/optional-key :http-opts) {s/Keyword s/Any}
   (s/optional-key :url)       s/Str})

(def OptsS3
  {:access-key s/Str
   :secret-key s/Str
   :bucket     s/Str
   :prefix     s/Str})

(def Opts
  {:job-name   s/Str
   :version    VersionInfo
   :configfile (s/maybe s/Str)
   :email      OptsEmail
   (s/optional-key :slack) OptsSlack
   :es         OptsElasticsearch
   :process    OptsProcess
   :s3         OptsS3
   :java       JavaProperties
   :env        Env})

(def BuildSystem
  {:arch                   s/Str
   (s/optional-key :cpu-type) s/Str
   :cpus                   s/Num
   (s/optional-key :cpus-physical) s/Num
   (s/optional-key :datacenter) s/Str
   :facter-provider        s/Str
   :facter-version         s/Str
   :fs-mountpoint          s/Str
   :fs-type                s/Str
   :fs-bytes-total         s/Num
   :fs-bytes-free          s/Num
   :fs-bytes-used          s/Num
   :fs-percent-free        s/Num
   :fs-percent-used        s/Num
   :provider               s/Str
   :hostname               s/Str
   (s/optional-key :image-id) s/Str
   (s/optional-key :instance-id) s/Str
   (s/optional-key :instance-type) s/Str
   :ip4                    s/Str
   (s/optional-key :ip6)   s/Str
   :kernel-name            s/Str
   :kernel-release         s/Str
   :kernel-version         s/Str
   :model                  s/Str
   :os                     s/Str
   :os-version             s/Str
   :ram-mb                 s/Num
   :ram-gb                 s/Num
   (s/optional-key :ram-bytes) s/Num
   (s/optional-key :region) s/Str
   :timezone               s/Str
   :uptime                 s/Str
   :uptime-days            s/Num
   :uptime-secs            s/Num
   (s/optional-key :virtual) s/Bool})

(def Build
  {:org                       s/Str
   :project                   s/Str
   :branch                    s/Str
   :job-name-extra            s/Str
   :job-name                  s/Str
   :org-project-branch        s/Str
   :scheduler                 s/Str
   :url                       s/Str
   :console-url               s/Str
   :tags                      [s/Str]
   (s/optional-key :number)   s/Str
   (s/optional-key :executor) s/Str
   (s/optional-key :node)     s/Str})

(def OptsWithSys
  (merge Opts {:sys    BuildSystem
               :logger clojure.lang.IFn}))

(def OptsWithEnv
  (merge OptsWithSys {:env Env}))

(def OptsWithJava
  (merge OptsWithEnv {:java JavaProperties}))

(def OptsWithScheduler
  (merge OptsWithJava {:scheduler (s/protocol Scheduler)}))

(def OptsWithBuild
  (merge OptsWithScheduler {:id    s/Str
                            :build Build}))

(def MainOpts
  (merge OptsWithBuild {:vcs {s/Keyword s/Any}}))

(def ProcessResult
  (merge
   {:exit-code      s/Num
    :millis-end     s/Num
    :millis-start   s/Num
    :status         s/Str
    :time-end       s/Str
    :time-start     s/Str
    :took           s/Num

    :cmd            [s/Str]
    :cmd-source     s/Str

    :err-bytes      s/Int
    :out-bytes      s/Int
    :total-bytes    s/Num}))

(def StoredProcessResult
  ProcessResult)

(def VcsLog
  {
   :author-name   s/Str
   :commit-id     s/Str
   :commit-short  s/Str
   :commit-time   s/Str
   :message       s/Str
   :provider      s/Str
   :log-pretty    s/Str
   :project-url   s/Str

   (s/optional-key :branch-url   ) s/Str
   (s/optional-key :commit-url   ) s/Str
   (s/optional-key :author-email ) s/Str
   (s/optional-key :author-time  ) s/Str
   (s/optional-key :commit-email ) s/Str
   (s/optional-key :commit-name  ) s/Str
   (s/optional-key :message-full ) s/Str})

(def XML
  {:tag     s/Keyword
   :attrs   {s/Keyword s/Str}
   :content [s/Any]})

(def FailedTestCase
  {:error-type s/Str
   :class      s/Str
   :test       s/Str
   :stacktrace s/Str
   :summary    s/Str
   :type       s/Str
   (s/optional-key :message) s/Str})

(def TestSummary
  {:errors   s/Num
   :failures s/Num
   :tests    s/Num
   :skipped  s/Num
   :failed-testcases [FailedTestCase]})

(def TestReport
  {:report-has-tests        s/Bool
   (s/optional-key :report) TestSummary})

(def StoredTestSummary
  {:errors   s/Num
   :failures s/Num
   :tests    s/Num
   :skipped  s/Num
   :failed-testcases
   [{:error-type s/Str
     :class      s/Str
     :test       s/Str
     :type       s/Str
     :summary    s/Str
     (s/optional-key :message) s/Str}]})

(def StoredFailure
  (merge
   FailedTestCase
   {:build-id s/Str
    :time     s/Str
    :org      s/Str
    :project  s/Str
    :branch   s/Str}))

(def StoredBuild
  {:id      s/Str
   :version VersionInfo
   :build   Build
   :java    JavaProperties
   :sys     BuildSystem
   :vcs     VcsLog
   :process StoredProcessResult
   :test (s/maybe StoredTestSummary)})

(def StoredBuildIndexSettings
  {:settings
   {:analysis
    {:analyzer m/classpath-analyzer}}
   :mappings
   {DocType
    {:_all {:enabled false}
     :properties
     {:id m/not-analyzed
      :version {:properties
                {:string m/not-analyzed
                 :hash   m/not-analyzed}}
      :build {:properties
              {:org                 m/not-analyzed
               :project             m/not-analyzed
               :branch              m/not-analyzed
               :job-name-extra      m/multi-string
               :job-name            m/multi-string
               :org-project-branch  m/not-analyzed
               :scheduler           m/not-analyzed
               :url                 m/multi-string
               :console-url         m/multi-string
               :tags                m/not-analyzed
               :number              m/not-analyzed
               :executor            m/not-analyzed
               :node                m/not-analyzed}}
      :sys {:properties
            {:arch            m/not-analyzed
             :cpu-type        m/multi-string
             :cpus            m/long
             :cpus-physical   m/long
             :datacenter      m/not-analyzed
             :provider        m/not-analyzed
             :hostname        m/not-analyzed
             :facter-provider m/not-analyzed
             :facter-version  m/not-analyzed
             :fs-mountpoint   m/not-analyzed
             :fs-type         m/not-analyzed
             :fs-bytes-total  m/long
             :fs-bytes-free   m/long
             :fs-bytes-used   m/long
             :fs-percent-free m/double
             :fs-percent-used m/double
             :image-id        m/not-analyzed
             :instance-id     m/not-analyzed
             :instance-type   m/not-analyzed
             :ip4             m/not-analyzed
             :ip6             m/not-analyzed
             :kernel-name     m/not-analyzed
             :kernel-release  m/not-analyzed
             :kernel-version  m/not-analyzed
             :model           m/not-analyzed
             :os              m/not-analyzed
             :os-version      m/not-analyzed
             :ram-mb          m/double
             :ram-gb          m/double
             :region          m/not-analyzed
             :timezone        m/not-analyzed
             :uptime          m/analyzed
             :uptime-days     m/long
             :uptime-secs     m/long
             :virtual         m/boolean}}
      :vcs {:properties
            {:author-name   m/multi-string
             :commit-id     m/not-analyzed
             :commit-short  m/not-analyzed
             :commit-time   m/date
             :message       m/analyzed
             :provider      m/not-analyzed
             :log-pretty    m/analyzed
             :project-url   m/not-analyzed

             :branch-url   m/not-analyzed
             :commit-url   m/not-analyzed
             :author-email m/multi-string
             :author-time  m/date
             :commit-email m/not-analyzed
             :commit-name  m/multi-string
             :message-full m/analyzed}}
      :process {:properties
                {:cmd             m/not-analyzed
                 :cmd-source      m/multi-string
                 :err-accuracy    m/long
                 :err-bytes       m/long
                 :err-file-bytes  m/long
                 :exit-code       m/long
                 :millis-end      m/long
                 :millis-start    m/long
                 :out-accuracy    m/long
                 :out-bytes       m/long
                 :out-file-bytes  m/long
                 :status          m/not-analyzed
                 :time-end        m/date
                 :time-start      m/date
                 :took            m/long}}
      :test {:properties
             {:errors   m/long
              :failures m/long
              :tests    m/long
              :skipped  m/long
              :failed-testcases
              {:properties
               {:error-type m/not-analyzed
                :class      m/not-analyzed
                :test       m/not-analyzed
                :type       m/not-analyzed
                :summary    m/analyzed
                :message    m/multi-string}}}}
      :java {:properties
             {:home      m/multi-string
              :vendor    m/multi-string
              :version   m/multi-string
              :class
              {:properties
               {:path    m/classpath}}
              :runtime
              {:properties
               {:name    m/multi-string
                :version m/multi-string}}
              :vm
              {:properties
               {:info    m/multi-string
                :name    m/multi-string
                :vendor  m/multi-string
                :version m/multi-string}}}}}}}})

(def StoredFailureIndexSettings
  {:mappings
   {DocType
    {:_all {:enabled false}
     :properties
     {:build-id m/not-analyzed}}}})

(def StoredLogLine
  {:build-id s/Str
   :stream   s/Str
   :time     s/Str
   :size     s/Num
   :log      s/Str
   :ord
   {:stream  s/Num
    :total  s/Num}})

(def StoredLogIndexSettings
  {:mappings
   {DocType
    {:_all {:enabled false}
     :properties
     {:build-id m/not-analyzed
      :stream   m/not-analyzed
      :time     m/date
      :log      m/analyzed
      :size     m/long
      :ord
      {:properties
       {:total  m/long
        :stream m/long}}}}}})

(def NotifyCtx
  {:id       s/Str
   :version  VersionInfo
   :build    Build
   :java     JavaProperties
   :sys      BuildSystem
   :vcs      VcsLog
   :process  (merge StoredProcessResult
                    {:cmd s/Str
                     :args s/Str
                     :took-human s/Str
                     :failed s/Bool})
   :test     (s/maybe StoredTestSummary)
   (s/optional-key :failures) [StoredFailure]})

(def EmailCtx
  (merge
   NotifyCtx
   {:email {:to s/Str
            :subject s/Str}}))

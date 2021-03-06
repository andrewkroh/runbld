(ns runbld.notifications.slack
  (:refer-clojure :exclude [send])
  (:require [runbld.schema :refer :all]
            [schema.core :as s]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [runbld.io :as io]
            [runbld.notifications :as n]
            [runbld.store :as store]
            [stencil.core :as mustache]))

(defn api-send
  "Make the Slack REST API call"
  [opts js hook]
  (do
    ((opts :logger) "NOTIFYING SLACK")
    (http/post hook
               {:body js})))

(s/defn send :- s/Any
  "Format and send the Slack notifcation"
  [opts :- MainOpts
   ctx  :- NotifyCtx]
  (let [f     (-> opts :slack :template)
        tmpl  (-> f io/resolve-resource slurp)
        color (if (-> ctx :process :failed)
                "danger"
                "good")
        js    (mustache/render-string tmpl (assoc ctx :color color))
        hooks (-> opts :slack :hook)]
    (if (string? hooks)
      (api-send opts js hooks)
      (doall (map #(api-send opts js %) hooks)))))

(defn send?
  "Determine whether to send a slack alert depending on configs"
  [opts build]
  (let [ec (-> build :process :exit-code)]
    (and
     (or
      (and
       (zero? ec)
       (-> opts :slack :success))
      (and
       (pos? ec)
       (-> opts :slack :failure)))
     (not (-> opts :slack :disable)))))

(defn maybe-send! [opts {:keys [index type id] :as addr}]
  (let [build-doc (store/get (-> opts :es :conn) addr)
        failure-docs (store/get-failures opts (:id build-doc))]
    (when (send? opts build-doc)
      (let [ctx (n/make-context opts build-doc failure-docs)]
        (send opts ctx)))))

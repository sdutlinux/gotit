(ns gotit.common
  (:require [clj-http.client :as chc]
            [net.cgrand.enlive-html :refer [html-resource]]
            [taoensso.carmine :as car :refer [wcar]]

            [gotit.config :as config])

  (:import [java.nio.charset StandardCharsets]
           [java.io ByteArrayInputStream]
           [java.io InputStream]
           [java.util Base64]))

(declare ^:dynamic *yxt-session*)
(declare ^:dynamic *yxt-cookies*)

(def cm (clj-http.conn-mgr/make-reusable-conn-manager
         {:timeout 10 :threads 5 :default-per-route 10}))

(def default-ops {:headers {"User-agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"
                            "Upgrade-Insecure-Requests" "1"
                            "Host" "210.44.176.43"
                            "Connection" "Keep-Alive"}
                  :as "GB2312"
                  :connection-manager cm})

(defmacro wcar* [& body] `(car/wcar config/redis-conn  ~@body))

(defn assoc-session!
  [key value]
  (swap! *yxt-session* assoc key value))

(defn assoc-in-session!
  [keys value]
  (swap! *yxt-session* assoc-in keys value))

(defn clean-session!
  []
  (reset! *yxt-session* {}))

(defn dissoc-session!
  [key]
  (swap! *yxt-session* dissoc key))

(defn merge-zfck!
  [& m]
  (swap! *yxt-session* (fn [c maps]
                         (let [newzfc (apply merge (config/zfck c) maps)]
                           (assoc c config/zfck newzfc))) m))

(defn get-in-session
  [keys]
  (get-in @*yxt-session* keys))

(defn assoc-cookies!
  [key value]
  (swap! *yxt-cookies* assoc key value))

(defn assoc-in-cookies!
  [keys value]
  (swap! *yxt-cookies* assoc-in keys value))

(defn wrap-yxt-sc
  "对 *yxt-cookies* 和 *yxt-session* 进行操作，来修改 :session 和 :cookies字段"
  [handler]
  (fn [request]
    (binding [*yxt-session* (atom (get-in request [:session :yxt] {}))
              *yxt-cookies* (atom {})]
      (when-let [resp (handler request)]
        (-> resp
            (assoc :cookies (merge (:cookies resp) @*yxt-cookies*))
            (assoc :session (:session resp (:session request)))
            (assoc-in [:session :yxt] @*yxt-session*))))))

(defmacro defhandler [name args & body]
  (let [verify (:verify (first body))
        [code verify] (if verify
                        [(rest body) verify]
                        [body (list :else nil)])]
    `(defn ~name [req#]
       (let [{:keys ~args} (:params req#)
             ~'req req#]
         (if-let [error# (cond ~@verify)]
           {:body {:error error#}})
         (do
           ~@code)))))

(defn gtreq
  [method path & [opts]]
  (let [header (get opts :headers {})
        opts (dissoc opts :headers)
        dops (update-in default-ops [:headers] merge header)
        dops (assoc dops :cookies (let [t (get @*yxt-session* config/zfck)
                                        _ (println t)]t))]
    (chc/request (merge dops
                        {:method method
                         :url (format "http://%s/%s" config/zfhost  path)}
                        opts))))

(defn ^InputStream s->dom
  [^String s]
  (with-open [is (ByteArrayInputStream. (.getBytes s StandardCharsets/UTF_8))]
    (html-resource is)))

(defn encode
  [#^Byte s]
  (str "data:image/gif;base64,"(.encodeToString (Base64/getEncoder) s)))

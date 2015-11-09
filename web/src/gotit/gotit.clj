(ns gotit.gotit
  (:require [clj-http.client :as chc]
            [pl.danieljanus.tagsoup :as ts]
            [net.cgrand.enlive-html :as html]
            [gotit.config :as config])
  (:import [java.nio.charset StandardCharsets]
           [java.io ByteArrayInputStream]
           [java.io InputStream]))

(defn gtget
  [url & [opts]]
  (chc/get url
           (merge {:as "GB2312"}
                  opts)))

(defn gtpost
  [url & [opts]]
  (chc/post url
           (merge {:as "GB2312"}
                  opts)))

(defn ^InputStream s->dom
  [^String s]
  (with-open [is (ByteArrayInputStream. (.getBytes s StandardCharsets/UTF_8))]
    (html/html-resource is)))

(defn get-vs
  [body]
  (-> (s->dom body)
      (html/select [[:input (html/attr= :name "__VIEWSTATE")]])
      first
      :attrs
      :value))

(defn get-login-notic
  [body]
  (-> (s->dom body)
      (html/select [[:script (html/attr= :language "javascript" :defer "defer")]])
      first
      :content))

(defn get-index
  []
  (let [resp (gtget (format "http://%s/" config/zfhost))
        code (chc/get (format "http://%s/CheckCode.aspx" config/zfhost
                              {:cookies resp}))]
    resp))

(defn generate-header
  [referer & [opts]]
  (merge
   {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"
    "Referer" referer
    "Upgrade-Insecure-Requests" "1"
    "Origin" "http://210.44.176.43"
    "Host" "210.44.176.43"
    "Connection" "Keep-Alive"}
   opts))

(defn login
  [xh pwd code]
  (let [resp (get-index)
        vs (get-vs (:body resp))
        cookies (:cookies resp)]
    (gtpost "http://210.44.176.43/default2.aspx"
            {:headers (generate-header "http://210.44.176.43/")
             :form-params {:__VIEWSTATE vs
                           :txtUserName xh
                           :TextBox2 pwd
                           :txtSecretCode code
                           :RadioButtonList1 "学生"
                           :Button1 ""
                           :lbLanguage ""
                           :hidPdrs ""
                           :hidsc ""}
             :cookies cookies})))

(defn get-cet
  [number name-]
  (let [data (ts/parse-string
              (:body (chc/get
                      "http://www.chsi.com.cn/cet/query"
                      {:headers {"User-Agent" "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6"
                                 "Referer" "http://www.chsi.com.cn/cet/"}
                       :query-params {"zkzh" number
                                      "xm" name-}})))]
    (-> data
        (get 3)
        (get 6)
        (get 3)
        (get 3)
        (get 3)
        (get 2)
        (get 2)
        (get 2))))

(defmacro defcrawler
  [name params & body]
  (let [[p & body] body]))

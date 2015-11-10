(ns gotit.gotit
  (:require [clj-http.client :as chc]
            [net.cgrand.enlive-html :as html]

            [gotit.common :as gc :refer [defhandler]]
            [gotit.config :as config]))

(defn get-vs
  [body]
  (-> (gc/s->dom body)
      (html/select [[:input (html/attr= :name "__VIEWSTATE")]])
      first
      :attrs
      :value))

(defn get-login-notic
  [body]
  (-> (gc/s->dom body)
      (html/select [[:script (html/attr= :language "javascript" :defer "defer")]])
      first
      :content))

(defn get-captcha
  []
  (let [resp (gc/gtreq :get "CheckCode.aspx" {:as :byte-array})]
    (gc/merge-zfck! (:cookies resp))
    (gc/encode (:body resp))))

(defhandler hh
  [req]
  (format "<img src=\"%s\">" (get-captcha)))

(defhandler login
  [xh pwd code]
  (let [vs (get-vs (:body (gc/gtreq :get "")))]
    (gc/gtreq :post "default2.aspx"
              {:form-params
               {:__VIEWSTATE vs
                :txtUserName xh
                :TextBox2 pwd
                :txtSecretCode code
                :RadioButtonList1 "学生"
                :Button1 ""
                :lbLanguage ""
                :hidPdrs ""
                :hidsc ""}})))

(defmacro defcrawler
  [name params & body]
  (let [[p & body] body]))

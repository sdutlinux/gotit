(ns gotit.gotit
  (:require [clj-http.client :as chc]
            [pl.danieljanus.tagsoup :as ts]))

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

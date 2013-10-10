(ns phantom.core)

(defn open-site [url]
  (let [phantom (js/require "phantom")
        log-page-status-callback (fn [status] (.log js/console (str "opened site? " status)))
        open-page-callback (fn [status] log-page-status-callback)
        create-page-callback (fn [page] (.open page url open-page-callback))
        create-callback (fn [ph] (.createPage ph create-callback))]
    (.create phantom create-callback)))

(defn -main [& args]
  (open-site "http://tilomitra.com/repository/screenscrape/ajax.html"))

(set! *main-cli-fn* -main)


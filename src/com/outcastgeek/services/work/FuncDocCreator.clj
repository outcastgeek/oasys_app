(ns com.outcastgeek.services.work.FuncDocCreator
  (:use clojure.tools.logging)
  (:import java.util.UUID
           java.io.File
           java.io.FileOutputStream
           javax.xml.parsers.DocumentBuilderFactory
           org.xhtmlrenderer.pdf.ITextRenderer))

(defn generatePdfFromHtml [htmlString]
  (debug "Html to transform to PDF: " htmlString)
  (let [pdfFile (-> (File/createTempFile (str (UUID/randomUUID)) ".pdf")
                 (.deleteOnExit))]
    (do
     (-> (ITextRenderer.)
       (.setDocumentFromString htmlString)
       (.layout)
       (.createPDF (FileOutputStream. pdfFile) true))
      pdfFile)
    ))
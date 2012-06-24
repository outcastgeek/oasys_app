(ns com.outcastgeek.services.work.FuncDocExtractor
  (:import java.io.FileInputStream
    java.io.File
    java.io.StringWriter
    org.apache.tika.metadata.Metadata
    org.apache.tika.parser.AutoDetectParser
    org.apache.tika.parser.Parser
    org.apache.tika.parser.ParseContext
    org.apache.tika.parser.html.DefaultHtmlMapper
    org.apache.tika.parser.html.IdentityHtmlMapper
    org.apache.tika.parser.html.HtmlMapper
    org.apache.tika.sax.BodyContentHandler
    javax.xml.transform.OutputKeys
    javax.xml.transform.sax.SAXTransformerFactory
    javax.xml.transform.stream.StreamResult
    org.apache.tika.sax.XHTMLContentHandler
    org.xml.sax.ContentHandler
    org.slf4j.Logger
    org.slf4j.LoggerFactory))

(def loggerPoiDocExtractor (. LoggerFactory getLogger "com.outcastgeek.services.work.PoiDocExtractor"))

(defn extractTextFromDocument
  [this inputFile]
  (def factory
    (. SAXTransformerFactory newInstance))
  (def stringWriter
    (StringWriter.))
  (def contentHandler
    (. factory newTransformerHandler))
  (.setOutputProperty (. contentHandler getTransformer) (OutputKeys/METHOD) "xml")
  (.setOutputProperty (. contentHandler getTransformer) (OutputKeys/INDENT) "yes")
  (. contentHandler setResult (StreamResult. stringWriter))
  (.parse (AutoDetectParser.) (FileInputStream. inputFile) contentHandler (Metadata.) (ParseContext.))
  (def htmlOut
    (.toString stringWriter))
  (.debug loggerPoiDocExtractor htmlOut)
  htmlOut)


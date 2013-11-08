__author__ = 'outcastgeek'

from cStringIO import StringIO
from xhtml2pdf import pisa

from zope.interface import implementer
from pyramid.interfaces import IRenderer


@implementer(IRenderer)
class PDFRenderer(object):
    def __init__(self, **kw):
        self.kw = kw

    def __call__(self, info):
        """ Returns a PDF string with content-type
        ``application/pdf``. The content-type may be overridden by
        setting ``request.response.content_type``."""
        def _render(value, system):
            request = system.get('request')
            if request is not None:
                response = request.response
                ct = response.content_type
                if ct == response.default_content_type:
                    response.content_type = 'application/pdf'
            return self.create_pdf(value)

        return _render

    def create_pdf(self, pdf_data):
        pdf = StringIO()
        pisa.CreatePDF(StringIO(pdf_data.encode('utf-8')), pdf)
        return pdf.getvalue()

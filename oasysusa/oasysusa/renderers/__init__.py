__author__ = 'outcastgeek'

from .pdf_renderer import PDFRenderer

def includeme(config):
    config.scan(__name__)
    config.add_renderer('pdf', PDFRenderer())

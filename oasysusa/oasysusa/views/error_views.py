__author__ = 'outcastgeek'

import logging

from pyramid.view import view_config

logging.basicConfig()
log = logging.getLogger(__file__)

@view_config(context=Exception, renderer='templates/error.jinja2')
# @view_config(context=Exception)
def error_view(exc, request):
    log.error("ERROR::::", exc)
    return dict(error='Things do break you know ...')
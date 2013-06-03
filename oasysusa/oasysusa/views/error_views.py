__author__ = 'outcastgeek'

import logging

from pyramid.view import (
    notfound_view_config,
    view_config,
    )

logging.basicConfig()
log = logging.getLogger(__file__)

@view_config(context=Exception, renderer='templates/error.jinja2')
def error_view(exc, request):
    log.error("ERROR::::", exc)
    request.response_status = '500 Error'
    return dict(error='Error', message='Things do break you know ...')

@notfound_view_config(renderer='templates/error.jinja2')
def not_found(request):
    request.response_status = '404 Not Found'
    return dict(error='Not Found', message='Not Found, dude!')

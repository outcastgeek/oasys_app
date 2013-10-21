__author__ = 'outcastgeek'

from pyramid.view import view_config

import logging

logging.basicConfig()
log = logging.getLogger(__file__)


@view_config(route_name='timesheet',
             renderer='templates/timesheet.jinja2',
             permission='user')
def timesheet(request):
    return dict(request=request)

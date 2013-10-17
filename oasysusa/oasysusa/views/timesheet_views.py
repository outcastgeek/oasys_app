__author__ = 'outcastgeek'

from pyramid.view import view_config
from pyramid.security import authenticated_userid

import logging

logging.basicConfig()
log = logging.getLogger(__file__)


@view_config(route_name='timesheet',
             renderer='templates/timesheet.jinja2',
             permission='user')
def timesheet(request):
    session = request.session
    uniq = session['provider_id']
    username = authenticated_userid(request)

    return dict(logged_in=username, request=request,
                cljs_debug=True if request.registry.settings['cljs_debug'] == 'debug' else False, )

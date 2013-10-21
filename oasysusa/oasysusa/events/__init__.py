__author__ = 'outcastgeek'

from pyramid.events import subscriber, BeforeRender
from pyramid.security import authenticated_userid
from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)


@subscriber(BeforeRender)
def add_globals(event):
    # request = event['request']
    request = get_current_request()
    userID = authenticated_userid(request)
    settings = get_current_registry().settings
    cljs_debug = True if settings['cljs_debug'] == 'debug' else False
    project = 'oasysusa'
    event.update(dict(
        USER_ID=userID,
        project=project,
        cljs_debug=cljs_debug
    ))


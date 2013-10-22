__author__ = 'outcastgeek'

import logging

from beaker.cache import cache_region
from pyramid.events import (
    subscriber,
    BeforeRender,
    ApplicationCreated)
from pyramid.security import authenticated_userid
from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)

from ..models import Group

logging.basicConfig()
log = logging.getLogger(__file__)

@cache_region('long_term', 'settings')
def get_settings():
    settings = get_current_registry().settings
    return settings

def check_before_insert_group(groupname):
    existing_group = Group.by_name(groupname)
    if not existing_group:
        log.info("Adding group %s" % groupname)
        group = Group(groupname)
        group.save()

@subscriber(ApplicationCreated)
def application_created_subscriber(event):
    log.warn('Provisioning the database...')
    map(check_before_insert_group, ['employee', 'admin'])

@subscriber(BeforeRender)
def add_globals(event):
    # request = event['request']
    request = get_current_request()
    session = request.session
    userID = authenticated_userid(request)
    settings = get_settings()
    cljs_debug = True if settings['cljs_debug'] == 'debug' else False
    project = 'oasysusa'
    event.update(dict(
        USER_ID=userID,
        USER_SESSION=session,
        project=project,
        cljs_debug=cljs_debug
    ))


__author__ = 'outcastgeek'

import logging

from beaker.cache import cache_region
from pyramid.events import (
    subscriber,
    BeforeRender,
    ApplicationCreated)
from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)

from ..models import DATE_FORMAT

from .bootstrap import bootstrap_data

logging.basicConfig()
log = logging.getLogger(__file__)


class TemplateUtils(object):
    def __init__(self):
        pass

    def format_date(self, some_date):
        date_string = some_date.strftime(DATE_FORMAT)
        return date_string


@cache_region('long_term', 'template_utils')
def get_template_utils():
    template_utils = TemplateUtils()
    return template_utils


@cache_region('long_term', 'settings')
def get_settings():
    settings = get_current_registry().settings
    return settings


@subscriber(ApplicationCreated)
def application_created_subscriber(event):
    bootstrap_data()


@subscriber(BeforeRender)
def add_globals(event):
    # request = event['request']
    request = get_current_request()
    session = request.session
    userID = session.get('auth.userid')
    settings = get_settings()
    cljs_debug = True if settings['cljs_debug'] == 'debug' else False
    project = 'oasysusa'
    template_utils = get_template_utils()
    event.update(dict(
        request=request,
        USER_ID=userID,
        USER_SESSION=session,
        project=project,
        cljs_debug=cljs_debug,
        TUTILS=template_utils,
    ))


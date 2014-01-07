__author__ = 'outcastgeek'

import logging
import sys
import uuid
import pymongo

from functools import partial
from uuid import uuid5

from beaker.cache import (
    cache_region,
    region_invalidate)

from pyramid.events import (
    subscriber,
    BeforeRender,
    ApplicationCreated,
    NewRequest, ContextFound)

from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)

from pyramid.httpexceptions import HTTPUnauthorized

from ..models import DATE_FORMAT
from ..events.s3 import ensure_s3_bucket

from ..async.srvc_client import (
    srvc_tell,
    srvc_ask
    )

from ..async.srvc_mappings import (
    ENSURE_S3,
    ENSURE_ADMINS
    )

log = logging.getLogger("oasysusa")


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
    region_invalidate(get_settings, 'long_term', 'settings')
    # pass
    registry = get_current_registry()
    settings = registry.settings # do not use the cacheable version during startup
    workers_tcp_address = settings.get('workers_tcp_address')
    srvc_tell(workers_tcp_address, dict(srvc=ENSURE_S3))
    srvc_tell(workers_tcp_address, dict(srvc=ENSURE_ADMINS))


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


@subscriber(ContextFound)
def csrf_validation_event(event):
    request = event.request
    session = request.session
    token = session.get_csrf_token()
    userID = session.get('auth.userid')
    if (request.method == 'POST' or request.is_xhr) and userID and token != request.POST['_csrf']:
        raise HTTPUnauthorized


def setup_client_timesheets(settings):
    mongo_db = settings.get('mongo.db')
    mongo_url = settings.get('mongo.url')
    mongo_conn = pymongo.MongoClient(mongo_url)
    return mongo_conn[mongo_db]['employee_data']['client_timesheets']


def includeme(config):
    settings = config.registry.settings
    try:
        services_tcp_address = settings.get('services_tcp_address')
        workers_tcp_address = settings.get('workers_tcp_address')
        configure_tell = lambda request: partial(srvc_tell, workers_tcp_address)
        config.add_request_method(configure_tell, 'tell', reify=True)
        configure_ask = lambda request: partial(srvc_ask,
                                                '%s_%s' % (request.path, uuid5(uuid.NAMESPACE_DNS, request.path)),
                                                services_tcp_address)
        config.add_request_method(configure_ask, 'ask', reify=True)
        configure_s3conf = lambda request: dict(s3_access_key_id=settings.get('s3_access_key_id'),
                                                s3_secret=settings.get('s3_secret'),
                                                s3_bucket_name=settings.get('s3_bucket_name'))
        config.add_request_method(configure_s3conf, 's3conf', reify=True)
    except:
        e = sys.exc_info()[0]
        log.error("Could not setup zmq client: %s", e)
    configure_client_timesheets = lambda request: setup_client_timesheets(settings)
    config.add_request_method(configure_client_timesheets, 'client_timesheets', reify=True)


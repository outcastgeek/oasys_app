__author__ = 'outcastgeek'

import itertools
import logging

import pymongo
import sys
import transaction
import uuid

from functools import partial
from uuid import uuid5

from beaker.cache import (
    cache_region,
    region_invalidate)

from pyramid.events import (
    subscriber,
    BeforeRender,
    ApplicationCreated,
    NewRequest)

from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)

from ..models import DATE_FORMAT
from ..admin.bootstrap import check_before_insert_user, check_before_insert_group

from ..events.s3 import ensure_s3_bucket

from ..async.srvc_client import (
    srvc_tell,
    srvc_ask
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
    ensure_s3_bucket(settings)

    conn_string = settings.get('sqlalchemy.url')
    # log.warn('The connection string in use is: %s' % conn_string)
    if "sqlite" in conn_string or "localhost" in conn_string:
        log.warn('Provisioning the database...')
        admins = [dict(username='admin', password='OneAdmin13', group='admin')]
        managers = [dict(username='manager', password='ManaJa13', group='manager')]
        with transaction.manager:
            map(check_before_insert_group, ['user', 'employee', 'manager', 'admin'])
            map(lambda user_creds: check_before_insert_user(**user_creds), itertools.chain(admins, managers))


@subscriber(NewRequest)
def add_mongo(event):
    settings = get_settings()
    mongo_db = settings.get('mongo.db')
    mongo_url = settings.get('mongo.url')
    mongo_conn = pymongo.MongoClient(mongo_url)
    request = get_current_request()
    request.client_timesheets = mongo_conn[mongo_db]['employee_data']['client_timesheets']


@subscriber(NewRequest)
def add_zmq_srvc_tools(event):
    try:
        settings = get_settings()
        services_tcp_address = settings.get('services_tcp_address')
        workers_tcp_address = settings.get('workers_tcp_address')
        request = get_current_request()
        identity = '%s_%s' % (request.path, uuid5(uuid.NAMESPACE_DNS, request.path))
        request.tell = partial(srvc_tell, workers_tcp_address)
        request.ask = partial(srvc_ask, identity, services_tcp_address)
        request.s3conf = dict(s3_access_key_id=settings.get('s3_access_key_id'),
                              s3_secret=settings.get('s3_secret'),
                              s3_bucket_name=settings.get('s3_bucket_name'))
    except:
        e = sys.exc_info()[0]
        log.error("Could not setup zmq client: %s", e)


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

__author__ = 'outcastgeek'

import itertools
import logging
import pymongo
import transaction
import zmq

from beaker.cache import (
    cache_region,
    region_invalidate)
from gridfs import GridFS
from pyramid.events import (
    subscriber,
    BeforeRender,
    ApplicationCreated, NewRequest)
from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)

from ..models import DATE_FORMAT

from ..admin.bootstrap import check_before_insert_user, check_before_insert_group

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
    region_invalidate(get_settings, 'long_term', 'settings')
    # pass
    settings = get_current_registry().settings # do not use the cacheable version during startup
    conn_string = settings.get('sqlalchemy.url')
    # log.warn('The connection string in use is: %s' % conn_string)
    if "sqlite"  in conn_string or "localhost" in conn_string:
        log.warn('Provisioning the database...')
        admins = [dict(username='admin', password='OneAdmin13', group='admin')]
        managers = [dict(username='manager', password='ManaJa13', group='manager')]
        with transaction.manager:
            map(check_before_insert_group, ['user', 'employee', 'manager', 'admin'])
            map(lambda user_creds: check_before_insert_user(**user_creds), itertools.chain(admins, managers))


@subscriber(NewRequest)
def add_mongo(event):
    settings = get_settings()
    mongo_url = settings.get('mongo.url')
    mongo_conn = pymongo.MongoClient(mongo_url)
    request = get_current_request()
    request.db = mongo_conn['client_timesheets']
    request.fs = GridFS(request.db)

@subscriber(NewRequest)
def add_s3_zmq_socket(event):
    settings = get_settings()
    s3_tcp_address = settings.get('s3_tcp_address')
    ctx = zmq.Context.instance()
    s3socket = ctx.socket(zmq.REQ)
    s3socket.connect(s3_tcp_address)
    request = get_current_request()
    request.s3socket = s3socket


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

######################## ZMQ Handlers ############################

from s3 import upload_to_s3

zmq_handlers = [dict(address_key='s3_tcp_address', handler=upload_to_s3)]
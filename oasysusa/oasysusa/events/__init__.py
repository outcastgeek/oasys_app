__author__ = 'outcastgeek'

import boto
import itertools
import logging
import pymongo
import transaction
import zmq

from zmq.eventloop import zmqstream

from boto.exception import S3ResponseError

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
    mongo_url = settings.get('mongo.url')
    mongo_conn = pymongo.MongoClient(mongo_url)
    request = get_current_request()
    request.client_timesheets = mongo_conn['employee_data']['client_timesheets']


@subscriber(ApplicationCreated)
def ensure_s3_bucket(event):
    log.info('Setting up s3 bucket...')
    settings = get_current_registry().settings # do not use the cacheable version during startup
    try:
        conn = boto.connect_s3(aws_access_key_id=settings.get('s3_access_key_id'),
                               aws_secret_access_key=settings.get('s3_secret'))
        conn.create_bucket(settings.get('s3_bucket_name'))
        log.info('Done with s3 bucket setup!!!!')
    except S3ResponseError, error:
        log.error("Could not setup s3 bucket: \n%s", error)


@subscriber(NewRequest)
def add_s3_zmq_socket(event):
    settings = get_settings()
    s3_tcp_address = settings.get('s3_tcp_address')
    s3ctx = zmq.Context.instance()
    s = s3ctx.socket(zmq.PUSH)
    s.connect(s3_tcp_address)
    loop = get_current_registry().loop
    s3socket = zmqstream.ZMQStream(s, loop)
    request = get_current_request()
    request.s3ctx = s3ctx
    request.s3socket = s3socket
    request.s3conf = dict(s3_access_key_id=settings.get('s3_access_key_id'),
                          s3_secret=settings.get('s3_secret'),
                          s3_bucket_name=settings.get('s3_bucket_name'))


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

zmq_handlers = [dict(address_key='s3_tcp_address', handler=upload_to_s3, socket_type=zmq.PULL)]
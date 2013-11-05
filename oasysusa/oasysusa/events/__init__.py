__author__ = 'outcastgeek'

import logging
import transaction

from beaker.cache import cache_region
from pyramid.events import (
    subscriber,
    BeforeRender,
    ApplicationCreated)
from pyramid.threadlocal import (
    get_current_registry,
    get_current_request)

from ..models import (
    Group,
    DATE_FORMAT, Employee)

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


def check_before_insert_group(groupname):
    existing_group = Group.query().filter(Group.groupname == groupname).first()
    if not existing_group:
        log.info("Adding group %s" % groupname)
        group = Group(groupname)
        group.save()

def check_before_insert_admin(username, password):
    existing_admin = Employee.query().filter(Employee.username == username).first()
    if not existing_admin:
        log.info("Adding admin (%s, ********)" % username)
        admin_group = Group.query().filter(Group.groupname =="admin").first()
        groups = [admin_group]
        admin = Employee(username=username, password=password, groups=groups)
        admin.save()


@subscriber(ApplicationCreated)
def application_created_subscriber(event):
    log.warn('Provisioning the database...')
    with transaction.manager:
        map(check_before_insert_group, ['user', 'employee', 'admin'])
        map(lambda admin_creds:check_before_insert_admin(**admin_creds), [dict(username='admin', password='OneAdmin13')])


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


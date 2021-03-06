__author__ = 'outcastgeek'

import itertools
import logging
import transaction

from beaker.cache import region_invalidate

from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config

from ..models import (
    Employee,
    Group,
    Project)

from ..api.timesheet_api import get_all_projects

from ..async.srvc_client import srvc_tell

from ..async.srvc_mappings import (
    zmq_service,
    INDEX_ALL_EMPLOYEES,
    GEN_TEST_EMPLOYEES,
    GEN_TEST_EMPLOYEE_TASK,
    DROP_TEST_EMPLOYEES,
    DROP_TEST_EMPLOYEE_TASK,
    SEND_EMAIL_TASK)

log = logging.getLogger('oasysusa')

NUM_OF_PROJECTS=8
NUM_OF_EMPLOYEES=1023

@view_config(route_name='bootstrap_data',
             request_method='POST',
             permission='admin')
def bootstrap_data(request):
    return_to = request.POST.get('return_to')
    log.warn('Provisioning the database...')
    request.tell(dict(srvc=GEN_TEST_EMPLOYEES))
    return HTTPFound(location=return_to)


@view_config(route_name='refresh_search_index',
             request_method='POST',
             permission='admin')
def refresh_search_index(request):
    return_to = request.POST.get('return_to')
    log.warn('Refreshing the search index...')
    request.tell(dict(srvc=INDEX_ALL_EMPLOYEES))
    # request.send_email(["outcastgeek+oasysusa@gmail.com"],
    #            "The employee's search index was just refreshed.",
    #            subject="Employee Index Refresh (DO NOT REPLY)",
    #            sender="donotreply@oasys-corp.com")
    # request.tell(dict(srvc=SEND_EMAIL_TASK,
    #                   recipients=["outcastgeek+oasysusa@gmail.com"],
    #                   body="The employee's search index was just refreshed.",
    #                   subject="Employee Index Refresh (DO NOT REPLY)",
    #                   sender="donotreply@oasys-corp.com"))
    return HTTPFound(location=return_to)


@view_config(route_name='clean_bootstrap_data',
             request_method='POST',
             permission='admin')
def clean_bootstrap_data(request):
    return_to = request.POST.get('return_to')
    request.tell(dict(srvc=DROP_TEST_EMPLOYEES))
    return HTTPFound(location=return_to)

############################# UTILITIES ########################################

@zmq_service(srvc_name='gen_test_employees')
def handle_bootstrap_data(data, settings=None):
    test_projects = gen_test_projects(NUM_OF_PROJECTS)
    with transaction.manager:
        map(lambda proj_info: check_before_insert_project(**proj_info), test_projects)
    test_users = gen_test_users(NUM_OF_EMPLOYEES)
    workers_tcp_address = settings.get('workers_tcp_address')
    map(lambda user_creds: srvc_tell(workers_tcp_address, dict(user_creds=user_creds,
                                                               srvc=GEN_TEST_EMPLOYEE_TASK)), test_users)

@zmq_service(srvc_name='drop_test_employees')
def handle_clean_bootstrap_data(data, settings=None):
    test_users = gen_test_users(NUM_OF_EMPLOYEES)
    workers_tcp_address = settings.get('workers_tcp_address')
    map(lambda user_creds: srvc_tell(workers_tcp_address, dict(user_creds=user_creds,
                                                               srvc=DROP_TEST_EMPLOYEE_TASK)), test_users)
    test_projects = gen_test_projects(NUM_OF_PROJECTS)
    with transaction.manager:
        map(lambda proj_info: check_before_dropping_project(**proj_info), test_projects)

@zmq_service(srvc_name='ensure_admins')
def ensure_admins(data, settings=None):
    conn_string = settings.get('sqlalchemy.url')
    # log.warn('The connection string in use is: %s' % conn_string)
    if "sqlite" in conn_string or "localhost" in conn_string:
        log.warn('Provisioning the database...')
        admins = [dict(username='admin', password='OneAdmin13', group='admin')]
        managers = [dict(username='manager', password='ManaJa13', group='manager')]
        with transaction.manager:
            map(check_before_insert_group, ['user', 'employee', 'manager', 'admin'])
            map(lambda user_creds: check_before_insert_user(**user_creds), itertools.chain(admins, managers))

def check_before_insert_group(groupname):
    existing_group = Group.query().filter(Group.groupname == groupname).first()
    if not existing_group:
        log.info("Adding group %s" % groupname)
        group = Group(groupname)
        group.save()


def check_before_insert_project(**kwargs):
    project_name = kwargs.get('name')
    existing_project = Project.query().filter(Project.name == project_name).first()
    if not existing_project:
        log.info("Adding project %s" % project_name)
        region_invalidate(get_all_projects, 'long_term', 'projects')
        project = Project(**kwargs)
        project.save()


def check_before_dropping_project(**kwargs):
    project_name = kwargs.get('name')
    existing_project = Project.query().filter(Project.name == project_name).first()
    if existing_project:
        region_invalidate(get_all_projects, 'long_term', 'projects')
        existing_project.delete()


@zmq_service(srvc_name='gen_test_employee_task')
def gen_user(data, settings=None):
    user_creds = data.get('user_creds')
    check_before_insert_user(**user_creds)


def check_before_insert_user(username, password, group, **kwargs):
    with transaction.manager:
        existing_user = Employee.query().filter(Employee.username == username).first()
        if not existing_user:
            log.info("Adding user (%s, ********)" % username)
            user_group = Group.query().filter(Group.groupname == group).first()
            groups = [user_group]
            user = Employee(username=username, password=password, groups=groups, **kwargs)
            user.save()


@zmq_service(srvc_name='drop_test_employee_task')
def del_user(data, settings=None):
    user_creds = data.get('user_creds')
    check_before_dropping_user(**user_creds)


def check_before_dropping_user(username, password, group, **kwargs):
    with transaction.manager:
        existing_user = Employee.query().filter(Employee.username == username).first()
        if existing_user:
            existing_user.delete()


def gen_test_projects(count):
    return map(
        lambda index: dict(name='Project%s' % index, client='client%s' % index, description='description%s' % index,
                           email='project%s@projects.com' % index, address='address%s' % index,
                           telephone_number='123-456-7890',
                           manager='manager%s' % index, manager_telephone_number='098-765-4321',
                           manager_email='manager%s@managers.com' % index), xrange(0, count))


def gen_test_users(count):
    return map(
        lambda index: dict(username='Username%s' % index, first_name='NameFirst%s' % index,
                           last_name='NameLast%s' % index, provider='TESTING', provider_id='TESTING',
                           password='WordPass%s' % index, group='employee', email='test%s@employee.com' % index,
                           active=True), xrange(0, count))






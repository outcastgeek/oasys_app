__author__ = 'outcastgeek'

import itertools
import logging

from functools import partial

from beaker.cache import region_invalidate

from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config

from webhelpers.paginate import Page

from sqlalchemy import (
    distinct,
    func
    )

from ..models import (
    Employee,
    Group,
    Project)

from ..api.timesheet_api import get_all_projects

log = logging.getLogger('oasysusa')

@view_config(route_name='bootstrap_data',
             request_method='POST',
             permission='admin')
def bootstrap_data(request):
    return_to = request.POST.get('return_to')
    log.warn('Provisioning the database...')
    test_projects = gen_test_projects(8)
    map(lambda proj_info: check_before_insert_project(**proj_info), test_projects)
    test_users = gen_test_users(31)
    map(lambda user_creds: check_before_insert_user(**user_creds), test_users)
    return HTTPFound(location=return_to)


@view_config(route_name='refresh_search_index',
             request_method='POST',
             permission='admin')
def refresh_search_index(request):
    return_to = request.POST.get('return_to')
    log.warn('Refreshing the search index...')
    # index_all_employees()
    return HTTPFound(location=return_to)


@view_config(route_name='clean_bootstrap_data',
             request_method='POST',
             permission='admin')
def clean_bootstrap_data(request):
    return_to = request.POST.get('return_to')
    test_users = gen_test_users(31)
    map(lambda user_creds: check_before_dropping_user(**user_creds), test_users)
    test_projects = gen_test_projects(8)
    map(lambda proj_info: check_before_dropping_project(**proj_info), test_projects)
    return HTTPFound(location=return_to)

############################# UTILITIES ########################################

# def index_all_employees():
#     # Get lambda
#     get_page = lambda collection, count, i : Page(collection, page=i, item_count=count).items
#     # Get count
#     count = Employee.session().query(func.count(distinct(Employee.username)))
#     # Get page count
#     page_count = Page(Employee, item_count=count).page_count
#     # Get current page partial
#     get_current_page = partial(get_page, Employee, count)
#     itertools.chain.from_iterable(
#         itertools.starmap(lambda i: get_current_page(i),
#                           itertools.islice(xrange(1, page_count + 1), 1, page_count + 1, 1)))

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
        region_invalidate(get_all_projects, 'long_term', 'projects')
        log.info("Adding project %s" % project_name)
        project = Project(**kwargs)
        project.save()


def check_before_dropping_project(**kwargs):
    project_name = kwargs.get('name')
    existing_project = Project.query().filter(Project.name == project_name).first()
    if existing_project:
        region_invalidate(get_all_projects, 'long_term', 'projects')
        existing_project.delete()


def check_before_insert_user(username, password, group, **kwargs):
    existing_user = Employee.query().filter(Employee.username == username).first()
    if not existing_user:
        log.info("Adding user (%s, ********)" % username)
        user_group = Group.query().filter(Group.groupname == group).first()
        groups = [user_group]
        admin = Employee(username=username, password=password, groups=groups, **kwargs)
        admin.save()


def check_before_dropping_user(username, password, group, **kwargs):
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






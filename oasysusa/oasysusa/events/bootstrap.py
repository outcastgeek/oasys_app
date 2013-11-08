__author__ = 'outcastgeek'

import itertools
import logging
import transaction

from beaker.cache import region_invalidate

from ..models import (
    Employee,
    Group,
    Project)

from ..api.timesheet_api import get_all_projects

logging.basicConfig()
log = logging.getLogger(__file__)


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


def check_before_insert_user(username, password, group, **kwargs):
    existing_user = Employee.query().filter(Employee.username == username).first()
    if not existing_user:
        log.info("Adding user (%s, ********)" % username)
        user_group = Group.query().filter(Group.groupname == group).first()
        groups = [user_group]
        admin = Employee(username=username, password=password, groups=groups, **kwargs)
        admin.save()


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
                           last_name='NameLast%s' % index, provider='TESTING',
                           password='WordPass%s' % index, group='employee', email='test%s@employee.com' % index,
                           active=True), xrange(0, count))


def bootstrap_data():
    log.warn('Provisioning the database...')
    with transaction.manager:
        map(check_before_insert_group, ['user', 'employee', 'admin'])
        test_projects = gen_test_projects(8)
        map(lambda proj_info: check_before_insert_project(**proj_info), test_projects)
        test_users = gen_test_users(31)
        admins = [dict(username='admin', password='OneAdmin13', group='admin')]
        map(lambda user_creds: check_before_insert_user(**user_creds),
            itertools.chain(admins, test_users))


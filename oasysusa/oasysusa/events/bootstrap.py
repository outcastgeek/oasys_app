__author__ = 'outcastgeek'

import itertools
import logging
import transaction

from ..models import (
    Group,
    Employee)

logging.basicConfig()
log = logging.getLogger(__file__)


def check_before_insert_group(groupname):
    existing_group = Group.query().filter(Group.groupname == groupname).first()
    if not existing_group:
        log.info("Adding group %s" % groupname)
        group = Group(groupname)
        group.save()


def check_before_insert_user(username, password, group, active=False):
    existing_user = Employee.query().filter(Employee.username == username).first()
    if not existing_user:
        log.info("Adding user (%s, ********)" % username)
        admin_group = Group.query().filter(Group.groupname == group).first()
        groups = [admin_group]
        admin = Employee(username=username, password=password, groups=groups, active=active)
        admin.save()


def gen_test_users(count):
    return map(lambda index: dict(username='user%s' % index, password='WordPass%s' % index, group='employee',
                                  active=True), xrange(0, count))


def bootstrap_data():
    log.warn('Provisioning the database...')
    with transaction.manager:
        map(check_before_insert_group, ['user', 'employee', 'admin'])
        test_users = gen_test_users(31)
        admins = [dict(username='admin', password='OneAdmin13', group='admin')]
        map(lambda admin_creds: check_before_insert_user(**admin_creds),
            itertools.chain(admins, test_users))
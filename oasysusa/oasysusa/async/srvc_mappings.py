__author__ = 'outcastgeek'

import oasysusa
import venusian

ENSURE_S3 = 'ensure_s3'
S3SRVC_UPLOAD = 's3srvc_upload'
S3SRVC_DELETE = 's3srvc_delete'
RECREATE_EMPLOYEE_INDEX = 'recreate_employee_index'
INDEX_ONE_EMPLOYEE = 'index_one_employee'
REINDEX_ONE_EMPLOYEE = 'reindex_one_employee'
BULK_INDEX_NEW_EMPLOYEE = 'bulk_index_new_employee'
INDEX_ALL_EMPLOYEES = 'index_all_employees'
ENSURE_ADMINS = 'ensure_admins'
GEN_TEST_EMPLOYEES = 'gen_test_employees'
GEN_TEST_EMPLOYEE_TASK = 'gen_test_employee_task'
DROP_TEST_EMPLOYEES = 'drop_test_employees'
DROP_TEST_EMPLOYEE_TASK = 'drop_test_employee_task'
SEND_EMAIL_TASK = 'send_email_task'
TIMESHEET_REMINDER_TASK='timesheet_reminder_task'

SRVC_MAP = {}

class zmq_service(object):
    def __init__(self, **settings):
        self.__dict__.update(settings)

    def __call__(self, wrapped):
        settings = self.__dict__.copy()

        def callback(context, name, ob):

            srvc_name = settings.get('srvc_name')
            SRVC_MAP.update({srvc_name:ob})

        venusian.attach(wrapped, callback, category='zmq_services')
        return wrapped

class Registry(object):
    def __init__(self):
        self.registered = []

    def add(self, name, ob):
        self.registered.append((name, ob))

registry = Registry()

def scan_for_zmq_services():
    scanner = venusian.Scanner(registry=registry)
    scanner.scan(oasysusa, categories=('zmq_services',))

__author__ = 'outcastgeek'

import logging
import sys

from functools import partial

from webhelpers.paginate import Page

from ..async.srvc_client import srvc_tell

from ..async.srvc_mappings import (
    BULK_INDEX_NEW_EMPLOYEE,
    RECREATE_EMPLOYEE_INDEX
    )

from ..models import (
    Employee
    )

from ..search import get_es_client

from ..search.indices import (
    EMPLOYEE_INDEX,
    refresh_user_index
    )

from ..async.srvc_mappings import zmq_service

log = logging.getLogger('oasysusa')

@zmq_service(srvc_name='index_one_employee')
def index_new_employee(data, settings=None):
    employee = data.get('employee')
    es = get_es_client(settings)
    es.index(
        EMPLOYEE_INDEX,
        'employee',
        employee.get_data(),
        id=employee.id
    )

@zmq_service(srvc_name='reindex_one_employee')
def index_updated_employee(data, settings=None):
    employee = data.get('employee')
    es = get_es_client(settings)
    es.update(
        EMPLOYEE_INDEX,
        'employee',
        doc=employee.get_data(),
        id=employee.id
    )

def send_page_for_bulk_index(workers_tcp_address, data):
    #gevent.sleep(0.1) # Sleep for a while
    srvc_tell(workers_tcp_address, data)

@zmq_service(srvc_name='index_all_employees')
def handle_index_all_employees(data, settings=None):
    try:
        # Get settings
        workers_tcp_address = settings.get('workers_tcp_address')
        # Get employees collection
        employees_collection = Employee.all(Employee.username)

        # Get count
        # count = Employee.session().query(func.count(distinct(Employee.username)))
        count = employees_collection.count()
        log.debug('\n\nThere are %s employees\n\n', count)
        # Get page count
        page_count = Page(Employee.all(Employee.username), page=1, item_count=count).page_count
        log.debug('\n\nThere are %s pages\n\n', page_count)

        # Refresh the employee index
        srvc_tell(workers_tcp_address, dict(srvc=RECREATE_EMPLOYEE_INDEX))

        send_page_for_bulk_index_partial = partial(send_page_for_bulk_index, workers_tcp_address)

        map(lambda page: send_page_for_bulk_index_partial(dict(srvc=BULK_INDEX_NEW_EMPLOYEE,
                                                               count=count,
                                                               current_page=page)), xrange(1, page_count + 1))

        # itertools.chain.from_iterable(
        #     itertools.starmap(lambda i: get_current_page(i),
        #                       itertools.islice(xrange(1, page_count + 1), 1, page_count + 1, 1)))
    except: # catch *all* exceptions
        e = sys.exc_info()[0]
        log.error("Error: %s, Data: %s" % (e, data))

@zmq_service(srvc_name='bulk_index_new_employee')
def handle_index_new_employee_request(data, settings=None):
    # Get employees collection
    employees_collection = Employee.all(Employee.username)
    # Get current page partial
    count = data.get('count')
    current_page=data.get('current_page')
    try:
        page_items = Page(employees_collection, current_page, item_count=count).items
        employees_data = map(lambda employee: employee.get_data(), page_items)
        es = get_es_client(settings)
        es.bulk_index(
            EMPLOYEE_INDEX,
            'employee',
            employees_data,
            id_field='id'
        )
    except: # catch *all* exceptions
        e = sys.exc_info()[0]
        log.error("Error: %s, Data: %s" % (e, data))



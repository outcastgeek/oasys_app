__author__ = 'outcastgeek'

import gevent
import itertools
import logging
import sys

from functools import partial

from webhelpers.paginate import Page

from pyramid.threadlocal import get_current_registry

from pyramid.events import (
    subscriber,
    ApplicationCreated
    )

from pyelasticsearch.exceptions import ElasticHttpNotFoundError

from ..async.srvc_client import srvc_tell

from ..models import (
    Employee,
    IndexNewEvent,
    IndexUpdateEvent
    )

from ..search import get_es_client

from ..search.indices import (
    EMPLOYEE_INDEX,
    refresh_user_index
    )

log = logging.getLogger('oasysusa')

INDEX_NEW_EMPLOYEE = 'index_new_employee'
INDEX_ALL_EMPLOYEES = 'index_all_employees'

def index_new_employee(employee, settings):
    es = get_es_client(settings)
    es.index(
        EMPLOYEE_INDEX,
        'employee',
        employee.get_data(),
        id=employee.id
    )

def index_updated_employee(employee, settings):
    es = get_es_client(settings)
    es.update(
        EMPLOYEE_INDEX,
        'employee',
        doc=employee.get_data(),
        id=employee.id
    )

@subscriber(ApplicationCreated)
def setup_user_index(event):
    registry = get_current_registry()
    settings = registry.settings # do not use the cacheable version during startup
    refresh_user_index(settings)

@subscriber(IndexUpdateEvent)
def updated_employee_subscriber(event): # TODO: Revisit this!!!!
    registry = get_current_registry()
    settings = registry.settings # do not use the cacheable version during startup
    employee = event.target
    index_updated_employee(employee, settings)

@subscriber(IndexNewEvent)
def new_employee_subscriber(event): # TODO: Revisit this!!!!
    registry = get_current_registry()
    settings = registry.settings # do not use the cacheable version during startup
    employee = event.target
    index_new_employee(employee, settings)

def send_page_for_bulk_index(workers_tcp_address, data):
    gevent.sleep(4) # Sleep for a while
    srvc_tell(workers_tcp_address, data)

def handle_index_all_employees(data, settings=None):
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
    refresh_user_index(settings)

    send_page_for_bulk_index_partial = partial(send_page_for_bulk_index, workers_tcp_address)

    map(lambda page: send_page_for_bulk_index_partial(dict(srvc=INDEX_NEW_EMPLOYEE,
                                                           count=count,
                                                           current_page=page)), xrange(1, page_count + 1))

    # itertools.chain.from_iterable(
    #     itertools.starmap(lambda i: get_current_page(i),
    #                       itertools.islice(xrange(1, page_count + 1), 1, page_count + 1, 1)))

def handle_index_new_employee_request(data, settings=None):
    # Get employees collection
    employees_collection = Employee.all(Employee.username)
    # Get current page partial
    count = data.get('count')
    current_page=data.get('page')
    page_items = Page(employees_collection, current_page, item_count=count).items
    employees_data = map(lambda employee: employee.get_data(), page_items)
    try:
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



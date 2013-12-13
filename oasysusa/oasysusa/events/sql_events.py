__author__ = 'outcastgeek'

import logging

from pyramid.events import (
    subscriber,
    ApplicationCreated
    )

from pyelasticsearch.exceptions import ElasticHttpNotFoundError

from ..models import (
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

def index_new_employee(employee):
    es = get_es_client()
    es.index(
        EMPLOYEE_INDEX,
        'employee',
        employee.get_data(),
        id=employee.id
    )

def index_updated_employee(employee):
    es = get_es_client()
    es.update(
        EMPLOYEE_INDEX,
        'employee',
        doc=employee.get_data(),
        id=employee.id
    )

@subscriber(ApplicationCreated)
def setup_user_index(event):
    refresh_user_index()

@subscriber(IndexUpdateEvent)
def updated_employee_subscriber(event):
    employee = event.target
    index_updated_employee(employee)

@subscriber(IndexNewEvent)
def new_employee_subscriber(event):
    employee = event.target
    index_new_employee(employee)

def handle_index_new_employee_request(data):
    employee = data.get('employee')
    es = get_es_client()
    es.index(
        EMPLOYEE_INDEX,
        'employee',
        employee,
        id=employee.get('id')
    )



__author__ = 'outcastgeek'

import logging

from pyramid.events import (
    subscriber,
    ApplicationCreated
    )

from pyelasticsearch.exceptions import ElasticHttpNotFoundError

from ..models import IndexUpdateEvent

from ..search import (
    get_es_client,
    EMPLOYEE_INDEX,
    employee_mapping
    )

log = logging.getLogger('oasysusa')

@subscriber(ApplicationCreated)
def setup_user_index(event):
    es = get_es_client()
    try:
        es.delete_index(EMPLOYEE_INDEX)
    except ElasticHttpNotFoundError:
        pass

    es.create_index(EMPLOYEE_INDEX)
    es.put_mapping(EMPLOYEE_INDEX, 'employee', employee_mapping)

@subscriber(IndexUpdateEvent)
def application_created_subscriber(event):
    es = get_es_client()
    employee = event.target
    es.index(
        EMPLOYEE_INDEX,
        'employee',
        employee.get_data(),
        id=employee.id
    )



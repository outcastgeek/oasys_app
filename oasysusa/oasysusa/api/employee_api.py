__author__ = 'outcastgeek'

import logging

from datetime import datetime

from pyramid.response import Response
from pyramid.view import (
    view_defaults,
    view_config,
    )

from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from pyramid.httpexceptions import HTTPFound

from pyramid.security import authenticated_userid

from ..models import (
    Employee,
    EmployeeSchema,
    DATE_FORMAT)

logging.basicConfig()
log = logging.getLogger(__file__)

@view_defaults(route_name='employeeapi',
               permission='user',
               renderer='json')
class EmployeeApi(object):

    def __init__(self, request):
        self.request = request

    @view_config(request_method='GET')
    def get(self):
        # return Response('get')
        session = self.request.session
        uniq = session['provider_id']
        existing_employee = Employee.by_provider_id(uniq)
        # return existing_employee
        return {
            "lambert": "lambert"
        }

    @view_config(request_method='POST')
    def post(self):
        return Response('post')

    @view_config(request_method='DELETE')
    def delete(self):
        return Response('delete')

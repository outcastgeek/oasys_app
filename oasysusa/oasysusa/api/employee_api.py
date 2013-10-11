__author__ = 'outcastgeek'

import logging

from pyramid.response import Response
from pyramid.view import (
    view_defaults,
    view_config)

from ..mixins.sqla import Q

from ..models import Employee

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
        session = self.request.session
        uniq = session['provider_id']
        existing_employee = Q(Employee, Employee.provider_id == str(uniq)).first()
        return existing_employee

    @view_config(request_method='POST')
    def post(self):
        return Response('post')

    @view_config(request_method='DELETE')
    def delete(self):
        return Response('delete')

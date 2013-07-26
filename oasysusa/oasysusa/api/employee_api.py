__author__ = 'outcastgeek'

import logging

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
    )

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
        return existing_employee

    @view_config(request_method='POST')
    def post(self):
        return Response('post')

    @view_config(request_method='DELETE')
    def delete(self):
        return Response('delete')

@view_config(route_name='profile',
             renderer='templates/profile.jinja2',
             # request_method='POST',
             permission='user')
def profile(request):
    session = request.session
    uniq = session['provider_id']
    # existing_employee = Employee.by_provider_id(uniq)
    username = authenticated_userid(request)

    form = Form(request,
                schema=EmployeeSchema(),
                obj=Employee())

    if 'submit' in request.POST:
        if form.validate():
            employee = form.bind(Employee())
            log.info("Persisting employee model somewhere...")
            Employee.update(username, employee)
            return HTTPFound(location = request.route_url('home'))
        else:
            log.info('Invalid form...')
            return dict(logged_in = username,
                        renderer=FormRenderer(form))

    existing_employee = Employee.by_username(username)

    if existing_employee:
        existing_employee_form = Form(request,
                                      schema=EmployeeSchema(),
                                      obj=existing_employee)
        return dict(logged_in = username,
                    renderer=FormRenderer(existing_employee_form))



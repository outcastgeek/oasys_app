__author__ = 'outcastgeek'

import logging

from datetime import date

from pyramid.view import (
    view_config,
    view_defaults)

from pyramid.httpexceptions import HTTPFound

from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import (
    Employee, Project)

logging.basicConfig()
log = logging.getLogger(__file__)


@view_defaults(route_name='employees',
               permission='admin')
class EmployeesManagement(object):
    def __init__(self, request):
        self.request = request

    @view_config(request_method='GET',
                 renderer='templates/admin/employees.jinja2')
    def list_employees(self):
        current_page = int(self.request.params.get('page', 1))
        # employees = Employee.get_paginator(request, Employee.last_name, page=int(current_page), items_per_page=1)
        employees = Employee.get_paginator(self.request, Employee.username, page=int(current_page))
        return dict(employees=employees, current_page=current_page,
                    bootstrap_renderer=FormRenderer(Form(self.request, defaults=dict(return_to=self.request.url))),
                    clean_bootstrap_renderer=FormRenderer(Form(self.request, defaults=dict(return_to=self.request.url))))

    @view_config(request_method='POST')
    def update_employee(self):
        current_page = int(self.request.params.get('page', 1))
        project_name = self.request.POST.get('project')
        active = self.request.POST.get('active')
        project = Project.query().filter(Project.name == project_name).first()
        username = self.request.POST.get('username')
        employee = Employee.query().filter(Employee.username == username).first()
        employee.projects.append(project)
        # employee.active = active
        employee.update()
        self.request.session.flash(
            'The %s project was successfully added to %s\' list of projects.' % (project_name, username))
        return HTTPFound(location=self.request.route_url('employees', _query=dict(page=current_page)))

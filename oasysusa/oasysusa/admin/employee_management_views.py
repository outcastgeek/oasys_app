__author__ = 'outcastgeek'

import logging

from pyramid.view import (
    view_config,
    view_defaults)

from pyramid.httpexceptions import HTTPFound

from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import (
    Employee, Project)

from ..search.indices import (
    EMPLOYEE_INDEX,
    gen_employee_query
    )

log = logging.getLogger('oasysusa')


@view_defaults(route_name='employees', permission='admin')
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


@view_defaults(route_name='employees_es', permission='admin')
class EmployeesManagementES(object):
    def __init__(self, request):
        self.request = request

    @view_config(request_method='GET')
    def back_to_employees(self):
        current_page = int(self.request.params.get('page', 1))
        return HTTPFound(location=self.request.route_url('employees', _query=dict(page=current_page)))

    @view_config(request_method='POST', renderer='templates/admin/employees_search.jinja2')
    def find_employee(self):
        query_string = self.request.POST.get('query')
        query = gen_employee_query(query_string)
        employee_res = self.request.es.search(query, index=EMPLOYEE_INDEX, doc_type='employee')
        log.debug("Employee Search Results:\n%s\n", employee_res)
        return dict(employee_res=employee_res,
                    bootstrap_renderer=FormRenderer(Form(self.request, defaults=dict(return_to=self.request.url))),
                    clean_bootstrap_renderer=FormRenderer(Form(self.request, defaults=dict(return_to=self.request.url))))


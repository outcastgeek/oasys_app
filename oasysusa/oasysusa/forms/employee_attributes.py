__author__ = 'outcastgeek'

import logging

from formencode import Schema
from formencode.validators import (
    NotEmpty,
    Bool)

from pyramid.renderers import render
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..api.timesheet_api import (
    get_project_names )


logging.basicConfig()
log = logging.getLogger(__file__)


def form(request, employee, current_page):
    form = Form(request,
                schema=EmployeeAttributesSchema(),
                obj=employee)
    timesheet_report_form = Form(request,
                                 schema=EmployeeAttributesSchema(),
                                 obj=employee)
    project_names = get_project_names()
    response = render('templates/admin/employee_attributes.jinja2',
                      dict(renderer=FormRenderer(form),
                           timesheet_report_renderer=FormRenderer(timesheet_report_form),
                           employee=employee,
                           projects=project_names,
                           current_page=current_page))
    return response


class EmployeeAttributesSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True
    username = NotEmpty
    project = NotEmpty
    active = Bool


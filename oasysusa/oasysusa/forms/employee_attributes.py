__author__ = 'outcastgeek'

import logging

from formencode import Schema
from formencode.validators import (
    NotEmpty,
    Bool)

from pyramid.renderers import render
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import DATE_FORMAT

from ..api.timesheet_api import (
    get_project_names,
    first_and_last_dow)


logging.basicConfig()
log = logging.getLogger(__file__)


def form(request, employee, current_page):
    session = request.session
    current_day = session.get('current_day')
    form = Form(request,
                schema=EmployeeAttributesSchema(),
                obj=employee)
    timesheet_report_form = Form(request,
                                 schema=EmployeeAttributesSchema(),
                                 obj=employee)
    project_names = get_project_names()
    monday, sunday = first_and_last_dow(current_day)
    existing_files_handles = list(request.db.fs.files.find(dict(username=employee.username,
                                                                file_upload_type="Client's Timesheet",
                                                                start=monday.strftime(DATE_FORMAT),
                                                                end=sunday.strftime(DATE_FORMAT))))
    existing_files = map(lambda f: dict(fileid=str(f.get('_id')),
                                        filename=f.get('filename'),
                                        content_type=f.get('content_type')), existing_files_handles)
    response = render('templates/partials/employee_attributes_partial.jinja2',
                      dict(renderer=FormRenderer(form),
                           timesheet_report_renderer=FormRenderer(timesheet_report_form),
                           employee=employee,
                           projects=project_names,
                           current_page=current_page,
                           existing_files=existing_files))
    return response


class EmployeeAttributesSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True
    username = NotEmpty
    project = NotEmpty
    active = Bool


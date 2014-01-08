__author__ = 'outcastgeek'

import logging

from datetime import date

from functools import partial

from formencode import Schema
from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer
from formencode.validators import NotEmpty, DateValidator, UnicodeString

from ..models import Employee

from ..api.timesheet_api import (
    first_and_last_dow,
    get_first_and_last_d_o_m,
    ensure_payroll_cycle,
    ensure_time_sheet,
    save_work_segment,
    get_timesheet_data, TimesheetData)

logging.basicConfig()
log = logging.getLogger(__file__)


@view_config(route_name='timesheet',
             renderer='templates/timesheet.jinja2',
             permission='employee')
def timesheet_form(request):
    session = request.session
    current_day = session.get('current_day')
    if not current_day:
        current_day = date.today()
        session['current_day'] = current_day
    monday, sunday = first_and_last_dow(current_day)
    username = session.get('auth.userid')
    employee = Employee.query().filter(Employee.username == username, Employee.active == True).first()
    if not employee:
        request.session.flash("You need to register first!")
        return HTTPFound(location=request.route_url('register'))
    project_names = map(lambda project: [project.name, "%s by %s" % (project.name, project.client)],
                        employee.projects)
    time_sheet_data = get_timesheet_data(employee, current_day, monday, sunday)
    form = Form(request,
                schema=TimesheetDataSchema(),
                obj=time_sheet_data)
    if 'submit' in request.POST:
        if form.validate():
            time_sheet_data = form.bind(TimesheetData())
            log.info("Persisting timesheet somewhere...")
            first_o_m, last_o_m = get_first_and_last_d_o_m(current_day)
            payroll_cycle = ensure_payroll_cycle(first_o_m, last_o_m)
            time_sheet = ensure_time_sheet(employee, payroll_cycle, monday, sunday, time_sheet_data.description)
            partial_save_work_segment = partial(save_work_segment, time_sheet, payroll_cycle, employee)
            map(partial_save_work_segment, time_sheet_data.work_segments)
            request.session.flash("You successfully updated your time this week!")
            return HTTPFound(location=request.route_url('timesheet'))
        else:
            log.info('Invalid form...')
            request.session.flash("Invalid Timesheet...")
            return dict(renderer=FormRenderer(form), projects=project_names, monday=monday, sunday=sunday)
    return dict(renderer=FormRenderer(form), projects=project_names, monday=monday, sunday=sunday)

class TimesheetDataSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True
    project1 = NotEmpty
    project2 = NotEmpty
    project3 = NotEmpty
    project4 = NotEmpty
    project5 = NotEmpty
    project6 = NotEmpty
    project7 = NotEmpty
    Hours1 = NotEmpty
    Hours2 = NotEmpty
    Hours3 = NotEmpty
    Hours4 = NotEmpty
    Hours5 = NotEmpty
    Hours6 = NotEmpty
    Hours7 = NotEmpty
    Day1 = DateValidator(date_format='mm/dd/yyyy')
    Day2 = DateValidator(date_format='mm/dd/yyyy')
    Day3 = DateValidator(date_format='mm/dd/yyyy')
    Day4 = DateValidator(date_format='mm/dd/yyyy')
    Day5 = DateValidator(date_format='mm/dd/yyyy')
    Day6 = DateValidator(date_format='mm/dd/yyyy')
    Day7 = DateValidator(date_format='mm/dd/yyyy')
    # description = UnicodeString(min=125, max=250)
    description = UnicodeString(min=125)

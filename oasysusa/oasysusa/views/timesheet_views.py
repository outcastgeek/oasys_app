__author__ = 'outcastgeek'

import calendar
import itertools
import logging
import sys

from functools import partial
from datetime import (
    date,
    timedelta)
from formencode import Schema
from beaker.cache import cache_region
from pyramid.httpexceptions import HTTPFound
from pyramid.security import authenticated_userid
from pyramid.view import view_config
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer
from formencode.validators import NotEmpty

from ..mixins.sqla import Q
from ..models import (
    Employee,
    PayrollCycle,
    Project,
    TimeSheet,
    WorkSegment)

logging.basicConfig()
log = logging.getLogger(__file__)


class TimesheetData():
    def __init__(self, Day1=None, Day2=None, Day3=None, Day4=None,
                 Day5=None, Day6=None,
                 Day7=None, project_1=None, project_2=None, project_3=None, project_4=None, project_5=None,
                 project_6=None,
                 project_7=None):
        self.work_segments = [(Day1, project_1), (Day2, project_2), (Day3, project_3),
                              (Day4, project_4), (Day5, project_5),
                              (Day6, project_6), (Day7, project_7)]


class TimesheetDataSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True
    Day1 = NotEmpty
    Day2 = NotEmpty
    Day3 = NotEmpty
    Day4 = NotEmpty
    Day5 = NotEmpty
    Day6 = NotEmpty
    Day7 = NotEmpty
    project_1 = NotEmpty
    project_2 = NotEmpty
    project_3 = NotEmpty
    project_4 = NotEmpty
    project_5 = NotEmpty
    project_6 = NotEmpty
    project_7 = NotEmpty


def first_and_last_dow(day):
    monday = day - timedelta(days=day.weekday())
    sunday = monday + timedelta(days=6)
    return monday, sunday


def get_first_and_last_d_o_m(day):
    year = day.year
    month = day.month
    first_date, last_date = calendar.monthrange(year, month)
    first = date(year, month, first_date)
    last = date(year, month, last_date)
    return first, last


def ensure_payroll_cycle(first_o_m, last_o_m):
    existing_payroll_cycle = Q(PayrollCycle, PayrollCycle.payroll_cycle_number == first_o_m.month).first()
    if existing_payroll_cycle:
        return existing_payroll_cycle
    else:
        payroll_cycle = PayrollCycle(payroll_cycle_number=first_o_m.month, payroll_cycle_year=first_o_m.year,
                                     check_date=last_o_m,
                                     direct_deposit_date=last_o_m, start_date=first_o_m, end_date=last_o_m)
        return payroll_cycle.save()

def ensure_time_sheet(employee, payroll_cycle, monday, sunday):
    existing_time_sheet = Q(TimeSheet, TimeSheet.start_date == monday, TimeSheet.end_date == sunday).first()
    if existing_time_sheet:
        return existing_time_sheet
    else:
        time_sheet = TimeSheet(start_date=monday, end_date=sunday)
        time_sheet.employee_id = employee.id
        time_sheet.payroll_cycle_id = payroll_cycle.id
        return time_sheet.save()


def save_work_segment(time_sheet, payroll_cycle, employee, work_segment_tuple):
    hours, project_name = work_segment_tuple
    project = itertools.ifilter(lambda project: project.name == project_name, get_all_projects()).next()
    work_segment = WorkSegment(hours=hours)
    work_segment.project_id = project.id
    work_segment.time_sheet_id = time_sheet.id
    work_segment.payroll_cycle_id = payroll_cycle.id
    work_segment.employee_id = employee.id
    work_segment.save()


@cache_region('long_term', 'projects')
def get_all_projects():
    projects = Q(Project).all()
    return projects


def get_project_names():
    projects = get_all_projects()
    project_names = map(lambda project: [project.name, "%s by %s" % (project.name, project.client)], projects)
    return project_names


@view_config(route_name='current_day',
             request_method='POST',
             permission='user')
def current_day(request):
    session = request.session
    direction = request.matchdict['direction']
    current_day = session.get('current_day')
    if not current_day:
        current_day = date.today()
    monday, sunday = first_and_last_dow(current_day)

    if 'prev' == direction:
        current_day = monday + timedelta(days=-1)
    if 'next' == direction:
        current_day = sunday + timedelta(days=1)
    session['current_day'] = current_day
    return HTTPFound(location=request.route_url('timesheet'))


@view_config(route_name='timesheet',
             renderer='templates/timesheet.jinja2',
             permission='user')
def timesheet_form(request):
    session = request.session
    project_names = get_project_names()
    current_day = session.get('current_day')
    if not current_day:
        current_day = date.today()
        session['current_day'] = current_day
    monday, sunday = first_and_last_dow(current_day)
    form = Form(request,
                schema=TimesheetDataSchema(),
                obj=TimesheetData())
    if 'submit' in request.POST:
        if form.validate():
            time_sheet_data = TimesheetData(**form.data)
            log.info("Persisting timesheet somewhere...")
            try:
                username = authenticated_userid(request)
                employee = Employee.by_username(username)
                first_o_m, last_o_m = get_first_and_last_d_o_m(current_day)
                payroll_cycle = ensure_payroll_cycle(first_o_m, last_o_m)
                time_sheet = ensure_time_sheet(employee, payroll_cycle, monday, sunday)
                partial_save_work_segment = partial(save_work_segment, time_sheet, payroll_cycle, employee)
                map(partial_save_work_segment, time_sheet_data.work_segments)
                request.session.flash("You successfully updated your time this week!")
            except: # catch *all* exceptions
                e = sys.exc_info()[0]
                request.session.flash("Error: %s" % e.message)
            return HTTPFound(location=request.route_url('timesheet'))
        else:
            log.info('Invalid form...')
            request.session.flash("Invalid Timesheet...")
            return dict(renderer=FormRenderer(form), prev_renderer=FormRenderer(Form(request)),
                        next_renderer=FormRenderer(Form(request)),
                        projects=project_names, monday=monday, sunday=sunday)
    return dict(renderer=FormRenderer(form), prev_renderer=FormRenderer(Form(request)),
                next_renderer=FormRenderer(Form(request)),
                projects=project_names, monday=monday, sunday=sunday)

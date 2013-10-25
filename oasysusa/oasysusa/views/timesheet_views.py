__author__ = 'outcastgeek'

import logging
import sys

from beaker.cache import region_invalidate

from datetime import (
    date,
    timedelta)

from functools import partial

from formencode import Schema
from pyramid.httpexceptions import HTTPFound
from pyramid.security import authenticated_userid
from pyramid.view import view_config
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer
from formencode.validators import NotEmpty

from ..models import (
    Employee,
    WorkSegment)

from ..api.timesheet_api import (
    get_all_projects, first_and_last_dow, get_first_and_last_d_o_m, ensure_payroll_cycle, ensure_time_sheet, save_work_segment, get_all_work_segments_in_range)

logging.basicConfig()
log = logging.getLogger(__file__)


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
    existing_timesheet = get_timesheet(monday, sunday)
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
                region_invalidate(get_all_work_segments_in_range, 'long_term', 'work_segments')
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


################# UTILITIES ########################


def get_project_names():
    projects = get_all_projects()
    project_names = map(lambda project: [project.name, "%s by %s" % (project.name, project.client)], projects)
    return project_names


def get_timesheet(monday, sunday):
    work_segments = get_all_work_segments_in_range(monday, sunday)

    return work_segments


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


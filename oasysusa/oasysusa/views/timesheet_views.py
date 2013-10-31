__author__ = 'outcastgeek'

import itertools
import logging

from datetime import (
    date,
    datetime )

from functools import partial

from formencode import Schema
from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer
from formencode.validators import NotEmpty, DateValidator, UnicodeString

from ..models import (
    DATE_FORMAT,
    Employee,
    TimeSheet )

from ..api.timesheet_api import (
    get_all_projects,
    first_and_last_dow,
    get_first_and_last_d_o_m,
    ensure_payroll_cycle,
    ensure_time_sheet,
    save_work_segment,
    get_all_work_segments_in_range,
    get_week_dates)

logging.basicConfig()
log = logging.getLogger(__file__)


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
    username = session.get('auth.userid')
    employee = Employee.query().filter(Employee.username == username, Employee.active == True).first()
    if not employee:
        request.session.flash("You need to register first!")
        return HTTPFound(location=request.route_url('register'))
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


################# UTILITIES ########################

def get_timesheet_data(employee, current_day, monday, sunday):
    existing_timesheet = get_timesheet(employee, monday, sunday)
    week_dates_map = get_week_dates_map(current_day)
    if len(existing_timesheet) == 0:
        return TimesheetData(**week_dates_map)
    else:
        timesheet = TimeSheet.query().filter(TimeSheet.id == existing_timesheet[0].time_sheet_id,
                                             TimeSheet.employee_id == employee.id).first()
        project = get_project_by_id(existing_timesheet[0].project_id)
        timesheet_metadata = dict(Hours1=existing_timesheet[0].hours if len(existing_timesheet) > 0 else 0,
                                  Hours2=existing_timesheet[1].hours if len(existing_timesheet) > 1 else 0,
                                  Hours3=existing_timesheet[2].hours if len(existing_timesheet) > 2 else 0,
                                  Hours4=existing_timesheet[3].hours if len(existing_timesheet) > 3 else 0,
                                  Hours5=existing_timesheet[4].hours if len(existing_timesheet) > 4 else 0,
                                  Hours6=existing_timesheet[5].hours if len(existing_timesheet) > 5 else 0,
                                  Hours7=existing_timesheet[6].hours if len(existing_timesheet) > 6 else 0,
                                  project=project.name,
                                  description=timesheet.description if timesheet else None)
        default_for_week = dict(week_dates_map.items() + timesheet_metadata.items())
        return TimesheetData(**default_for_week)


def date_string_to_date(date_string):
    dt = datetime.strptime(date_string, DATE_FORMAT) if date_string else None
    return dt


def get_week_dates_map(day):
    week_dates = get_week_dates(day)
    week_dates_string = map(lambda dt: dt.strftime(DATE_FORMAT), week_dates)
    week_dates_map = dict(Day1=week_dates_string[0], Day2=week_dates_string[1], Day3=week_dates_string[2],
                          Day4=week_dates_string[3],
                          Day5=week_dates_string[4], Day6=week_dates_string[5], Day7=week_dates_string[6])
    return week_dates_map


def get_project_by_id(project_id):
    project = itertools.ifilter(lambda project: project.id == project_id, get_all_projects()).next()
    return project


def get_project_names():
    projects = get_all_projects()
    project_names = map(lambda project: [project.name, "%s by %s" % (project.name, project.client)], projects)
    return project_names


def get_timesheet(employee, monday, sunday):
    work_segments = get_all_work_segments_in_range(employee, monday, sunday)
    return work_segments


class TimesheetData():
    def __init__(self, project=None, Hours1=None, Hours2=None, Hours3=None, Hours4=None,
                 Hours5=None, Hours6=None,
                 Hours7=None, Day1=None, Day2=None, Day3=None, Day4=None,
                 Day5=None, Day6=None, Day7=None, description=None):
        self.project = project
        self.Hours1 = Hours1
        self.Hours2 = Hours2
        self.Hours3 = Hours3
        self.Hours4 = Hours4
        self.Hours5 = Hours5
        self.Hours6 = Hours6
        self.Hours7 = Hours7
        self.Day1 = Day1
        self.Day2 = Day2
        self.Day3 = Day3
        self.Day4 = Day4
        self.Day5 = Day5
        self.Day6 = Day6
        self.Day7 = Day7
        self.description = description

    @property
    def work_segments(self):
        return [(self.Hours1, date_string_to_date(self.Day1), self.project),
                (self.Hours2, date_string_to_date(self.Day2), self.project),
                (self.Hours3, date_string_to_date(self.Day3), self.project),
                (self.Hours4, date_string_to_date(self.Day4), self.project),
                (self.Hours5, date_string_to_date(self.Day5), self.project),
                (self.Hours6, date_string_to_date(self.Day6), self.project),
                (self.Hours7, date_string_to_date(self.Day7), self.project)]


class TimesheetDataSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True
    project = NotEmpty
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
    description = UnicodeString(min=125, max=250)


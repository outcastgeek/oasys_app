__author__ = 'outcastgeek'

import calendar
import itertools
import logging
from datetime import (
    date,
    timedelta,
    datetime)

from beaker.cache import cache_region
from pyramid.response import Response
from pyramid.view import (
    view_defaults,
    view_config)

from ..models import (
    PayrollCycle,
    Project,
    TimeSheet,
    WorkSegment, DATE_FORMAT)


logging.basicConfig()
log = logging.getLogger(__name__)


@view_defaults(route_name='project',
               permission='user',
               renderer='json')
class ProjectApi(object):
    def __init__(self, request):
        self.request = request

    @view_config(request_method='GET')
    def get(self):
        projects = Project.query().all()
        return projects

    @view_config(request_method='POST')
    def post(self):
        context = self.request.context
        log.debug(context)
        return Response('post')

    @view_config(request_method='DELETE')
    def delete(self):
        return Response('delete')


@view_defaults(route_name='week',
               permission='user',
               renderer='json')
class WeekApi(object):
    def __init__(self, request):
        self.request = request

    @view_config(request_method='GET')
    def get(self):
        today = date.today()
        monday, sunday = first_and_last_dow(today)
        week = get_all_work_segments_in_range(monday, sunday)
        return week

    @view_config(request_method='POST')
    def post(self):
        context = self.request.context
        log.debug(context)
        return Response('post')

    @view_config(request_method='DELETE')
    def delete(self):
        return Response('delete')

############ UTILITIES ###################

@cache_region('long_term', 'projects')
def get_all_projects():
    projects = Project.query().all()
    return projects


def get_project_names():
    projects = get_all_projects()
    project_names = map(lambda project: [project.name, "%s by %s" % (project.name, project.client)], projects)
    return project_names


def get_project_by_id(project_id):
    project = itertools.ifilter(lambda project: project.id == project_id, get_all_projects()).next()
    return project

# @cache_region('long_term', 'work_segments')
def get_all_work_segments_in_range(employee, start, finish):
    work_segments = WorkSegment.query().filter(WorkSegment.employee_id == employee.id, WorkSegment.date >= start,
                                               WorkSegment.date <= finish).all()
    return work_segments


def first_and_last_dow(day):
    monday = day - timedelta(days=day.weekday())
    sunday = monday + timedelta(days=6)
    return (monday, sunday)


def first_and_last_dow(day):
    monday = day - timedelta(days=day.weekday())
    sunday = monday + timedelta(days=6)
    return monday, sunday


def get_week_dates(day):
    monday = day - timedelta(days=day.weekday())
    week_dates = [monday + timedelta(days=x) for x in range(7)]
    return week_dates


def get_first_and_last_d_o_m(day):
    year = day.year
    month = day.month
    first_date, last_date = calendar.monthrange(year, month)
    first = date(year, month, first_date)
    last = date(year, month, last_date)
    return first, last


def ensure_payroll_cycle(first_o_m, last_o_m):
    existing_payroll_cycle = PayrollCycle.query().filter(PayrollCycle.payroll_cycle_number == first_o_m.month).first()
    if existing_payroll_cycle:
        return existing_payroll_cycle
    else:
        payroll_cycle = PayrollCycle(payroll_cycle_number=first_o_m.month, payroll_cycle_year=first_o_m.year,
                                     check_date=last_o_m,
                                     direct_deposit_date=last_o_m, start_date=first_o_m, end_date=last_o_m)
        return payroll_cycle.save()


def ensure_time_sheet(employee, payroll_cycle, monday, sunday, description):
    existing_time_sheet = TimeSheet.query().filter(TimeSheet.start_date == monday, TimeSheet.end_date == sunday).first()
    if existing_time_sheet:
        return existing_time_sheet.update(description=description)
    else:
        time_sheet = TimeSheet(start_date=monday, end_date=sunday)
        time_sheet.employee_id = employee.id
        time_sheet.payroll_cycle_id = payroll_cycle.id
        time_sheet.description = description
        return time_sheet.save()


def save_work_segment(time_sheet, payroll_cycle, employee, work_segment_tuple):
    hours, date_input, project_name = work_segment_tuple
    project = itertools.ifilter(lambda project: project.name == project_name, get_all_projects()).next()
    work_segment = WorkSegment.query().filter(WorkSegment.date == date_input).first()
    if work_segment:
        return work_segment.update(date=date_input, hours=hours)
    else:
        work_segment = WorkSegment(date=date_input, hours=hours)
        work_segment.project_id = project.id
        work_segment.time_sheet_id = time_sheet.id
        work_segment.payroll_cycle_id = payroll_cycle.id
        work_segment.employee_id = employee.id
        work_segment.save()

################# UTILITIES ########################

def get_timesheet_data(employee, current_day, monday, sunday):
    existing_work_segments = get_work_segments(employee, monday, sunday)
    week_dates_map = get_week_dates_map(current_day)
    if len(existing_work_segments) == 0:
        return TimesheetData(**week_dates_map)
    else:
        timesheet = TimeSheet.query().filter(TimeSheet.id == existing_work_segments[0].time_sheet_id,
                                             TimeSheet.employee_id == employee.id).first()
        projects = map(lambda existing_work_segment: get_project_by_id(existing_work_segment.project_id), existing_work_segments)
        timesheet_metadata = dict(Hours1=existing_work_segments[0].hours if len(existing_work_segments) > 0 else 0,
                                  Hours2=existing_work_segments[1].hours if len(existing_work_segments) > 1 else 0,
                                  Hours3=existing_work_segments[2].hours if len(existing_work_segments) > 2 else 0,
                                  Hours4=existing_work_segments[3].hours if len(existing_work_segments) > 3 else 0,
                                  Hours5=existing_work_segments[4].hours if len(existing_work_segments) > 4 else 0,
                                  Hours6=existing_work_segments[5].hours if len(existing_work_segments) > 5 else 0,
                                  Hours7=existing_work_segments[6].hours if len(existing_work_segments) > 6 else 0,
                                  project1=projects[0].name if len(projects) > 0 else None,
                                  project2=projects[1].name if len(projects) > 1 else None,
                                  project3=projects[2].name if len(projects) > 2 else None,
                                  project4=projects[3].name if len(projects) > 3 else None,
                                  project5=projects[4].name if len(projects) > 4 else None,
                                  project6=projects[5].name if len(projects) > 5 else None,
                                  project7=projects[6].name if len(projects) > 6 else None,
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


def get_work_segments(employee, monday, sunday):
    work_segments = get_all_work_segments_in_range(employee, monday, sunday)
    return work_segments


class TimesheetData():
    def __init__(self, project1=None, project2=None, project3=None, project4=None, project5=None, project6=None,
                 project7=None, Hours1=None, Hours2=None, Hours3=None, Hours4=None,
                 Hours5=None, Hours6=None,
                 Hours7=None, Day1=None, Day2=None, Day3=None, Day4=None,
                 Day5=None, Day6=None, Day7=None, description=None):
        self.project1 = project1
        self.project2 = project2
        self.project3 = project3
        self.project4 = project4
        self.project5 = project5
        self.project6 = project6
        self.project7 = project7
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
        return [(self.Hours1, date_string_to_date(self.Day1), self.project1),
                (self.Hours2, date_string_to_date(self.Day2), self.project2),
                (self.Hours3, date_string_to_date(self.Day3), self.project3),
                (self.Hours4, date_string_to_date(self.Day4), self.project4),
                (self.Hours5, date_string_to_date(self.Day5), self.project5),
                (self.Hours6, date_string_to_date(self.Day6), self.project6),
                (self.Hours7, date_string_to_date(self.Day7), self.project7)]


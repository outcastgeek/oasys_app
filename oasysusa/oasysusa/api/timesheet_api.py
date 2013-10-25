__author__ = 'outcastgeek'

import calendar
import itertools
import logging

from beaker.cache import cache_region

from datetime import (
    date,
    timedelta)

from pyramid.response import Response
from pyramid.view import (
    view_defaults,
    view_config)

from ..mixins.sqla import Q

from ..models import (
    Employee,
    PayrollCycle,
    Project,
    TimeSheet,
    WorkSegment)

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
        projects = Q(Project).all()
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
    projects = Q(Project).all()
    return projects

# @cache_region('long_term', 'work_segments')
def get_all_work_segments_in_range(start, finish):
    work_segments = Q(TimeSheet, TimeSheet.start_date == start, TimeSheet.end_date == finish).all()
    # work_segments = WorkSegment.in_range_inc(start, finish)
    return work_segments

def first_and_last_dow(day):
    monday = day - timedelta(days=day.weekday())
    sunday = monday + timedelta(days=6)
    return (monday, sunday)


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


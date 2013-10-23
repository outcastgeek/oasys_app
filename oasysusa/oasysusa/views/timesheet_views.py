__author__ = 'outcastgeek'

import calendar
import logging
import sys

from datetime import (
    date,
    timedelta)
from formencode import Schema
from beaker.cache import cache_region
from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer
from formencode.validators import (
    NotEmpty)

from ..mixins.sqla import Q
from ..models import (
    PayrollCycle,
    Project,
    WorkSegment, DATE_FORMAT)

logging.basicConfig()
log = logging.getLogger(__file__)


class Timesheet():
    def __init__(self, work_segment_1=None, work_segment_2=None, work_segment_3=None, work_segment_4=None,
                 work_segment_5=None, work_segment_6=None,
                 work_segment_7=None, project_1=None, project_2=None, project_3=None, project_4=None, project_5=None,
                 project_6=None,
                 project_7=None):
        self.work_segments = [(work_segment_1, project_1), (work_segment_2, project_2), (work_segment_3, project_3),
                              (work_segment_4, project_4), (work_segment_5, project_5),
                              (work_segment_6, project_6), (work_segment_7, project_7)]


class TimesheetSchema(Schema):
    work_segment_1 = NotEmpty
    work_segment_2 = NotEmpty
    work_segment_3 = NotEmpty
    work_segment_4 = NotEmpty
    work_segment_5 = NotEmpty
    work_segment_6 = NotEmpty
    work_segment_7 = NotEmpty
    project_1 = NotEmpty
    project_2 = NotEmpty
    project_3 = NotEmpty
    project_4 = NotEmpty
    project_5 = NotEmpty
    project_6 = NotEmpty
    project_7 = NotEmpty


def first_and_last_dow():
    today = date.today()
    monday = today - timedelta(days=today.weekday())
    sunday = monday + timedelta(days=6)
    return (monday, sunday)


def get_first_and_last_d_o_m():
    today = date.today()
    year = today.year
    month = today.month
    first_date, last_date = calendar.monthrange(year, month)
    first = date(year, month, first_date)
    last = date(year, month, last_date)
    return (first, last)


def ensure_payroll_cycle(first_o_m, last_o_m):
    existing_payroll_cycle = Q(PayrollCycle, PayrollCycle.payroll_cycle_number == first_o_m.month).first()
    if existing_payroll_cycle:
        return existing_payroll_cycle
    else:
        payroll_cycle = PayrollCycle(payroll_cycle_number=first_o_m.month, payroll_cycle_year=first_o_m.year,
                                     check_date=last_o_m,
                                     direct_deposit_date=last_o_m, start_date=first_o_m, end_date=last_o_m)
        payroll_cycle.save()
        return payroll_cycle


def save_work_segment(work_segment, project, time_sheet, payroll_cycle, employee):
    work_segment.project_id = project.id
    work_segment.time_sheet_id = time_sheet.id
    work_segment.payroll_cycle_id = payroll_cycle.id
    work_segment.employee_id = employee.id
    work_segment.save()


@cache_region('long_term', 'projects')
def get_all_projects():
    projects = Q(Project).all()
    return projects


@view_config(route_name='timesheet',
             renderer='templates/timesheet.jinja2',
             permission='user')
def timesheet(request):
    projects = get_all_projects()
    monday, sunday = first_and_last_dow()
    monday_s = monday.strftime(DATE_FORMAT)
    sunday_s = sunday.strftime(DATE_FORMAT)
    project_names = map(lambda project: [project.name, "%s by %s" % (project.name, project.client)], projects)
    form = Form(request,
                schema=TimesheetSchema(),
                obj=Timesheet())
    if 'submit' in request.POST:
        if form.validate():
            timesheet = form.bind(Timesheet())
            log.info("Persisting project model somewhere...")
            try:
                work_segment, project, time_sheet, payroll_cycle, employee = None
                map(lambda work_segment, project: save_work_segment(work_segment, project, time_sheet, payroll_cycle,
                                                                    employee),
                    timesheet.worksegments)
                request.session.flash("You successfully updated your time this week!")
            except: # catch *all* exceptions
                e = sys.exc_info()[0]
                request.session.flash("<p>Error: %s</p>" % e)
            return HTTPFound(location=request.route_url('timesheet'), projects=project_names, monday=monday_s,
                             sunday=sunday_s)
        else:
            log.info('Invalid form...')
            request.session.flash("Invalid Project Information...")
            return dict(renderer=FormRenderer(form), projects=project_names, monday=monday_s, sunday=sunday_s)
    return dict(renderer=FormRenderer(form), projects=project_names, monday=monday_s, sunday=sunday_s)

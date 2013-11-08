__author__ = 'outcastgeek'

from datetime import date

from pyramid.view import view_config
from pyramid.renderers import render

from ..models import Employee

from ..api.timesheet_api import get_timesheet_data, first_and_last_dow


@view_config(route_name='timesheet_report',
             renderer='pdf',
             request_method='POST',
             permission='admin')
def timesheet_report(request):
    session = request.session
    current_day = session.get('current_day')
    if not current_day:
        current_day = date.today()
        session['current_day'] = current_day
    monday, sunday = first_and_last_dow(current_day)
    username = request.POST.get('username')
    employee = Employee.query().filter(Employee.username == username).first()
    timesheet_data = get_timesheet_data(employee, current_day, monday, sunday)
    response = render('templates/admin/timesheet_report.jinja2',
                      dict(employee=employee, monday=monday, sunday=sunday, timesheet_data=timesheet_data))
    return response

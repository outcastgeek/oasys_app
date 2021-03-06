__author__ = 'outcastgeek'

import logging
import sys

from datetime import (
    date,
    datetime,
    timedelta)

from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config
from pyramid.renderers import render
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import (
    DATE_FORMAT,
    EARLIEST_DATE)

from ..api.timesheet_api import first_and_last_dow

logging.basicConfig()
log = logging.getLogger(__file__)


def currentday(request, display_range=False):
    session = request.session
    current_day = session.get('current_day')
    if not current_day:
        current_day = date.today()
        session['current_day'] = current_day
    monday, sunday = first_and_last_dow(current_day)
    current_day_str = current_day.strftime(DATE_FORMAT)
    response = render('templates/partials/currentday_partial.jinja2',
                      dict(prev_renderer=FormRenderer(Form(request, defaults=dict(return_to=request.url))),
                           jump_to_date_renderer=FormRenderer(Form(request, defaults=dict(new_date=current_day_str,
                                                                                          return_to=request.url))),
                           next_renderer=FormRenderer(Form(request, defaults=dict(return_to=request.url))),
                           display_range=display_range, monday=monday, sunday=sunday))
    return response


@view_config(route_name='current_day',
             request_method='POST',
             permission='employee')
def current_day(request):
    session = request.session
    return_to = request.POST.get('return_to')
    new_date = request.params.get('new_date')
    direction = request.matchdict.get('direction')
    if 'new_date' == direction and new_date:
        try:
            current_day = datetime.strptime(new_date, DATE_FORMAT)
            current_day = current_day if current_day > EARLIEST_DATE else EARLIEST_DATE
        except:
            e = sys.exc_info()[0]
            request.session.flash("Error: %s" % e)
            return HTTPFound(location=return_to)
    else:
        current_day = session.get('current_day')
        if not current_day:
            current_day = date.today()
        monday, sunday = first_and_last_dow(current_day)

        if 'prev' == direction:
            current_day = monday + timedelta(days=-1)
        if 'next' == direction:
            current_day = sunday + timedelta(days=1)
    session['current_day'] = current_day
    request.session.flash("The current day was changed to %s!!!!" % current_day.strftime(DATE_FORMAT))
    return HTTPFound(location=return_to)


__author__ = 'outcastgeek'

import logging
import datetime

from pyramid.response import Response
from pyramid.view import (
    view_defaults,
    view_config)

from ..mixins.sqla import Q

from ..models import (
    Project,
    WorkSegment)

logging.basicConfig()
log = logging.getLogger(__name__)

def first_and_last_dow(day):
    monday = day - datetime.timedelta(days=day.weekday())
    sunday = monday + datetime.timedelta(days=6)
    return (monday, sunday)

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
        today = datetime.date.today()
        monday, sunday = first_and_last_dow(today)
        week = WorkSegment.in_range_inc(monday, sunday)
        return week

    @view_config(request_method='POST')
    def post(self):
        context = self.request.context
        log.debug(context)
        return Response('post')

    @view_config(request_method='DELETE')
    def delete(self):
        return Response('delete')


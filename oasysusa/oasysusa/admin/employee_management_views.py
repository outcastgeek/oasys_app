__author__ = 'outcastgeek'

import logging
import sys

from beaker.cache import region_invalidate
from pyramid.httpexceptions import HTTPFound
from pyramid.view import view_config
from pyramid.renderers import render
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import (
    ProjectSchema,
    Project, Employee)

from ..views.timesheet_views import get_all_projects

logging.basicConfig()
log = logging.getLogger(__file__)


@view_config(route_name='employees',
             renderer='templates/admin/employees.jinja2',
             permission='admin')
def employees(request):
    current_page = request.matchdict.get('page') or 1
    paginator = Employee.get_paginator(request, Employee.last_name, page=int(current_page), items_per_page=2)
    return dict(paginator=paginator)

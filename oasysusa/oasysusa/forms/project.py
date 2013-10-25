__author__ = 'C148810'

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
    Project)

from ..views.timesheet_views import get_all_projects

logging.basicConfig()
log = logging.getLogger(__file__)

def form(request):
    form = Form(request,
                schema=ProjectSchema(),
                obj=Project())
    response = render('templates/admin/project_partial.jinja2',
                                  dict(renderer=FormRenderer(form), request=request))
    return response.body


@view_config(route_name='project-form',
             renderer='templates/admin/project.jinja2',
             permission='admin')
def project(request):
    form = Form(request,
                schema=ProjectSchema(),
                obj=Project())
    if 'submit' in request.POST:
        if form.validate():
            project = form.bind(Project())
            log.info("Persisting project model somewhere...")
            try:
                Project.save(project)
                region_invalidate(get_all_projects, 'long_term', 'projects')
                request.session.flash("You successfully created project %s" % project.name)
            except: # catch *all* exceptions
                e = sys.exc_info()[0]
                request.session.flash("<p>Error: %s</p>" % e)
            return HTTPFound(location=request.route_url('timesheet'))
        else:
            log.info('Invalid form...')
            request.session.flash("Invalid Project Information...")
            return dict(renderer=FormRenderer(form))
    return dict(renderer=FormRenderer(form))



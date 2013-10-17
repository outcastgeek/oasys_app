__author__ = 'C148810'

from pyramid.view import view_config
from pyramid.security import authenticated_userid
from pyramid.renderers import render_to_response
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import ProjectSchema, Project


def form(request):
    session = request.session
    uniq = session['provider_id']
    # existing_employee = Employee.by_provider_id(uniq)
    username = authenticated_userid(request)

    form = Form(request,
                schema=ProjectSchema(),
                obj=Project())

    response = render_to_response('templates/admin/project.jinja2', dict(renderer=FormRenderer(form), request=request))
    return response.body

@view_config(route_name='project-form',
             request_method='POST',
             permission='user')
def project_submit(request):
    pass


__author__ = 'C148810'

from pyramid.view import view_config
from pyramid.security import authenticated_userid
from pyramid.renderers import render_to_response
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from ..models import ProjectSchema, Project

def initial_form_data(request):
    session = request.session
    uniq = session['provider_id']
    # existing_employee = Employee.by_provider_id(uniq)
    username = authenticated_userid(request)

    form = Form(request,
                schema=ProjectSchema(),
                obj=Project())
    return dict(logged_in =username, renderer=FormRenderer(form), request=request)

def form(request):
    form_response = initial_form_data(request)

    response = render_to_response('templates/admin/project_partial.jinja2', form_response)
    return response.body

@view_config(route_name='project-form',
             renderer='templates/admin/project.jinja2',
             # request_method='POST',
             permission='admin')
def project_submit(request):
    # if 'submit' in request.POST:
    #     pass
    return initial_form_data(request)



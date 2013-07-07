import browserid
import pkg_resources
from pyramid.response import Response

__author__ = 'outcastgeek'

import logging
import json

from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

from pyramid.view import view_config
from pyramid.threadlocal import get_current_registry

from pyramid.httpexceptions import HTTPFound, HTTPBadRequest

from pyramid.view import (
    forbidden_view_config
    )

from pyramid.security import (
    remember,
    forget,
    authenticated_userid,
    )

from ..security import USERS

from ..models import (
    DBSession,
    Employee,
    EmployeeSchema,
    find_employee_by_provider_id,
    )

from velruse import login_url

logging.basicConfig()
log = logging.getLogger(__file__)

# Authentication Stuff

@view_config(route_name='login', renderer='templates/login.jinja2')
@forbidden_view_config(renderer='templates/login.jinja2')
def login(request):
    login_route_url = request.route_url('login')
    referrer = request.url
    session = request.session
    if referrer == login_route_url:
        referrer = '/' # never use the login form itself as came_from
    came_from = request.params.get('came_from', referrer)
    session['came_from'] = came_from
    message = ''
    login = ''
    password = ''
    if 'form.submitted' in request.params:
        login = request.params['login']
        password = request.params['password']
        if USERS.get(login) == password:
            headers = remember(request, login)
            log.info(headers)
            logged_in = authenticated_userid(request)
            log.info(logged_in)
            return HTTPFound(location = came_from,
                             headers = headers)
        message = 'Failed login'

    # providers = request.registry.settings['login_providers']
    providers = get_current_registry().settings['login_providers']
    log.info(providers)

    providers_info = [dict(provider_name=provider_name,
                           login_url=login_url(request, provider_name)) for provider_name in providers]
    log.info(providers_info)

    return dict(
        message = message,
        url = request.application_url + '/login',
        came_from = came_from,
        login = login,
        logged_in = login,
        password = password,
        providers_info = providers_info,
        )

@view_config(
    context='velruse.AuthenticationComplete',
    renderer='templates/register.jinja2')
def login_complete_view(request):
    context = request.context
    session = request.session
    result = {
        'provider_type': context.provider_type,
        'provider_name': context.provider_name,
        'profile': context.profile,
        'credentials': context.credentials,
        }

    message = "Successfully logged in with " + context.provider_name + " of type " + context.provider_type + "!!!!"
    log.info(message)

    result_string = json.dumps(result, indent=4)
    log.info(result_string)

    proceed_url = session['came_from']

    display_name = context.profile['displayName'] if context.profile['displayName'] \
        else context.profile['accounts'][0]['username']
    unique_identifier=context.profile['accounts'][0]['userid']

    headers = remember(request, display_name)
    log.info(headers)

    session['provider_id'] = unique_identifier

    # logged_in = authenticated_userid(request)
    # log.info(logged_in)

    existing_employee = find_employee_by_provider_id(unique_identifier)
    if existing_employee:
        log.debug("Found existing employee: \n")
        log.debug(existing_employee)
        return HTTPFound(location = proceed_url,
                         headers = headers)

    # return dict(message = message,
    #             location = proceed_url,
    #             result = result_string,)

    form = Form(request,
                schema=EmployeeSchema(),
                obj=Employee(username=display_name,
                             email=context.profile['emails'][0]['value'],
                             provider_id=unique_identifier,
                             provider=context.provider_name,))
    if form.validate():
        employee = form.bind(Employee())
        # persist employee model
        DBSession.add(employee)
        return dict(message = message,
                    location = proceed_url,
                    result = result_string,)
    return dict(message = message,
                location = proceed_url,
                result = result_string,
                logged_in = authenticated_userid(request),
                renderer=FormRenderer(form))

@view_config(
    context='velruse.AuthenticationDenied',
    renderer='templates/register.jinja2')
def login_denied_view(request):
    context = request.context
    session = request.session
    log.error(context.reason)
    return {
        'message': context.reason,
        'result': 'denied',
        'proceed_url': session['came_from'],
        }

@view_config(route_name='logout')
def logout(request):
    headers = forget(request)
    return HTTPFound(location = request.route_url('home'),
                     headers = headers)

# Mozilla Persona

# def verify_login(request):
#     """Verifies the assertion and the csrf token in the given request.
#
#     Returns the email of the user if everything is valid, otherwise raises
#     a HTTPBadRequest"""
#     verifier = request.registry['persona.verifier']
#     try:
#         data = verifier.verify(request.POST['assertion'])
#     except (ValueError, browserid.errors.TrustError) as e:
#         log.info('Failed persona login: %s (%s)', e, type(e).__name__)
#         raise HTTPBadRequest('Invalid assertion')
#     return data['email']
#
#
# def login(request):
#     """View to check the persona assertion and remember the user"""
#     email = verify_login(request)
#     request.response.headers.extend(remember(request, email))
#     return {'redirect': request.POST['came_from'], 'success': True}
#
#
# def logout(request):
#     """View to forget the user"""
#     request.response.headers.extend(forget(request))
#     return {'redirect': request.POST['came_from']}
#
#
# def forbidden(request):
#     """A basic 403 view, with a login button"""
#     template = pkg_resources.resource_string('pyramid_persona', 'templates/forbidden.html').decode()
#     html = template % {'js': request.persona_js, 'button': request.persona_button}
#     return Response(html, status='403 Forbidden')


__author__ = 'outcastgeek'

import logging
import json
import uuid

from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer
from pyramid.view import view_config
from pyramid.threadlocal import get_current_registry
from pyramid.httpexceptions import HTTPFound
from pyramid.view import (
    forbidden_view_config
    )
from pyramid.security import (
    remember,
    forget,
    authenticated_userid,
    )
from velruse import login_url

from ..models import (
    Employee,
    EmployeeSchema
    )


logging.basicConfig()
log = logging.getLogger(__file__)

# Authentication Stuff

@view_config(route_name='login', renderer='templates/login.jinja2')
@view_config(route_name='register', renderer='templates/login.jinja2')
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
    if 'submit' in request.POST:
        login = request.params['login']
        password = request.params['password']
        valid_credentials = Employee.check_password(login, password)
        if valid_credentials:
            headers = remember(request, login)
            log.info(headers)
            return HTTPFound(location=came_from,
                             headers=headers)
        message = 'Failed login'
        log.debug(message)

    providers = get_current_registry().settings['login_providers']
    log.info(providers)

    providers_info = [dict(provider_name=provider_name,
                           login_url=login_url(request, provider_name)) for provider_name in providers]
    log.info(providers_info)

    return dict(
        message=message,
        url=request.application_url + '/login',
        came_from=came_from,
        password=password,
        providers_info=providers_info,
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

    display_name = context.profile.get('displayName') if context.profile.get('displayName') \
        else context.profile.get('accounts')[0].get('username')
    unique_identifier = context.profile.get('accounts')[0].get('userid') if context.profile.get('accounts')[0].get('userid') \
        else uuid.uuid5(uuid.NAMESPACE_DNS, context.profile.get('accounts')[0].get('domain'))

    log.debug('Unique Identifier:\n')
    log.debug(unique_identifier)

    headers = remember(request, display_name)
    log.info(headers)

    session['provider_id'] = unique_identifier

    # existing_employee = Employee.by_provider_id(unique_identifier)
    existing_employee = Employee.by_username(display_name)
    if existing_employee:
        log.debug("Found existing employee: \n")
        log.debug(existing_employee)
        return HTTPFound(location=proceed_url,
                         headers=headers)

    # return dict(message = message,
    #             location = proceed_url,
    #             result = result_string,)

    form = Form(request,
                schema=EmployeeSchema(),
                obj=Employee(username=display_name,
                             email=context.profile['emails'][0]['value'],
                             provider_id=unique_identifier,
                             provider=context.provider_name, ))
    if form.validate():
        employee = form.bind(Employee())
        # persist employee model
        log.debug("Saving employee: \n")
        log.debug(employee)
        Employee.save(employee)
        return dict(message=message,
                    location=proceed_url,
                    result=result_string, )
    log.info('Invalid form...')
    return dict(message=message,
                location=proceed_url,
                result=result_string,
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
    return HTTPFound(location=request.route_url('home'),
                     headers=headers)


@view_config(route_name='profile',
             renderer='templates/profile.jinja2',
             # request_method='POST',
             permission='user')
def profile(request):
    session = request.session
    uniq = session['provider_id']
    # existing_employee = Employee.by_provider_id(uniq)
    username = authenticated_userid(request)

    form = Form(request,
                schema=EmployeeSchema(),
                obj=Employee())

    if 'submit' in request.POST:
        if form.validate():
            employee = form.bind(Employee())
            log.info("Persisting employee model somewhere...")
            Employee.update_or_insert(username, employee)

            return HTTPFound(location=request.route_url('home'))
        else:
            log.info('Invalid form...')
            return dict(renderer=FormRenderer(form))

    existing_employee = Employee.by_username(username)

    if existing_employee:
        # existing_employee.date_of_birth = datetime.strftime(existing_employee.date_of_birth, DATE_FORMAT)
        existing_employee_form = Form(request,
                                      schema=EmployeeSchema(),
                                      obj=existing_employee)
        return dict(renderer=FormRenderer(existing_employee_form))
    else:
        return HTTPFound(location=request.route_url('register'))

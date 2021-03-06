__author__ = 'outcastgeek'

import logging
import json
import uuid

from beaker.cache import (
    cache_region)

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
    )
from velruse import login_url

from ..models import (
    Employee,
    EmployeeSchema
    )

from ..errors import ConflictingProfileException

logging.basicConfig()
log = logging.getLogger(__file__)

# Authentication Stuff

@cache_region('long_term', 'providers')
def get_providers():
    providers = get_current_registry().settings['login_providers']
    log.info(providers)
    return providers


@cache_region('long_term', 'providers_info')
def get_providers_info(request):
    providers = get_providers()

    providers_info = [dict(provider_name=provider_name,
                           login_url=login_url(request, provider_name)) for provider_name in providers]
    log.info(providers_info)

    return providers_info


@view_config(route_name='login', renderer='templates/login.jinja2')
@view_config(route_name='register', renderer='templates/login.jinja2')
@forbidden_view_config(renderer='templates/login.jinja2')
def login(request):
    login_route_url = request.route_url('login')
    referrer = request.url
    session = request.session
    if referrer == login_route_url:
        referrer = '/' # never use the login form itself as came_from
    else:
        request.session.flash("You lack the credentials!")
    came_from = request.params.get('came_from', referrer)
    session['came_from'] = came_from
    message = ''
    login = ''
    password = ''
    if 'submit' in request.POST:
        login = request.params.get('login')
        password = request.params.get('password')
        valid_credentials, employee = Employee.check_password(login, password)
        if valid_credentials:
            headers = remember(request, login)
            log.info(headers)
            session['provider_id'] = employee.provider_id
            request.session.flash("Welcome %s!" % login)
            return HTTPFound(location=came_from,
                             headers=headers)
        message = 'Failed login'
        request.session.flash("Failed login!")
        log.debug(message)

    providers_info = get_providers_info(request)

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
    unique_identifier = context.profile.get('accounts')[0].get('userid') if context.profile.get('accounts')[0].get(
        'userid') \
        else uuid.uuid5(uuid.NAMESPACE_DNS, context.profile.get('accounts')[0].get('domain'))

    log.debug('Unique Identifier:\n')
    log.debug(unique_identifier)

    session['provider_id'] = unique_identifier

    headers = remember(request, display_name)
    log.info(headers)

    existing_employee = Employee.query().filter(Employee.username == display_name).first()
    if existing_employee:
        if existing_employee.provider_id == unique_identifier or existing_employee.provider == context.provider_name:
            log.debug("Found existing employee: \n")
            log.debug(existing_employee)
            request.session.flash("Welcome %s!" % display_name)
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
                             provider=context.provider_name))
    if form.validate():
        employee = form.bind(Employee())
        # persist employee model
        log.debug("Saving employee: \n")
        log.debug(employee)
        Employee.save(employee)
        request.session.flash("Your information was successfully updated, %s!" % display_name)
        return dict(message=message,
                    location=proceed_url,
                    result=result_string, )
    log.info('Invalid form...')
    request.session.flash("Invalid form!")
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
    session = request.session
    username = session.get('auth.userid')
    request.session.flash("Goodbye %s!" % username)
    headers = forget(request)
    return HTTPFound(location=request.route_url('home'),
                     headers=headers)


@view_config(route_name='profile',
             renderer='templates/profile.jinja2',
             # request_method='POST',
             permission='user')
def profile(request):
    session = request.session
    username = session.get('auth.userid')
    provider_id = session.get('provider_id')

    form = Form(request,
                schema=EmployeeSchema(),
                obj=Employee())

    if 'submit' in request.POST:
        if form.validate():
            employee = form.bind(Employee())
            log.info("Persisting employee model somewhere...")
            try:
                Employee.update_or_insert(username, employee)
            except ConflictingProfileException, e:
                log.info('Conflicting profile detected...')
                request.session.flash(e.message)
                employee.username = None
                employee.email = None
                conflict_form = Form(request,
                                     schema=EmployeeSchema(),
                                     obj=employee)
                return dict(renderer=FormRenderer(conflict_form))
            request.session.flash('Your profile was successfully updated.')
            return HTTPFound(location=request.route_url('home'))
        else:
            log.info('Invalid form...')
            return dict(renderer=FormRenderer(form))

    existing_employee = Employee.query().filter(Employee.username == username,
                                                Employee.provider_id == str(provider_id)).first()

    if existing_employee:
        existing_employee_form = Form(request,
                                      schema=EmployeeSchema(),
                                      obj=existing_employee)
        return dict(renderer=FormRenderer(existing_employee_form))
    else:
        return HTTPFound(location=request.route_url('register'))

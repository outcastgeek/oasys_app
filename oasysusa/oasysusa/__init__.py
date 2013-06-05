import  logging

from pyramid.config import Configurator
from pyramid.authentication import SessionAuthenticationPolicy
from pyramid.authorization import ACLAuthorizationPolicy

from sqlalchemy import engine_from_config

from .security import groupfinder

from .models import (
    DBSession,
    Base,
    )

logging.basicConfig()
log = logging.getLogger(__file__)

def main(global_config, **settings):
    """ This function returns a Pyramid WSGI application.
    """
    engine = engine_from_config(settings, 'sqlalchemy.')
    DBSession.configure(bind=engine)
    Base.metadata.bind = engine

    # determine which providers we want to configure
    providers = settings.get('login_providers', '')
    providers = filter(None, [p.strip()
                              for line in providers.splitlines()
                              for p in line.split(', ')])
    settings['login_providers'] = providers
    if not any(providers):
        log.warn('no login providers configured, double check your ini '
                 'file and add a few')

    authn_policy = SessionAuthenticationPolicy(callback=groupfinder)
    authz_policy = ACLAuthorizationPolicy()

    config = Configurator(settings=settings,
                          root_factory='oasysusa.models.RootFactory')

    config.set_authentication_policy(authn_policy)
    config.set_authorization_policy(authz_policy)

    if 'github' in providers:
        config.include('velruse.providers.github')
        config.add_github_login_from_settings(prefix='github.')
    if 'google' in providers:
        config.include('velruse.providers.google_oauth2')
        config.add_google_oauth2_login_from_settings(prefix='google.')
    # if 'live' in providers:
    #     config.include('velruse.providers.live')
    #     config.add_live_login_from_settings(prefix='live.')

    config.add_static_view('static', 'static', cache_max_age=3600)
    config.add_route('home', '/')
    config.add_route('login', '/login')
    config.add_route('logout', '/logout')
    config.add_route('profile', '/profile')
    config.add_route('employeeapi', '/employee')

    # config.scan('oasysusa.views')
    config.scan()
    return config.make_wsgi_app()

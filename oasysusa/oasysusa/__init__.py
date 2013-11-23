# from gevent import monkey; monkey.patch_all()
# from psycogreen import gevent; gevent.patch_psycopg()

# import pyximport
#
# pyximport.install(pyimport=True)

import logging

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
    engine = None
    try:
        engine = engine_from_config(settings, 'sqlalchemy.', pool_size=24, max_overflow=0)
    except:
        engine = engine_from_config(settings, 'sqlalchemy.')
    # try:
    #     engine = engine_from_config(settings, 'sqlalchemy.', pool_size=24, max_overflow=0, echo_pool=True, echo=True)
    # except:
    #     engine = engine_from_config(settings, 'sqlalchemy.', echo_pool=True, echo=True)
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

    # scan for config
    config.include('.views.ext')
    config.include('.forms')
    config.include('.renderers')
    config.include('.api', route_prefix='/api')
    config.include('.admin', route_prefix='/admin')

    config.set_authentication_policy(authn_policy)
    config.set_authorization_policy(authz_policy)

    if 'github' in providers:
        config.include('velruse.providers.github')
        config.add_github_login_from_settings(prefix='github.')
    if 'google' in providers:
        config.include('velruse.providers.google_oauth2')
        config.add_google_oauth2_login_from_settings(prefix='google.')
    if 'bitbucket' in providers:
        config.include('velruse.providers.bitbucket')
        config.add_bitbucket_login_from_settings(prefix='bitbucket.')
        # if 'live' in providers:
    #     config.include('velruse.providers.live')
    #     config.add_live_login_from_settings(prefix='live.')

    config.add_static_view('static', 'static', cache_max_age=3600)
    config.add_route('home', '/')
    config.add_route('login', '/login')
    config.add_route('register', '/register')
    config.add_route('logout', '/logout')
    config.add_route('profile', '/profile')
    config.add_route('timesheet', '/timesheet')
    config.add_route('current_day', '/timesheet/{direction}')

    # scan for subscribers
    config.scan('.events')

    # config.scan('oasysusa.views')
    config.scan()
    return config.make_wsgi_app()

######################## RUNNER #########################################

from .async import tornado_paste_server

def serve_paste(app, global_conf, **kw):
    tornado_paste_server.serve_paste(app, global_conf, **kw)




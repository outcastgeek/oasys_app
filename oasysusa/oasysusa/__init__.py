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

from zmq.eventloop import ioloop

ioloop.install() # should be done before any tornado stuff

loop = ioloop.IOLoop.instance()

from tornado import (
    wsgi,
    web,
    httpserver)

from tornado.process import (
    cpu_count,
    fork_processes,
    )

from .async.web import (
    dot,
    TestHandler)

from .async.zmq_tornado_app import ZmqTornadoApp

import threading


def serve_paste(app, global_conf, **kw):
    # Logger objects for internal tornado use
    access_log = logging.getLogger("tornado.access")
    access_log.setLevel(logging.INFO)

    app_log = logging.getLogger("tornado.application")
    app_log.setLevel(logging.INFO)

    gen_log = logging.getLogger("tornado.general")
    gen_log.setLevel(logging.INFO)

    oasysusa_log = logging.getLogger("oasysusa")
    oasysusa_log.setLevel(logging.INFO)

    port = kw.get('port', 6543)
    # Enhance the current app settings
    app.registry.settings = dict(app.registry.settings.items() + kw.items())
    app.registry.loop = loop
    wsgi_app = wsgi.WSGIContainer(app)

    log.info('Starting Custom Tornado server on port: %s' % str(port))

    tornado_app = ZmqTornadoApp(
        [
            (r"/async/web", TestHandler),
            (r'(.*)', web.FallbackHandler, dict(fallback=wsgi_app)),
        ],
        **kw
    )
    tornado_app.setup_zmq_handlers(loop=loop)

    # worker = threading.Thread(target=slow_responder)
    # worker.daemon=True
    # worker.start()

    beat = ioloop.PeriodicCallback(dot, 100)
    beat.start()

    # try:
    #     fork_processes(cpu_count())
    # except:
    #     log.error('Fork is not available on this system, proceeding...')

    http_server = httpserver.HTTPServer(tornado_app,
                                        xheaders=True)
    http_server.listen(port)

    tornado_app.setup_graceful_shutdown(http_server)

    loop.start()
    log.info("Exit...")




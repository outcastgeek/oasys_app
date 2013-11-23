__author__ = 'outcastgeek'

import logging

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

from .web import (
    dot,
    TestHandler)

from .zmq_tornado_app import ZmqTornadoApp

import threading

logging.basicConfig()
log = logging.getLogger(__file__)

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






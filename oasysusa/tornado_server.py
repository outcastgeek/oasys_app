__author__ = 'outcastgeek'

from tornado import (
    wsgi,
    web,
    httpserver,
    ioloop,
)

from tornado.process import (
    cpu_count,
    fork_processes,
)

from tornado.options import (
    define,
    options,
    parse_command_line,
)

from pyramid.paster import (
    get_app,
    get_appsettings,
)

define("props", default='development.ini', help="specify properties file", type=str)

def main():
    parse_command_line()
    props = options.props
    settings = get_appsettings(props)
    # static = settings.get('here') + "/static/"
    port = settings.get('tornado.port')

    app = get_app(props)
    wsgi_app = wsgi.WSGIContainer(app)

    tornado_app = web.Application(
        [
            # (r"/static/(.*)", web.StaticFileHandler, {"path": static}),
            (r'(.*)', web.FallbackHandler, dict(fallback=wsgi_app)),
        ]
    )

    # fork_processes(cpu_count())

    http_server = httpserver.HTTPServer(tornado_app)
    http_server.listen(port)

    ioloop.IOLoop.instance().start()

if __name__ == "__main__":
    main()

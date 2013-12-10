__author__ = 'outcastgeek'

from socketio.server import SocketIOServer

def serve(app, **kw):
    _quiet = kw.pop('_quiet', False)
    _resource = kw.pop('resource', 'socket.io')
    if not _quiet: # pragma: no cover
        # idempotent if logging has already been set up
        import logging
        logging.basicConfig()

    host = kw.pop('host', '127.0.0.1')
    port = int(kw.pop('port', 6543))

    transports = kw.pop('transports', None)
    if transports:
        transports = [x.strip() for x in transports.split(',')]

    policy_server = kw.pop('policy_server', False)
    if policy_server in (True, 'True', 'true', 'enable', 'yes', 'on', '1'):
        policy_server = True
        policy_listener_host = kw.pop('policy_listener_host', host)
        policy_listener_port = int(kw.pop('policy_listener_port', 10843))
        kw['policy_listener'] = (policy_listener_host, policy_listener_port)
    else:
        policy_server = False

    server = SocketIOServer((host, port),
                            app,
                            resource=_resource,
                            transports=transports,
                            policy_server=policy_server,
                            **kw)
    if not _quiet:
        print('serving on http://%s:%s' % (host, port))
    server.serve_forever()


def serve_paste(app, global_conf, **kw):
    """pserve / paster serve / waitress replacement / integration

    You can pass as parameters:

    transports = websockets, xhr-multipart, xhr-longpolling, etc...
    policy_server = True
    """
    serve(app, **kw)
    return 0


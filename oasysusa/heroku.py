__author__ = 'outcastgeek'

from gevent import monkey; monkey.patch_all()

import logging
import os
import sys

from paste.deploy import loadapp
from socketio.server import SocketIOServer

log = logging.getLogger('oasysusa')

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
        log.info('serving on http://%s:%s' % (host, port))

    try:
        server.serve_forever()
    except:
        e = sys.exc_info()[0]
        log.error("Error: %s" % e)
        server.serve_forever()

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    #app = loadapp('config:heroku.ini', relative_to='.')
    app = loadapp('config:development.ini', relative_to=os.path.dirname(os.path.realpath(__file__)))
    print '\n\nApp running on port: %s\n\n' % port
    serve(app, host='0.0.0.0', port=port)

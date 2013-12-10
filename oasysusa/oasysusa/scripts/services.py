__author__ = 'outcastgeek'

import logging
import os
import signal
import sys

from gevent.pool import Pool
from zmq import green as zmq

from pyramid.paster import (
    get_appsettings,
    setup_logging
    )

from oasysusa.async.srvc_handler import handle_msg

log = logging.getLogger('oasysusa')


# Set the maximum pool size for the request handlers
POOL_SIZE = 40000

class Server(object):
    def __init__(self, address):
        self.address = address
        self.pool = Pool(POOL_SIZE)
        self.dead=False

    def stopped(self):
        return self.dead

    def run(self):
        context = zmq.Context()
        frontend = context.socket(zmq.ROUTER)
        frontend.bind(self.address)

        backend = context.socket(zmq.DEALER)
        backend.bind('inproc://backend')

        poll = zmq.Poller()
        poll.register(frontend, zmq.POLLIN)
        poll.register(backend,  zmq.POLLIN)

        while not self.stopped():
            sockets = dict(poll.poll(1000))
            if frontend in sockets:
                if sockets[frontend] == zmq.POLLIN:
                    _id = frontend.recv()
                    msg = frontend.recv()
                    log.debug('Server received message from: %s\n' % _id)
                    self.pool.wait_available()
                    self.pool.spawn(handle_msg, context, _id, msg)

            if backend in sockets:
                if sockets[backend] == zmq.POLLIN:
                    _id = backend.recv()
                    msg = backend.recv()
                    log.debug('Server sending to frontend: %s\n' % _id)
                    frontend.send(_id, zmq.SNDMORE)
                    frontend.send(msg)

        frontend.close()
        backend.close()
        context.term()

        # signal handler
    def sig_handler(self, sig, frame):
        log.warning("Caught Signal: %s", sig)
        self.pool.kill()
        self.dead=True

def usage(argv):
    cmd = os.path.basename(argv[0])
    print('usage: %s <config_uri>\n'
          '(example: "%s development.ini")' % (cmd, cmd))
    sys.exit(1)


def main(argv=sys.argv):
    if len(argv) != 2:
        usage(argv)
    config_uri = argv[1]
    setup_logging(config_uri)
    print "Config location: %s" % config_uri
    settings = get_appsettings(config_uri)

    services_tcp_address=settings.get('services_tcp_address')

    # Start the server that will handle incoming requests
    server = Server(services_tcp_address)
    # signal register
    signal.signal(signal.SIGINT, server.sig_handler)
    signal.signal(signal.SIGTERM, server.sig_handler)
    server.run()

if __name__ == "__main__":
    main()

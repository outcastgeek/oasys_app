__author__ = 'outcastgeek'

from zmq.eventloop import ioloop

ioloop.install() # should be done before any tornado stuff

loop = ioloop.IOLoop.instance()

import logging
import os
import sys
import zmq

from oasysusa.events.s3 import upload_to_s3

logging.basicConfig()
log = logging.getLogger(__file__)

from pyramid.paster import (
    get_appsettings,
    setup_logging
    )


log = logging.getLogger('oasysusa')

################### Lifecycle Events ################################


def setup_zmq_handlers(settings):
    map(lambda handler_info: add_handler(**handler_info),
        [dict(address=settings.get('back_test_zmq_tcp_address'), handler=slow_responder, socket_type=zmq.REP),
         dict(address=settings.get('back_s3_tcp_address'), handler=upload_to_s3, socket_type=zmq.PULL)])


################### END Lifecycle Events #############################


def add_handler(address=None, handler=None, socket_type=None):
    ctx = zmq.Context.instance()
    socket = ctx.socket(socket_type)
    socket.linger = 0
    log.info("Service available here: %s", address)
    socket.connect(address)
    loop.add_handler(socket, handler, zmq.POLLIN)

def slow_responder(socket, events):
    msg = socket.recv()
    # print "\nworker received %r\n" % msg
    # log.info("\nworker received %r\n" % msg)
    # time.sleep(random.randint(1,5))
    # time.sleep(1)
    log.info("Handling request here...")
    socket.send("%s to you too, #%i" % (msg, events))

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

    setup_zmq_handlers(settings)

    loop.start()

if __name__ == "__main__":
    main()

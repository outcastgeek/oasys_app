__author__ = 'outcastgeek'

import itertools
import logging
import random
import time
import zmq

from s3 import upload_to_s3

logging.basicConfig()
log = logging.getLogger(__file__)

################### Lifecycle Events ################################


def setup_zmq_handlers(registry):
    settings = registry.settings
    loop = registry.loop
    zmq_handlers = [dict(address_key='s3_tcp_address', handler=upload_to_s3, socket_type=zmq.PULL)]
    map(lambda handler_info: add_handler(loop, settings, **handler_info),
        itertools.chain(zmq_handlers,
                        [dict(address_key='test_zmq_tcp_address', handler=slow_responder)]))


################### END Lifecycle Events #############################


def add_handler(loop, settings, handler=None, address_key=None, socket_type=zmq.REP):
    ctx = zmq.Context.instance()
    socket = ctx.socket(socket_type)
    socket.linger = 0
    address = settings.get(address_key)
    socket.bind(address)
    loop.add_handler(socket, handler, zmq.POLLIN)

def slow_responder(socket, events):
    msg = socket.recv()
    # print "\nworker received %r\n" % msg
    # log.info("\nworker received %r\n" % msg)
    # time.sleep(random.randint(1,5))
    # time.sleep(1)
    socket.send("%s to you too, #%i" % (msg, events))

__author__ = 'outcastgeek'

import logging
import time
import zmq

from tornado import web

logging.basicConfig()
log = logging.getLogger(__file__)


class ZmqTornadoApp(web.Application):
    def setup_zmq_handlers(self, zmq_tcp_address=None, loop=None, replies=0):
        self.loop = loop
        self.zmq_tcp_address = zmq_tcp_address
        self.replies = replies
        self.add_handler(self.slow_responder)


    def slow_responder(self, socket, events):
        msg = socket.recv()
        # print "\nworker received %r\n" % msg
        log.info("\nworker received %r\n" % msg)
        # time.sleep(random.randint(1,5))
        # time.sleep(1)
        socket.send(msg + " to you too, #%i" % self.replies)
        self.replies += 1


    def add_handler(self, handler):
        ctx = zmq.Context.instance()
        socket = ctx.socket(zmq.REP)
        socket.linger = 0
        socket.bind(self.zmq_tcp_address)
        self.loop.add_handler(socket, handler, zmq.POLLIN)
__author__ = 'outcastgeek'

import itertools
import logging
import zmq

from tornado import web

from ..mixins.shutdown import ShutdownMixin
from ..events import zmq_handlers

logging.basicConfig()
log = logging.getLogger(__file__)


class ZmqTornadoApp(web.Application, ShutdownMixin):

    def setup_zmq_handlers(self, loop=None):
        self.loop = loop
        self.replies = 0
        map(lambda handler_info: self.add_handler(**handler_info),
            itertools.chain(zmq_handlers,
                            [dict(address_key='test_zmq_tcp_address', handler=self.slow_responder)]))


    def add_handler(self, handler=None, address_key=None, socket_type=zmq.REP):
        self.ctx = zmq.Context.instance()
        self.socket = self.ctx.socket(socket_type)
        self.socket.linger = 0
        address = self.settings.get(address_key)
        self.socket.bind(address)
        self.loop.add_handler(self.socket, handler, zmq.POLLIN)

    def slow_responder(self, socket, events):
        msg = socket.recv()
        # print "\nworker received %r\n" % msg
        log.info("\nworker received %r\n" % msg)
        # time.sleep(random.randint(1,5))
        # time.sleep(1)
        socket.send(msg + " to you too, #%i" % self.replies)
        self.replies += 1




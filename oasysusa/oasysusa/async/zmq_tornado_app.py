__author__ = 'outcastgeek'

import logging
import signal
import time
import zmq

from tornado import web

logging.basicConfig()
log = logging.getLogger(__file__)


MAX_WAIT_SECONDS_BEFORE_SHUTDOWN = 10

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
        self.ctx = zmq.Context.instance()
        self.socket = self.ctx.socket(zmq.REP)
        self.socket.linger = 0
        self.socket.bind(self.zmq_tcp_address)
        self.loop.add_handler(self.socket, handler, zmq.POLLIN)

    def setup_graceful_shutdown(self, httpserver=None):
        self.httpserver = httpserver
        # signal register
        signal.signal(signal.SIGINT, self.sig_handler)
        signal.signal(signal.SIGTERM, self.sig_handler)

    # signal handler
    def sig_handler(self, sig, frame):
        log.warning("Caught Signal: %s", sig)
        self.loop.add_callback(self.shutdown)

    # signal handler's callback
    def shutdown(self):
        log.info("Stopping HttpServer...")
        self.httpserver.stop() # no longer accepts new http traffic

        log.info("IOLoop Will be Terminated in %s Seconds...", MAX_WAIT_SECONDS_BEFORE_SHUTDOWN)

        deadline = time.time() + MAX_WAIT_SECONDS_BEFORE_SHUTDOWN

        # recursion for terminate IOLoop.instance()
        def terminate():
            now = time.time()
            if now < deadline and (self.loop._callbacks or self.loop._timeouts):
                self.loop.add_timeout(now + 1, terminate)
            else:
                self.loop.stop() # After process all _callbacks and _timeouts, break IOLoop.instance()
                log.info("Shutdown...")
        # process recursion
        terminate()
        # Terminate ZMQ Socket and Context
        self.socket.close()
        self.ctx.term()

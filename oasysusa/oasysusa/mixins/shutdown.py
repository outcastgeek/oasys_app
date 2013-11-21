__author__ = 'outcastgeek'

import logging
import signal
import time

logging.basicConfig()
log = logging.getLogger(__file__)

MAX_WAIT_SECONDS_BEFORE_SHUTDOWN = 10

class ShutdownMixin(object):
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


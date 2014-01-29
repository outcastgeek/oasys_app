from pyramid.response import Response

__author__ = 'outcastgeek'

import logging
import time
import gevent

from pyramid.view import view_config

from socketio import socketio_manage
from socketio.namespace import BaseNamespace

log = logging.getLogger('oasysusa')


class HelloNamespace(BaseNamespace):
    def initialize(self):
        log.debug("Initializing HelloNamespace ...")
        self.session['speed'] = 1
        self.spawn(self.job_send_hello)

    def job_send_hello(self):
        cnt = 0
        while True:
            cnt += 1
            tm = time.time()
            user = self.request.session.get('auth.userid', 'NoOne')
            self.emit("greeting", "%s: Hello %s!!!!" % (tm, user))
            gevent.sleep(self.session['speed'])


@view_config(route_name='socketio')
def socketio(request):
    retval = socketio_manage(request.environ, {
        "/streaming": HelloNamespace
    }, request=request)
    return Response(retval)


__author__ = 'outcastgeek'

import logging

from tornado import web

from ..mixins.shutdown import ShutdownMixin

logging.basicConfig()
log = logging.getLogger(__file__)


class ZmqTornadoApp(web.Application, ShutdownMixin):
    pass




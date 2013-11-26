__author__ = 'outcastgeek'

from zmq.eventloop import ioloop

ioloop.install() # should be done before any tornado stuff

loop = ioloop.IOLoop.instance()

import logging
import os
import sys
import zmq

from zmq.devices.basedevice import ProcessDevice

log = logging.getLogger('oasysusa')

from pyramid.paster import (
    get_appsettings,
    setup_logging
    )

logging.basicConfig()
log = logging.getLogger(__file__)

def setup_queue(front_addr=None, back_addr=None):
    queuedevice = ProcessDevice(zmq.QUEUE, zmq.XREP, zmq.XREQ)
    queuedevice.bind_in(front_addr)
    queuedevice.bind_out(back_addr)
    # queuedevice.setsockopt_in(zmq.HWM, 1)
    # queuedevice.setsockopt_out(zmq.HWM, 1)
    queuedevice.start()


def setup_streamer(front_addr=None, back_addr=None):
    streamerdevice  = ProcessDevice(zmq.STREAMER, zmq.PULL, zmq.PUSH)
    streamerdevice.bind_in(front_addr)
    streamerdevice.bind_out(back_addr)
    streamerdevice.setsockopt_in(zmq.IDENTITY, 'PULL')
    streamerdevice.setsockopt_out(zmq.IDENTITY, 'PUSH')
    streamerdevice.start()


def setup_queues(settings):
    map(lambda queue_info: setup_queue(**queue_info),
        [dict(front_addr=settings.get('test_zmq_tcp_address'), back_addr=settings.get('back_test_zmq_tcp_address'))])

def setup_streamers(settings):
    map(lambda streamer_info: setup_streamer(**streamer_info),
        [dict(front_addr=settings.get('s3_tcp_address'), back_addr=settings.get('back_s3_tcp_address'))])

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

    setup_queues(settings)
    setup_streamers(settings)

    loop.start()

if __name__ == "__main__":
    main()

__author__ = 'outcastgeek'

import logging
import sys
import umsgpack

from zmq import green as zmq

from ..events.s3 import handle_s3srvc_request

log = logging.getLogger('oasysusa')

def refresh_user_index(msg):
    log.debug('Refreshing user index: %s', msg.get('data'))

S3SRVC='s3srvc'
REFRESH_USER_INDEX='refresh_user_index'

SRVC_MAP = {
    S3SRVC:handle_s3srvc_request,
    REFRESH_USER_INDEX:refresh_user_index
}

def resolve_handler(msg):
    srvc_name = msg.get('srvc')
    log.debug('Resolved Service: %s', srvc_name)
    return SRVC_MAP.get(srvc_name)

def process_msg(raw_msg):
    msg = umsgpack.unpackb(raw_msg)
    srvc_func = resolve_handler(msg)
    return srvc_func(msg)


def handle_msg(context, _id, raw_msg):
    """
    RequestHandler
    :param context: ZeroMQ context
    :param id: Requires the identity frame to include in the reply so that it will be properly routed
    :param msg: Message payload for the worker to process
    """
    # Worker will process the task and then send the reply back to the DEALER backend socket via inproc
    response = None
    try:
        worker = context.socket(zmq.DEALER)
        worker.connect('inproc://backend')
        response = process_msg(raw_msg)
    except:
        e = sys.exc_info()[0]
        log.error("Error: %s" % e)
        response = "Error"

    worker.send(_id, zmq.SNDMORE)
    worker.send(response)

    del raw_msg

    log.debug('Request handler quitting.\n')
    worker.close()



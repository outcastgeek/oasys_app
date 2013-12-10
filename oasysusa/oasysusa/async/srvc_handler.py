__author__ = 'outcastgeek'

import logging
import umsgpack

from zmq import green as zmq

from ..events.s3 import S3Client

log = logging.getLogger('oasysusa')


def handle_s3srvc_request(file_data):
    s3client = S3Client(access_key=file_data.get('s3_access_key_id'),
                        secret_key=file_data.get('s3_secret'),
                        bucket=file_data.get('s3_bucket_name'))
    log.info("Uploading %s to s3", file_data.get('filename'))
    s3client.upload(path=file_data.get('filename'), data=file_data.get('file'))
    return "Done uploading..."

S3SRVC='s3srvc'

SRVC_MAP = {
    S3SRVC:handle_s3srvc_request
}

def resolve_handler(msg):
    srvc_name = msg.get('srvc')
    log.debug('Resolve Service: %s', srvc_name)
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
    worker = context.socket(zmq.DEALER)
    worker.connect('inproc://backend')

    response = process_msg(raw_msg)
    del raw_msg

    worker.send(_id, zmq.SNDMORE)
    worker.send(response)


    log.debug('Request handler quitting.\n')
    worker.close()



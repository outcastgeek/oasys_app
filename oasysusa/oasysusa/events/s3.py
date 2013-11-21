__author__ = 'outcastgeek'

import logging

logging.basicConfig()
log = logging.getLogger(__file__)

def upload_to_s3(socket, events):
    msg = socket.recv()
    log.info("JSON message: %s", msg)
    log.info("Uploading file to s3...\n")


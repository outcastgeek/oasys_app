__author__ = 'outcastgeek'

import logging
import boto

from boto.s3.key import Key
from boto.exception import S3ResponseError

import mimetypes

log = logging.getLogger("oasysusa")


################### Lifecycle Events ################################


def ensure_s3_bucket(settings):
    log.info('Setting up s3 bucket...')
    try:
        conn = boto.connect_s3(aws_access_key_id=settings.get('s3_access_key_id'),
                               aws_secret_access_key=settings.get('s3_secret'))
        conn.create_bucket(settings.get('s3_bucket_name'))
        log.info('Done with s3 bucket setup!!!!')
    except S3ResponseError, error:
        log.error("Could not setup s3 bucket: \n%s", error)


################### END Lifecycle Events #############################

def handle_s3srvc_request(file_data):
    log.info("Uploading %s to s3", file_data.get('filename'))
    type, encoding = mimetypes.guess_type(file_data.get('filename'))
    type = type or 'application/octet-stream'
    headers = { 'Content-Type': type, 'X-Amz-Acl': 'public-read' }
    conn = boto.connect_s3(aws_access_key_id = file_data.get('s3_access_key_id'),
                           aws_secret_access_key = file_data.get('s3_secret'))
    bucket = conn.get_bucket(file_data.get('s3_bucket_name'))
    key = Key(bucket)
    key.key = file_data.get('filename')
    key.set_contents_from_string(file_data.get('file'), headers)
    return "Done uploading..."

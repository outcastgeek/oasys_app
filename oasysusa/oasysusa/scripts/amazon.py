__author__ = 'outcastgeek'

import sys
import os
import boto.ec2

from sqlalchemy import engine_from_config

from pyramid.paster import (
    get_appsettings,
    setup_logging,
    )


def create_instance(region="us-east-1",
                    ami_image_id="ami-cbb593a2",
                    instance_type="m1.small",
                    aws_access_key_id=None,
                    aws_secret_access_key=None,
                    dry_run=True):
    regions = boto.ec2.regions()
    print regions
    conn = boto.ec2.connect_to_region(region,
                                      aws_access_key_id=aws_access_key_id,
                                      aws_secret_access_key=aws_secret_access_key)
    conn.run_instances(ami_image_id,
                       instance_type=instance_type,
                       dry_run=dry_run)


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
    settings = get_appsettings(config_uri)

    aws_access_key_id = settings.get('ec2_access_key_id')
    aws_secret_access_key = settings.get('ec2_secret')

    create_instance(aws_access_key_id=aws_access_key_id,
                    aws_secret_access_key=aws_secret_access_key #,dry_run=False
                    )



__author__ = 'outcastgeek'

import boto.ec2

amz_access_key_id = ""
amz_secret = ""

def create_instance(region="us-east-1",
                    ami_image_id="ami-cbb593a2",
                    instance_type="m1.small",
                    dry_run=True):
    regions = boto.ec2.regions()
    print regions
    conn = boto.ec2.connect_to_region(region,
                                      aws_access_key_id=amz_access_key_id,
                                      aws_secret_access_key=amz_secret)
    conn.run_instances(ami_image_id,
                       instance_type=instance_type,
                       dry_run=dry_run)


if __name__ == "__main__":
    create_instance()



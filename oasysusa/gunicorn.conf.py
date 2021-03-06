__author__ = 'outcastgeek'

import os

def get_workers():
    procs = os.sysconf('SC_NPROCESSORS_ONLN')
    if procs > 0:
        return procs * 2 + 1
    else:
        return 3

workers = get_workers()
#workers = 8
worker_class = 'tornado'

backlog = 2048
timeout = 30
keepalive = 2
debug = False
spew = False
proc_name = 'oasysusa'
# accesslog = '/home/oasysusa/oasys_corp/logs/oasysusa.log'
# pidfile = '/home/oasysusa/oasys_corp/logs/pid_5000.pid'



"""
use = egg:gunicorn#main
host = 0.0.0.0
port = 8080
# gunicorn configuration
workers = 8
#worker-class = 'tornado'
#worker-class = 'gevent'
worker-class = 'socketio.sgunicorn.GeventSocketIOWorker'
backlog = 2048
timeout = 30
keepalive = 2
debug = False
spew = False
proc_name = 'oasysusa'
accesslog = '/home/oasysusa/oasys_corp/logs/oasysusa.log'
pidfile = '/home/oasysusa/oasys_corp/logs/pid_5000.pid'
#config = %(here)/gunicorn.conf.py
"""
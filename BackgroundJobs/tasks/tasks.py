
from celery import Celery
#from celery.contrib import rdb

celery = Celery()
celery.config_from_object('celeryconfig')

from celery.utils.log import get_task_logger

logger = get_task_logger(__name__)

@celery.task
def add(x, y):
    logger.debug('Adding %s + %s' % (x, y))
    return x + y

@celery.task
def fib(n):
    logger.debug('Return Fibonacci series up to %s' % n)
    result = []
    a, b = 0, 1
    while b < n:
        result.append(b)
        a, b = b, a+b
    return result

################ ZMQ Stuff ########################

from gevent import spawn, spawn_later, shutdown, joinall, killall
import zerorpc

import itertools

from functools import wraps

def retry(tries=3):
    def retry_decorating_function(func):
        @wraps(func)
        def wrap(*args, **kwds):
            result = None
            x = 1
            ex = None
            while x <= tries + 1:
                try:
                    result = func(*args, **kwds)
                except Exception, e:
                    if x == tries + 1:
                        print "\n\nBREAK ON #%s!!!!\n\n" % x
                        logger.error("Something went wrong: %s" % e)
                        raise e
                    print "\n########################################"
                    print "           Retry #%s" % x
                    print "########################################\n"
                    x +=1
                    continue
                break
            return result
        return wrap
    return retry_decorating_function

def bicycle(iterable, repeat=1):
    for item in itertools.cycle(iterable):
        for _ in xrange(repeat):
            yield item

def run_streaming_rpc(endpoint):
    worker = zerorpc.Server(StreamingRPC())
    print endpoint
    worker.bind(endpoint)
    spawn(worker.run)

def run_rpc_servers(pool_size = 128, endpoint = "tcp://0.0.0.0:4242"):
    endpoints = bicycle([''.join(endpoint + str(x)) for x in xrange(pool_size)], 1)

    try:
        [run_streaming_rpc(endpoints.next()) for x in xrange(pool_size)]
    except:
        print "ZeroRPC Servers already started!!!!"
    finally:
        return endpoints

class StreamingRPC(object):
    @zerorpc.stream
    def streaming_range(self, fr, to, step):
        print (fr, to, step)
        #lb = 1 / 0
        return xrange(fr, to, step)

run_rpc_servers()

@celery.task
@retry()
def retry_stream(fr, to, step):
    endpoints = run_rpc_servers()
    endpoint = endpoints.next()
    print "Using Rpc Node %s" % endpoint
    c = zerorpc.Client(endpoint)
    #c.connect(endpoint)    
    for item in c.streaming_range(fr, to, step):
        print item

"""
if __name__ == '__main__':
    celery.worker_main()
"""

if __name__ == '__main__':

    retry_stream(10, 20, 2)

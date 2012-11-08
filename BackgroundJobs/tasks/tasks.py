
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

from gevent import spawn, spawn_later, joinall, killall
import zmq.green as zmq
from time import time

def ventilator(batch_size, test_size):
    """task ventilator function"""

    #"""set up a zeromq context"""
    context = zmq.Context()

    """create a push socket for sending tasks to workers"""
    send_sock = context.socket(zmq.PUSH)
    send_sock.bind("tcp://127.0.0.1:5555")

    """create a pull socket for receiving acks from the sink"""
    recv_sock = context.socket(zmq.PULL)
    recv_sock.bind("tcp://127.0.0.1:5557")

    """initiate counter for tasks sent"""
    current_batch_count = 0

    """start the message loop"""
    for x in range(test_size):

        """send until we reach our batch limit"""
        while current_batch_count < batch_size:
            send_sock.send("task")
            current_batch_count += 1

        """reset the batch count"""
        current_batch_count = 0

        """wait for an acknowledgement and block while waiting -
           note this could be more sophisticated and provide
           support for other message types from the sink,
           but keeping it simple for this example"""
        msg = recv_sock.recv()

def worker():
    """task worker function"""

    """set up a zeromq context"""
    context = zmq.Context()

    """create a pull socket for receiving tasks from the ventilator"""
    recv_socket = context.socket(zmq.PULL)
    recv_socket.connect("tcp://127.0.0.1:5555")

    """create a push socket for sending results to the sink"""
    send_socket = context.socket(zmq.PUSH)
    send_socket.connect("tcp://127.0.0.1:5556")

    """receive tasks and send results"""
    while True:
        task = recv_socket.recv()
        send_socket.send("result")

def sink(batch_size, test_size):
    """task sink function"""

    """set up a zmq context"""
    context = zmq.Context()

    """create a pull socket for receiving results from the workers"""
    recv_sockett = context.socket(zmq.PULL)
    recv_sockett.bind("tcp://127.0.0.1:5556")

    """create a push socket for sending acknowledgements to the ventilator"""
    send_sockett = context.socket(zmq.PUSH)
    send_sockett.connect("tcp://127.0.0.1:5557")

    result_count = 0
    batch_start_time = time()
    test_start_time = batch_start_time

    for x in range(test_size):
        """receive a result and increment the count"""
        msg = recv_sockett.recv()
        result_count += 1

        """acknowledge that we've completed a batch"""
        if result_count == batch_size:
            send_sockett.send("ACK")
            result_count = 0
            batch_start_time = time()

    duration = time() - test_start_time
    tps = test_size / duration
    print "ZeroMQ throughput...."
    print "messages per second: %s" % (tps)
    logger.debug('messages per second: %s' % tps)

@celery.task
def devide_and_conquer(num_workers, batch_size, test_size):
    def devide_and_conquer_runner(num_workers, batch_size, test_size):
        ventilator_t = spawn(ventilator, batch_size, test_size)
        sink_t = spawn(sink, batch_size, test_size)

        threads = [ventilator_t, sink_t] + [spawn(worker) for i in xrange(num_workers)]
        joinall(threads)
        killall(threads)
    spawn(devide_and_conquer_runner, num_workers, batch_size, test_size).join(10)
    #devide_and_conquer_runner(num_workers, batch_size, test_size)

"""
if __name__ == '__main__':
    celery.worker_main()
"""

if __name__ == '__main__':
    num_workers = 8
    batch_size = 100
    test_size = 1000
    spawn(devide_and_conquer, num_workers, batch_size, test_size).join(10)

"""
if __name__ == '__main__':
    from multiprocessing import Process
    num_workers = 4
    batch_size = 100
    test_size = 1000

    workers = {}
    ventilator_t = Process(target=ventilator, args=(batch_size, test_size,))
    sink_t = Process(target=sink, args=(batch_size, test_size,))

    sink_t.start()

    for x in range(num_workers):
        workers[x] = Process(target=worker, args=())
        workers[x].start()

    ventilator_t.start()
    ventilator_t.join()
"""

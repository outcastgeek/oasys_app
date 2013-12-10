__author__ = 'outcastgeek'

"""
Asynchronous request-reply single-threaded server in Python
that spawns a request handler each time a request is received

This is different from other examples because the number of request handler threads is not defined ahead of time.

Request:
Client DEALER --> Server ROUTER --> Request handler (spawned)
1. Clients send requests via a DEALER socket on port 5570
2. Server receives requests via a ROUTER socket on port 5570
3. Server passes both the request and the client identity directly to request handlers when they are spawned

Reply:
Client DEALER <-- Server ROUTER <-- Server DEALER <-- Request handler DEALER
1. Request handler returns the reply to the Server via a DEALER socket on inproc
2. Server receives the reply from the request handler via a DEALER socket on inproc
3. Server sends the reply to the client via a ROUTER socket on port 5570
4. Client receives the reply via a DEALER socket on port 5570
"""

import gevent

from datetime import datetime
from gevent import Greenlet
from gevent.pool import Pool
from zmq import green as zmq

# Set number of clients that will make simultaneous requests
NUMBER_OF_CLIENTS = 100000

# Set how long it will take each request to be processed by the server
PROCESSING_TIME = 5

# Set the maximum pool size for the request handlers
POOL_SIZE = 40000

SEND_DELAY=0.01

class Client(Greenlet):
    def __init__(self, identity):
        Greenlet.__init__(self)
        self.identity = '{}{}'.format('id_', identity)

    def run(self):
        context = zmq.Context()
        socket = context.socket(zmq.DEALER)
        socket.setsockopt(zmq.IDENTITY, self.identity)
        socket.connect('tcp://localhost:5570')
        print 'Client %s started\n' % self.identity
        poll = zmq.Poller()
        poll.register(socket, zmq.POLLIN)

        socket.send('[request from client %s]' % self.identity)
        print 'Req from client %s sent.\n' % self.identity

        received_reply = False
        while not received_reply:
            sockets = dict(poll.poll(1000))
            if socket in sockets:
                if sockets[socket] == zmq.POLLIN:
                    msg = socket.recv()
                    print 'Client %s received reply: %s\n' % (self.identity, msg)
                    del msg
                    received_reply = True

        socket.close()
        context.term()
        self.kill(block=False)


class Server(Greenlet):
    def __init__(self):
        Greenlet.__init__(self)
        self.pool = Pool(POOL_SIZE)

    def stop(self):
        self.kill(block=False)

    def stopped(self):
        return self.dead

    def run(self):
        context = zmq.Context()
        frontend = context.socket(zmq.ROUTER)
        frontend.bind('tcp://*:5570')

        backend = context.socket(zmq.DEALER)
        backend.bind('inproc://backend')

        poll = zmq.Poller()
        poll.register(frontend, zmq.POLLIN)
        poll.register(backend,  zmq.POLLIN)

        while not self.stopped():
            sockets = dict(poll.poll(1000))
            if frontend in sockets:
                if sockets[frontend] == zmq.POLLIN:
                    _id = frontend.recv()
                    msg = frontend.recv()
                    print 'Server received %s\n' % msg

                    handler = RequestHandler(context, _id, msg)
                    # self.pool.wait_available()
                    # self.pool.start(handler)
                    handler.start()

            if backend in sockets:
                if sockets[backend] == zmq.POLLIN:
                    _id = backend.recv()
                    msg = backend.recv()
                    print 'Server sending to frontend %s\n' % msg
                    frontend.send(_id, zmq.SNDMORE)
                    frontend.send(msg)

        frontend.close()
        backend.close()
        context.term()


class RequestHandler(Greenlet):
    def __init__(self, context, id, msg):
        """
        RequestHandler
        :param context: ZeroMQ context
        :param id: Requires the identity frame to include in the reply so that it will be properly routed
        :param msg: Message payload for the worker to process
        """
        Greenlet.__init__(self)
        self.context = context
        self.msg = msg
        self._id = id

    def run(self):
        # Worker will process the task and then send the reply back to the DEALER backend socket via inproc
        worker = self.context.socket(zmq.DEALER)
        worker.connect('inproc://backend')
        print 'Request handler started to process %s\n' % self.msg

        # Simulate a long-running operation
        # time.sleep(PROCESSING_TIME)
        gevent.sleep(PROCESSING_TIME)

        worker.send(self._id, zmq.SNDMORE)
        worker.send(self.msg)
        del self.msg

        print 'Request handler quitting.\n'
        worker.close()

def main():
    # Start the server that will handle incoming requests
    server = Server()
    server.start()

    starttime = datetime.now()

    # Start multiple clients which will each send a request that takes a while to process
    clients = []
    for i in xrange(NUMBER_OF_CLIENTS):
        client = Client(i)
        gevent.sleep(SEND_DELAY)
        client.start()
        clients.append(client)

    # Wait for all the clients to finish and then stop the server
    # for client in clients:
    #     client.join()
    gevent.joinall(clients)
    server.stop()

    endtime = datetime.now()
    elapsed = endtime-starttime
    print 'Total time elapsed: %d seconds\n' \
          'Number of requests processed: %s\n' \
          'Individual request processing time: %s seconds\n' \
          'Parallel request processing time: %.4f seconds' \
          % (elapsed.seconds,
             NUMBER_OF_CLIENTS,
             PROCESSING_TIME,
             float(elapsed.seconds)/NUMBER_OF_CLIENTS)

if __name__ == "__main__":
    main()



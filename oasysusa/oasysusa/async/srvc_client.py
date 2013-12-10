__author__ = 'outcastgeek'

import logging
import umsgpack

from zmq import green as zmq

log = logging.getLogger('oasysusa')

def srvc_ask(identity, address, message):
    identity = '{}{}'.format('id_', identity)
    address = address
    pack_msg = umsgpack.packb(message)
    context = zmq.Context()
    socket = context.socket(zmq.DEALER)
    socket.setsockopt(zmq.IDENTITY, identity)
    socket.connect(address)
    log.debug('Client %s started\n' % identity)
    poll = zmq.Poller()
    poll.register(socket, zmq.POLLIN)

    socket.send(pack_msg)
    log.debug('Req from client %s sent.\n' % identity)

    response = None
    received_reply = False
    while not received_reply:
        sockets = dict(poll.poll(1000))
        if socket in sockets:
            if sockets[socket] == zmq.POLLIN:
                response = socket.recv()
                log.debug('Client %s received reply: %s\n' % (identity, response))
                received_reply = True

    socket.close()
    context.term()
    return response

def srvc_tell(identity, address, message):
    identity = '{}{}'.format('id_', identity)
    address = address
    pack_msg = umsgpack.packb(message)
    context = zmq.Context()
    socket = context.socket(zmq.DEALER)
    socket.setsockopt(zmq.IDENTITY, identity)
    socket.connect(address)
    log.debug('Client %s started\n' % identity)
    poll = zmq.Poller()
    poll.register(socket, zmq.POLLIN)

    socket.send(pack_msg)
    log.debug('Req from client %s sent.\n' % identity)



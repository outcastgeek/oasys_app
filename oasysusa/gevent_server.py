__author__ = 'outcastgeek'

from socketio.server import SocketIOServer
from pyramid.paster import get_app
from gevent import monkey; monkey.patch_all()

if __name__ == '__main__':

    port = 6543
    # port = 8080
    app = get_app('oasysusa/development.ini')
    # app = get_app('production.ini')

    print 'Listening on port http://0.0.0.0:%d and on port 10843 (flash policy server)' % port

    SocketIOServer(('0.0.0.0', port), app,
                   resource="socket.io", policy_server=True,
                   policy_listener=('0.0.0.0', 10843)).serve_forever()


__author__ = 'outcastgeek'

from pyramid.paster import get_app
from waitress import serve

if __name__ == '__main__':

    port = 6543
    # port = 8080
    wsgiapp = get_app('development.ini')
    # wsgiapp = get_app('production.ini')

    serve(wsgiapp, host='0.0.0.0', port=port)


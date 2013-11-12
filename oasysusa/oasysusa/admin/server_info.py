__author__ = 'outcastgeek'

import subprocess

from pyramid.response import Response
from pyramid.view import view_config

from ..events import get_settings

#Check this out: http://stackoverflow.com/questions/12523044/how-can-i-tail-a-log-file-in-python
# and this too: http://blog.abourget.net/page/2/
# and this too: http://stackoverflow.com/questions/7353054/call-a-shell-command-containing-a-pipe-from-python-and-capture-stdout

@view_config(name='logs',
             request_method='GET',
             permission='admin')
def check_log_last_600(request):
    settings = get_settings()
    log_location = settings.get('log_location')
    num_of_lines = request.GET.get('num_of_lines', 8)
    p = subprocess.Popen('tail -%s %s' % (num_of_lines, log_location), shell=True, stdout=subprocess.PIPE)
    stdout, stderr = p.communicate()
    return Response(body=stdout, content_type='text/plain')

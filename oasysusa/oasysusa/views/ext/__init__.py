__author__ = 'outcastgeek'

from pyramid.renderers import render_to_response


def hello(name):
    greeting = "Hello %s!!!!" % name
    response = render_to_response('templates/admin/hello.jinja2', dict(greeting=greeting))
    return response.body

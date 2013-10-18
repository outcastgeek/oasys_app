__author__ = 'outcastgeek'

from pyramid.events import subscriber, BeforeRender

@subscriber(BeforeRender)
def add_globals(event):
    event['project'] = 'oasysusa'


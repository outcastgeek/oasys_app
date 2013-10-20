__author__ = 'outcastgeek'

from pyramid.events import subscriber, BeforeRender
from pyramid.security import authenticated_userid

@subscriber(BeforeRender)
def add_globals(event):
    # request = event['request']
    # userID = authenticated_userid(request)
    project = 'oasysusa'
    event.update(dict(
        # USER_ID=userID,
        project=project
    ))


__author__ = 'outcastgeek'

import logging

from .models import Employee

logging.basicConfig()
log = logging.getLogger(__file__)

def groupfinder(userid, request):

    user = Employee.by_username(userid)

    if user:
        return [g.groupname for g in user.groups]
    else:
        return ['user']

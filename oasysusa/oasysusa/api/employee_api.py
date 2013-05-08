__author__ = 'outcastgeek'

""" OasysUSA Cornice Services
"""

import os
import binascii

from cornice.service import Service

from ..models import (
    DBSession,
    Employee,
    row2dict,
    )

employees = Service(name='employees', path='/employees', description='All Employees')

#
# Helpers
#
def _create_token():
    return binascii.b2a_hex(os.urandom(20))

#
# Services
#

#
# Employee Management
#

# See:  http://h3manth.com/content/python-objects-json-string And: http://stackoverflow.com/questions/1958219/convert-sqlalchemy-row-object-to-python-dict

@employees.get()
def get_employees(request):
    all_employees = DBSession.query(Employee).all()
    return [row2dict(employee) for employee in all_employees]

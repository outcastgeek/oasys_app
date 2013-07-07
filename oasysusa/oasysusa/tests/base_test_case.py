__author__ = 'outcastgeek'

import unittest
import transaction
from pyramid import testing

from ..models import (
    DBSession,
    Base
    )

class BaseTestCase(unittest.TestCase):

    def setUp(self):
        self.config = testing.setUp()
        from sqlalchemy import create_engine
        engine = create_engine('sqlite://')
        DBSession.configure(bind=engine)
        Base.metadata.create_all(engine)

    def tearDown(self):
        DBSession.remove()
        testing.tearDown()

    def save(self, data):
        with transaction.manager:
            DBSession.add(data)

    def find_all(self, modelClass):
        return DBSession.query(modelClass).all()


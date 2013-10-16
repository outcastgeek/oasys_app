__author__ = 'outcastgeek'

import datetime
from pyramid import testing
from base_test_case import BaseTestCase

from ..api.timesheet_api import (
    first_and_last_dow,
    WeekApi)

class TestTimesheetAPI(BaseTestCase):

    def setUp(self):
        self.request = testing.DummyRequest()
        self.config = testing.setUp(request=self.request)
        super(TestTimesheetAPI, self).setUp()

    def test_first_and_last_dow(self):
        # setup
        monday_s_date = datetime.date(2013, 10, 07)
        sunday_s_date = datetime.date(2013, 10, 13)
        day1 = datetime.date(2013, 10, 7)
        day2 = datetime.date(2013, 10, 9)
        day3 = datetime.date(2013, 10, 11)
        day4 = datetime.date(2013, 10, 13)

        # run
        monday1, sunday1 = first_and_last_dow(day1)
        monday2, sunday2 = first_and_last_dow(day2)
        monday3, sunday3 = first_and_last_dow(day3)
        monday4, sunday4 = first_and_last_dow(day4)

        # verify
        self.assertEqual(monday_s_date, monday1)
        self.assertEqual(sunday_s_date, sunday1)
        self.assertEqual(monday_s_date, monday2)
        self.assertEqual(sunday_s_date, sunday2)
        self.assertEqual(monday_s_date, monday3)
        self.assertEqual(sunday_s_date, sunday3)
        self.assertEqual(monday_s_date, monday4)
        self.assertEqual(sunday_s_date, sunday4)

    def test_empty_week(self):
        week_api = WeekApi(self.request)
        self.assertTrue(len(week_api.get()) == 0)

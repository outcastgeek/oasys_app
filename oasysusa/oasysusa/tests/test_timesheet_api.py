__author__ = 'outcastgeek'

import datetime
from pyramid import testing
from base_test_case import BaseTestCase

from ..api.timesheet_api import (
    first_and_last_dow,
    WeekApi, get_week_dates)

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

    def test_get_week_dates(self):
        # setup
        monday = datetime.date(2013, 10, 21)
        tuesday = datetime.date(2013, 10, 22)
        wednesday = datetime.date(2013, 10, 23)
        thursday = datetime.date(2013, 10, 24)
        friday = datetime.date(2013, 10, 25)
        saturday = datetime.date(2013, 10, 26)
        sunday = datetime.date(2013, 10, 27)

        # run
        week_dates = get_week_dates(friday)

        # verify
        self.assertEqual(monday, week_dates[0])
        self.assertEqual(tuesday, week_dates[1])
        self.assertEqual(wednesday, week_dates[2])
        self.assertEqual(thursday, week_dates[3])
        self.assertEqual(friday, week_dates[4])
        self.assertEqual(saturday, week_dates[5])
        self.assertEqual(sunday, week_dates[6])

    def test_empty_week(self):
        week_api = WeekApi(self.request)
        self.assertTrue(len(week_api.get()) == 0)

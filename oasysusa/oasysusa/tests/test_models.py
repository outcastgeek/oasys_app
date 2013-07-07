from pyramid import testing

__author__ = 'outcastgeek'

from base_test_case import BaseTestCase

from ..models import (
    Employee,
    find_employee_by_provider_id,
    PayrollCycle,
    )

class TestEmployee(BaseTestCase):

    USERNAME = 'dummy'
    EMAIL = 'dummy@dummy.com'
    PROVIDER_ID = 'dummy_provider'

    def setUp(self):
        self.config = testing.setUp(request=testing.DummyRequest())
        super(TestEmployee, self).setUp()

    def test_find_employee_by_provider_id(self):
        # setup
        model = Employee(username=self.USERNAME, email=self.EMAIL, provider_id=self.PROVIDER_ID)
        self.save(model)

        # run
        employee = find_employee_by_provider_id(self.PROVIDER_ID)

        # verify
        self.assertEqual(employee.username, self.USERNAME)
        self.assertEqual(employee.email, self.EMAIL)
        self.assertEqual(employee.provider_id, self.PROVIDER_ID)

class TestPayrollCycle(BaseTestCase):

    USERNAME = 'dummy'
    EMAIL = 'dummy@dummy.com'
    PROVIDER_ID = 'dummy_provider'

    def setUp(self):
        self.config = testing.setUp(request=testing.DummyRequest())
        super(TestPayrollCycle, self).setUp()
        for payrollCycle in [PayrollCycle(payroll_cycle_number=7, payroll_cycle_year=2013),
                             PayrollCycle(payroll_cycle_number=3, payroll_cycle_year=2012),
                             PayrollCycle(payroll_cycle_number=11, payroll_cycle_year=2011)]:
            self.save(payrollCycle)

    def test_that(self):
        # run
        payrollCycles = self.find_all(PayrollCycle)

        # verify
        self.assertEqual(len(payrollCycles), 3)
        for payrollCycle in payrollCycles:
            self.assertIsNotNone(payrollCycle.payroll_cycle_number)
            self.assertIsNotNone(payrollCycle.payroll_cycle_year)

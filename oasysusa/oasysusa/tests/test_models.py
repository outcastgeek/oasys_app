__author__ = 'outcastgeek'

from pyramid import testing
from base_test_case import BaseTestCase
from ..mixins.sqla import Q

from ..models import (
    Employee,
    Group,
    DATE_FORMAT,
    PayrollCycle,
    )

class TestEmployee(BaseTestCase):

    USERNAME = 'dummy'
    EMAIL = 'dummy@dummy.com'
    PROVIDER_ID = 'dummy_provider'
    DATE_OF_BIRTH = '01/01/2001'

    def setUp(self):
        self.config = testing.setUp(request=testing.DummyRequest())
        super(TestEmployee, self).setUp()

    def test_find_employee_by_provider_id(self):
        # setup
        password = "password"
        admin_group = Group(groupname="admin")
        user_group = Group(groupname="user")
        groups = [user_group, admin_group]
        model = Employee(username=self.USERNAME, email=self.EMAIL, groups=groups, password=password,
                         provider_id=self.PROVIDER_ID, date_of_birth=self.DATE_OF_BIRTH)
        Employee.save(model)

        # run
        employee = Employee.by_provider_id(self.PROVIDER_ID) or Employee.by_username(self.USERNAME)

        # verify
        self.assertEqual(employee.username, self.USERNAME)
        self.assertEqual(employee.email, self.EMAIL)
        self.assertEqual(employee.provider_id, self.PROVIDER_ID)
        self.assertEqual(employee.date_of_birth.strftime(DATE_FORMAT), self.DATE_OF_BIRTH)
        matches = set(employee.groups) & set(groups)
        self.assertEqual(len(matches), len(groups))
        self.assertTrue(Employee.check_password(self.USERNAME, password), "Passwords should match")

class TestPayrollCycle(BaseTestCase):

    USERNAME = 'dummy'
    EMAIL = 'dummy@dummy.com'
    PROVIDER_ID = 'dummy_provider'

    def setUp(self):
        self.config = testing.setUp(request=testing.DummyRequest())
        super(TestPayrollCycle, self).setUp()
        pcns = [7, 3, 11]
        pcy = [2013, 2012, 2011]
        new_payroll_cycle = lambda **kwargs: PayrollCycle(**kwargs)
        for payrollCycle in [PayrollCycle(payroll_cycle_number=7, payroll_cycle_year=2013),
                             PayrollCycle(payroll_cycle_number=3, payroll_cycle_year=2012),
                             PayrollCycle(payroll_cycle_number=11, payroll_cycle_year=2011)]:
            payrollCycle.save()

    def test_that(self):
        # run
        payrollCycles = Q(PayrollCycle).all()

        # verify
        self.assertEqual(len(payrollCycles), 3)
        for payrollCycle in payrollCycles:
            self.assertIsNotNone(payrollCycle.payroll_cycle_number)
            self.assertIsNotNone(payrollCycle.payroll_cycle_year)

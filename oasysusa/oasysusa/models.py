__author__ = 'outcastgeek'

import string
import random
import os

from datetime import datetime

from sqlalchemy import (
    Column,
    Integer,
    Text,
    ForeignKey,
    Sequence,
    Unicode,
    Table,
    String,
    Boolean,
    Date)

from sqlalchemy.ext.declarative import declarative_base

from sqlalchemy.orm import (
    scoped_session,
    sessionmaker,
    relationship
    )

from .mixins.sqla import CRUDMixin

from formencode import Schema
from formencode.national import USPhoneNumber
from formencode.validators import (
    UnicodeString,
    Email,
    DateValidator,
    NotEmpty,
    )

from zope.sqlalchemy import ZopeTransactionExtension

from hashlib import sha1

from pyramid.security import (
    Allow,
    Authenticated,
    Everyone
    )

class RootFactory(object):
    __acl__ = [(Allow, Everyone, 'view'),
               (Allow, Authenticated, 'edit'),
               (Allow, Authenticated, 'user'),
               (Allow, 'group:editors', 'edit')]
    def __init__(self, request):
        pass

DBSession = scoped_session(sessionmaker(extension=ZopeTransactionExtension()))
Base = declarative_base()

def find_methods(obj):
    return [method for method in dir(obj) if callable(getattr(obj, method))]

DATE_FORMAT = "%m/%d/%Y"
SQL_DATE_FORMAT = "%Y-%m-%d"
EARLIEST_DATE = datetime.strptime('01/01/1900', DATE_FORMAT)

class MyModel(CRUDMixin, Base):
    __tablename__ = 'models'
    id = Column(Integer, primary_key=True)
    name = Column(Text, unique=True)
    value = Column(Integer)

    def __init__(self, name=None, value=None):
        self.name = name
        self.value = value
        self.session = DBSession

################# EMPLOYEES #############################

class Employee(Base):
    __tablename__ = 'employees'
    id = Column(Integer, primary_key=True)
    username = Column(Text, unique=True)
    password = Column(String)
    first_name = Column(Text)
    last_name = Column(Text)
    email = Column(Text, unique=True)
    active = Column(Boolean)
    groups = relationship("Group", secondary='employee_group', backref="employees")
    provider = Column(Text)
    address = Column(Text)
    employee_id = Column(Text)
    provider_id = Column(Text)
    telephone_number = Column(Text)
    date_of_birth = Column(Date)
    time_sheets = relationship("TimeSheet", backref="employees")

    # employees = Table(__tablename__, Base.metadata, autoload=True)

    @property
    def __acl__(self):
        return [
            (Allow, self.username, 'user'),
            ]

    def __init__(self, username=None, password=None, first_name=None,
                 last_name=None, email=None, employee_id=None,
                 provider_id=None, date_of_birth=None, provider=None,
                 active=False, address=None, telephone_number=None, groups=None):
        self.username = username
        self.password = self._set_password(password) if password else self._set_password(self._generate_password(16))
        self.first_name = first_name
        self.last_name = last_name
        self.email = email
        self.provider = provider
        self.active = active
        self.address = address
        self.employee_id = employee_id
        self.provider_id = provider_id
        self.telephone_number = telephone_number
        self.date_of_birth = date_of_birth
        self.groups = groups or []
        self.session = DBSession

    def update_fields(self, updated_fields={}):
        for key, value in updated_fields.iteritems():
            setattr(self, key, value)


    # Check this out: http://pyramid.chromaticleaves.com/simpleauth/
    def _set_password(self, password):
        hashed_password = password

        if isinstance(password, unicode):
            password_8bit = password.encode('UTF-8')
        else:
            password_8bit = password

        salt = sha1()
        salt.update(os.urandom(60))
        hash = sha1()
        hash.update(password_8bit + salt.hexdigest())
        hashed_password = salt.hexdigest() + hash.hexdigest()

        if not isinstance(hashed_password, unicode):
            hashed_password = hashed_password.decode('UTF-8')

        self.password = hashed_password

    def _generate_password(self, length):
        password_len = length

        password = []

        for group in (string.ascii_letters, string.punctuation, string.digits):
            password += random.sample(group, 3)

        password += random.sample(
            string.ascii_letters + string.punctuation + string.digits,
            password_len - len(password))

        random.shuffle(password)
        password = ''.join(password)

        return password

    def validate_password(self, password):
        if self.password:
            hashed_pass = sha1()
            hashed_pass.update(password + self.password[:40])
            return self.password[40:] == hashed_pass.hexdigest()
        else:
            return False

    @classmethod
    def by_id(cls, userid):
        return DBSession.query(Employee).filter(Employee.id==userid).first()

    @classmethod
    def by_provider_id(cls, unique_identifier):
        # DBSession.query(Employee).filter_by(provider_id=unique_identifier).first()
        DBSession.query(Employee).filter_by(provider_id=str(unique_identifier)).first()

    @classmethod
    def by_username(cls, username):
        return DBSession.query(Employee).filter(Employee.username==username).first()

    @classmethod
    def save(cls, employee):
        employee_dob = datetime.strptime(employee.date_of_birth, DATE_FORMAT)
        employee.date_of_birth = employee_dob if employee_dob > EARLIEST_DATE else EARLIEST_DATE
        employee_group = Group('employee')
        employee.groups.append(employee_group)
        return DBSession.add(employee)

    @classmethod
    def update_or_insert(cls, username, employee):
        #check
        existing_employee = cls.by_username(username)
        if existing_employee:
            # setup data
            employee_dob = datetime.strptime(employee.date_of_birth, DATE_FORMAT)
            employee.date_of_birth = employee_dob if employee_dob > EARLIEST_DATE else EARLIEST_DATE
            employee_data = employee.__dict__
            # update
            employees_table = employee.__table__
            update_stmt = employees_table.update(employees_table.c.username==username)
            update_stmt.execute(employee_data)
        else:
            cls.save(employee)

    def __json__(self, request):
        return {
            'username': self.username,
            'first_name': self.first_name,
            'last_name': self.last_name,
            'email': self.email,
            # 'provider': self.provider,
            # 'active': self.active,
            'address': self.address,
            # 'employee_id': self.employee_id,
            # 'provider_id': self.provider_id,
            'telephone_number': self.telephone_number,
            'date_of_birth': self.date_of_birth.strftime(DATE_FORMAT)
        }

        # def __repr__(self):
        #     return "<Employee('%s','%s', '%s', '%s')>" % (self.username, self.first_name, self.last_name, self.provider, self.email)

class EmployeeSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True

    provider_id = NotEmpty
    username = UnicodeString(min=2)
    first_name = UnicodeString(min=2)
    last_name = UnicodeString(min=2)
    email = Email()
    date_of_birth = DateValidator(date_format='mm/dd/yyyy')
    telephone_number = USPhoneNumber
    address = NotEmpty

# def row2dict(row):
#     d = {}
#     for column in row.__table__.columns:
#         d[column.name] = getattr(row, column.name)
#
#     return d

row2dict = lambda r: {c.name: getattr(r, c.name) for c in r.__table__.columns}

# See:  http://h3manth.com/content/python-objects-json-string And: http://stackoverflow.com/questions/1958219/convert-sqlalchemy-row-object-to-python-dict

def get_employees(request):
    all_employees = DBSession.query(Employee).all()
    return [row2dict(employee) for employee in all_employees]

################# END EMPLOYEES #########################

################# GROUPS #################################

class Group(Base):
    __tablename__ = 'groups'
    id = Column(Integer, Sequence('groups_seq_id', optional=True), primary_key=True)
    groupname = Column(Unicode(255), unique=True)

    def __init__(self, groupname):
        self.groupname = groupname

################# END GROUPS #############################

################# EMPLOYEES-GROUP #################################

user_group_table = Table('employee_group', Base.metadata,
                         Column('employee_id', Integer, ForeignKey('employees.id')),
                         Column('group_id', Integer, ForeignKey('groups.id')),
                         )

################# END EMPLOYEES-GROUP #############################

################# PAYROLL CYCLES #################################

class PayrollCycle(CRUDMixin, Base):
    __tablename__ = 'payroll_cycles'
    id = Column(Integer, primary_key=True)
    payroll_cycle_number = Column(Integer)
    payroll_cycle_year = Column(Integer)
    check_date = Column(Date)
    direct_deposit_date = Column(Date)
    start_date = Column(Date)
    end_date = Column(Date)
    time_sheets = relationship("TimeSheet", backref="payroll_cycles")
    work_segments = relationship("WorkSegment", backref="payroll_cycles")

    def __init__(self, payroll_cycle_number=None, payroll_cycle_year=None, check_date=None,
                 direct_deposit_date=None, start_date=None, end_date=None):
        self.payroll_cycle_number = payroll_cycle_number
        self.payroll_cycle_year = payroll_cycle_year
        self.check_date = check_date
        self.direct_deposit_date = direct_deposit_date
        self.start_date = start_date
        self.end_date = end_date
        self.session = DBSession

################# END PAYROLL CYCLES #############################

################# PROJECTS #####################################

class Project(CRUDMixin, Base):
    __tablename__ = 'projects'
    id = Column(Integer, primary_key=True)
    name = Column(Text)
    client = Column(Text)
    description = Column(Text)
    email = Column(Text, unique=True)
    telephone_number = Column(Text)
    address = Column(Text)
    work_segments = relationship("WorkSegment", backref="projects")

    def __init__(self, name=None, client=None, description=None,
                 email=None, address=None, telephone_number=None):
        self.name = name
        self.client = client
        self.description = description
        self.email = email
        self.telephone_number = telephone_number
        self.address = address
        self.session = DBSession

    def __json__(self, request):
        return {
            'name': self.name,
            'client': self.client,
            'description': self.description,
            'email': self.email,
            'telephone_number': self.telephone_number,
            'address': self.address
        }

class ProjectSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True

    name = NotEmpty
    client = NotEmpty
    description = UnicodeString(min=250)
    email = Email()
    telephone_number = USPhoneNumber
    address = NotEmpty

################# END PROJECTS #################################

################# TIME SHEETS #########################################

class TimeSheet(CRUDMixin, Base):
    __tablename__= 'time_sheets'
    id = Column(Integer, primary_key=True)
    employee_id = Column(Integer, ForeignKey('employees.id'))
    payroll_cycle_id = Column(Integer, ForeignKey('payroll_cycles.id'))
    start_date = Column(Date)
    end_date = Column(Date)
    description = Column(Text)
    work_segments = relationship("WorkSegment", backref="time_sheets")

    def __init__(self, start_date=None, end_date=None, description=None):
        self.start_date = start_date
        self.end_date = end_date
        self.description = description
        self.session = DBSession

class TimeSheetSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True

    description = UnicodeString(min=250)

################# END TIME SHEETS #####################################

################# WORK SEGMENTS #############################################

class WorkSegment(CRUDMixin, Base):
    __tablename__ = 'work_segments'
    id = Column(Integer, primary_key=True)
    project_id = Column(Integer, ForeignKey('projects.id'))
    time_sheet_id = Column(Integer, ForeignKey('time_sheets.id'))
    payroll_cycle_id = Column(Integer, ForeignKey('payroll_cycles.id'))
    employee_id = Column(Integer, ForeignKey('employees.id'))
    date = Column(Date)
    hours = Column(Integer)

    def __init__(self, date=None, hours=None, description=None):
        self.date = date
        self.hours = hours
        self.session = DBSession

    @classmethod
    def in_range_inc(cls, low, high):
        return DBSession.query(WorkSegment) \
                        .filter(WorkSegment.date >= low) \
                        .filter(WorkSegment.date <= high).all()

    def __json__(self, request):
        return {
            'date': self.date.strftime(DATE_FORMAT),
            'hours': self.hours
        }

class WorkSegmentSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True

    hours = NotEmpty

################# END WORK SEGMENTS #########################################
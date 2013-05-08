
from pyramid.security import (
    Allow,
    Authenticated,
    Everyone
    )

from formencode import Schema
from formencode.validators import (
    UnicodeString,
    Email,
    DateValidator,
    NotEmpty)

class RootFactory(object):
    __acl__ = [(Allow, Everyone, 'view'),
               (Allow, Authenticated, 'edit'),
               (Allow, Authenticated, 'user'),
               (Allow, 'group:editors', 'edit')]
    def __init__(self, request):
        pass

from sqlalchemy import (
    Column,
    Integer,
    Text,
    Boolean,
    Date)

from sqlalchemy.ext.declarative import declarative_base

from sqlalchemy.orm import (
    scoped_session,
    sessionmaker,
    )

from zope.sqlalchemy import ZopeTransactionExtension

DBSession = scoped_session(sessionmaker(extension=ZopeTransactionExtension()))
Base = declarative_base()


class MyModel(Base):
    __tablename__ = 'models'
    id = Column(Integer, primary_key=True)
    name = Column(Text, unique=True)
    value = Column(Integer)

    def __init__(self, name, value):
        self.name = name
        self.value = value

class Employee(Base):
    __tablename__ = 'employees'
    id = Column(Integer, primary_key=True)
    username = Column(Text, unique=True)
    first_name = Column(Text)
    last_name = Column(Text)
    email = Column(Text, unique=True)
    provider = Column(Text)
    active = Column(Boolean)
    address = Column(Text)
    employee_id = Column(Text)
    provider_id = Column(Text)
    telephone_number = Column(Text)
    date_of_birth = Column(Date)

    def __init__(self, username=None, first_name=None, last_name=None,
                 email=None, employee_id=None, provider_id=None,
                 date_of_birth=None, provider=None, active=False,
                 address=None, telephone_number=None):
        self.username = username
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

def find_employee_by_provider_id(unique_identifier):
    return DBSession.query(Employee).filter_by(provider_id=unique_identifier).first()

# def row2dict(row):
#     d = {}
#     for column in row.__table__.columns:
#         d[column.name] = getattr(row, column.name)
#
#     return d

row2dict = lambda r: {c.name: getattr(r, c.name) for c in r.__table__.columns}


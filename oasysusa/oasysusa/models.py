
from pyramid.security import (
    Allow,
    Authenticated,
    Everyone
    )

class RootFactory(object):
    __acl__ = [(Allow, Everyone, 'view'),
               (Allow, Authenticated, 'edit'),
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
    telephone_number = Column(Text)
    date_of_birth = Column(Date)

    def __init__(self, username, first_name, last_name,
                 email, employee_id, date_of_birth,
                 provider=None, active=False, address=None,
                 telephone_number=None):
        self.username = username
        self.first_name = first_name
        self.last_name = last_name
        self.email = email
        self.provider = provider
        self.active = active
        self.address = address
        self.employee_id = employee_id
        self.telephone_number = telephone_number
        self.date_of_birth = date_of_birth




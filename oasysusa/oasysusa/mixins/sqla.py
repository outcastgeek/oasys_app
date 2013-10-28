__author__ = 'outcastgeek'

from time import time
from sqlalchemy import (
    event,
    Column,
    Integer)

def Q(clazz, filters=None, page=0, page_size=None):
    instance = clazz()
    query = instance.session.query(clazz)
    if filters:
        query = query.filter(**filters)
    if page_size:
        query = query.limit(page_size)
    if page:
        query = query.offset(page*page_size)
    return query

class TimeStampMixin(object):
    # other class methods

    @staticmethod
    def create_time(mapper, connection, target):
        target.created = time()

    @classmethod
    def __declare_last__(cls):
        # get called after mappings are completed
        # http://docs.sqlalchemy.org/en/rel_0_7/orm/extensions/declarative.html#declare-last
        event.listen(cls, 'before_insert', cls.create_time)


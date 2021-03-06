__author__ = 'outcastgeek'

import logging
import sys

from functools import partial

from pyramid_mailer.mailer import Mailer
from pyramid_mailer import get_mailer
from pyramid_mailer.message import Message

from ..models import Employee

from ..async.srvc_mappings import zmq_service, SEND_EMAIL_TASK
from ..async.srvc_client import srvc_tell

log = logging.getLogger('oasysusa')

def send_email(request,
               recipients,
               body,
               subject="DO NOT REPLY",
               sender="donotreply@oasys-corp.com"):
    try:
        # mailer = Mailer()
        mailer =get_mailer(request)
        message = Message(recipients=recipients, body=body, subject=subject, sender=sender)
        mailer.send(message)
        log.info('Sucessfully sent emails to: %s', recipients)
    except: # catch *all* exceptions
        e = sys.exc_info()[0]
        log.error("Error: %s, failed to email: %s", e, recipients)

def send_email_now(settings=None,
                  recipients=None,
                  body=None,
                  subject="DO NOT REPLY",
                  sender="donotreply@oasys-corp.com",
                  ** kw):
    try:
        mailer =Mailer.from_settings(settings)
        message = Message(recipients=recipients, body=body, subject=subject, sender=sender)
        mailer.send_immediately(message, fail_silently=False)
        log.info('Sucessfully sent emails to: %s', recipients)
    except: # catch *all* exceptions
        e = sys.exc_info()[0]
        log.error("Error: %s, failed to email: %s", e, recipients)

@zmq_service(srvc_name='send_email_task')
def send_email_task(data, settings=None):
    email_now = partial(send_email_now, settings)
    email_now(**data)

@zmq_service(srvc_name='timesheet_reminder_task')
def send_timesheet_reminder(data, settings=None):
    # Get settings
    workers_tcp_address = settings.get('workers_tcp_address')
    # Get employee
    username = data.get('username')
    employee = Employee.query().filter(Employee.username == username).first()
    # Message
    timesheet_url = settings.get('timesheet_url')
    message = "Remember to fill your weekly timesheet,\
               \nand upload your client's timecard at: %s.\
               \n\nThank you.\
               \n\nAdmin" % timesheet_url
    srvc_tell(workers_tcp_address, dict(srvc=SEND_EMAIL_TASK,
                                        recipients=[employee.email],
                                        body=message,
                                        subject="Timesheet Reminder (DO NOT REPLY)",
                                        sender="donotreply@oasys-corp.com"))

def includeme(config):
    settings = config.registry.settings
    # config.registry['mailer'] = Mailer.from_settings(settings)
    configure_send_email = lambda request: partial(send_email, request)
    config.add_request_method(configure_send_email, 'send_email', reify=True)

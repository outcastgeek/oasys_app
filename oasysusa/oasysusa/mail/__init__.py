__author__ = 'outcastgeek'

import logging
import transaction
import sys

from functools import partial

from pyramid_mailer.mailer import Mailer
from pyramid_mailer import get_mailer
from pyramid_mailer.message import Message

from ..async.srvc_mappings import zmq_service

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
        # mailer.send_immediately(message, fail_silently=False)
        log.info('Sucessfully sent emails to: %s', recipients)
    except: # catch *all* exceptions
        e = sys.exc_info()[0]
        log.error("Error: %s,\nfailed to email: %s", e)

def send_email_tx(settings=None,
                  recipients=None,
                  body=None,
                  subject="DO NOT REPLY",
                  sender="donotreply@oasys-corp.com",
                  ** kw):
    with transaction.manager:
        try:
            mailer =Mailer.from_settings(settings)
            message = Message(recipients=recipients, body=body, subject=subject, sender=sender)
            mailer.send(message)
            log.info('Sucessfully sent emails to: %s', recipients)
        except: # catch *all* exceptions
            e = sys.exc_info()[0]
            log.error("Error: %s,\nfailed to email: %s", e)

@zmq_service(srvc_name='send_email_task')
def send_email_task(data, settings=None):
    email_tx_send = partial(send_email_tx, settings)
    email_tx_send(**data)

def includeme(config):
    settings = config.registry.settings
    # config.registry['mailer'] = Mailer.from_settings(settings)
    configure_send_email = lambda request: partial(send_email, request)
    config.add_request_method(configure_send_email, 'send_email', reify=True)

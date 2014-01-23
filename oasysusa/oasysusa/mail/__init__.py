__author__ = 'outcastgeek'

import logging
import transaction

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
    # mailer = Mailer()
    mailer =get_mailer(request)
    message = Message(recipients=recipients, body=body, subject=subject, sender=sender)
    mailer.send(message)
    # mailer.send_immediately(message, fail_silently=False)

def send_email_tx(settings=None,
                  recipients=None,
                  body=None,
                  subject="DO NOT REPLY",
                  sender="donotreply@oasys-corp.com",
                  ** kw):
    with transaction.manager:
        mailer =Mailer.from_settings(settings)
        message = Message(recipients=recipients, body=body, subject=subject, sender=sender)
        mailer.send(message)

@zmq_service(srvc_name='send_email_task')
def send_email_task(data, settings=None):
    email_tx_send = partial(send_email_tx, settings)
    email_tx_send(**data)

def includeme(config):
    settings = config.registry.settings
    # config.registry['mailer'] = Mailer.from_settings(settings)
    configure_send_email = lambda request: partial(send_email, request)
    config.add_request_method(configure_send_email, 'send_email', reify=True)

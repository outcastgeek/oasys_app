__author__ = 'outcastgeek'

import logging

from formencode import Schema
from formencode.validators import (
    NotEmpty )

from pyramid.renderers import render
from pyramid_simpleform import Form
from pyramid_simpleform.renderers import FormRenderer

log = logging.getLogger('oasysusa')


def form(request, type, query_string=None):
    form = Form(request,
                schema=SearchSchema())
    response = render('templates/partials/search_partial.jinja2',
                      dict(renderer=FormRenderer(form), search_type=type,
                           query_string=query_string))
    return response


class SearchSchema(Schema):
    allow_extra_fields = True
    filter_extra_fields = True
    query = NotEmpty


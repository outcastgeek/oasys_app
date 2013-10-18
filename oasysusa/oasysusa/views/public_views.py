import colander
from deform import Form
from pyramid.response import Response
from pyramid.view import view_config

from pyramid.security import (
    authenticated_userid)

from sqlalchemy.exc import DBAPIError

from ..models import MyModel

from ..mixins.sqla import Q


@view_config(route_name='home', renderer='templates/home.jinja2')
def my_view(request):
    logged_in = authenticated_userid(request)
    try:
        one = Q(MyModel, MyModel.name == 'one').first()
    except DBAPIError:
        return Response(conn_err_msg, content_type='text/plain', status_int=500)
    return {'one': one, 'logged_in': logged_in}

conn_err_msg = """\
Pyramid is having a problem using your SQL database.  The problem
might be caused by one of the following things:

1.  You may need to run the "initialize_oasysusa_db" script
    to initialize your database tables.  Check your virtual 
    environment's "bin" directory for this script and try to run it.

2.  Your database server may not be running.  Check that the
    database server referred to by the "sqlalchemy.url" setting in
    your "development.ini" file is running.

After you fix the problem, please restart the Pyramid application to
try it again.
"""

@view_config(route_name='contact', renderer='templates/contact.jinja2')
def contact_view(request):
    logged_in = authenticated_userid(request)
    return {'logged_in': logged_in}

######## TRYING DEFORM #########
class Person(colander.MappingSchema):
    name = colander.SchemaNode(colander.String())
@view_config(route_name='try_deform', renderer='templates/try_deform.jinja2')
def try_deform(request):
    schema = Person()
    myform = Form(schema, buttons=('submit',))

    return {
        "form": myform
    }

import colander

from beaker.cache import cache_region
from deform import Form
from pyramid.response import Response
from pyramid.view import view_config

from sqlalchemy.exc import DBAPIError

from ..models import MyModel

@cache_region('long_term', 'my_model')
def get_my_model(name):
    return MyModel.query().filter(MyModel.name == name).first()

@view_config(route_name='home', renderer='templates/home.jinja2')
def my_view(request):
    try:
        one = get_my_model('one')
    except DBAPIError:
        return Response(conn_err_msg, content_type='text/plain', status_int=500)
    return {'one': one}

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
import os
import sys
import transaction

from sqlalchemy import engine_from_config

from pyramid.paster import (
    get_appsettings,
    setup_logging,
    )

from ..models import (
    DBSession,
    Base,
    MyModel,
    Project,
    Employee)


def usage(argv):
    cmd = os.path.basename(argv[0])
    print('usage: %s <config_uri>\n'
          '(example: "%s development.ini")' % (cmd, cmd))
    sys.exit(1)


def main(argv=sys.argv):
    if len(argv) != 2:
        usage(argv)
    config_uri = argv[1]
    setup_logging(config_uri)
    settings = get_appsettings(config_uri)
    engine = engine_from_config(settings, 'sqlalchemy.')
    DBSession.configure(bind=engine)
    Base.metadata.create_all(engine)
    with transaction.manager:
        model = MyModel(name='one', value=1)
        DBSession.add(model)
        project1 = Project(name="Project1", client="Client1", description="Description1",
                           email="Email1", address="Address1", telephone_number="TelephoneNumber1")
        DBSession.add(project1)
        project2 = Project(name="Project2", client="Client2", description="Description2",
                           email="Email2", address="Address2", telephone_number="TelephoneNumber2")
        DBSession.add(project2)
        admin = Employee(username='admin', password='OneAdmin13')
        DBSession.add(admin)
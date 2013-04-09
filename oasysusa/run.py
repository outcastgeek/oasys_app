#! /usr/bin/env python
"""
Usage: run.py [-myzdpirtu] [--migrate | --upgrade | --history | --development | --production | --install | --remove | --test | --update]

-h --help            show this help message and exit
--version            show version and exit
-m --migrate         migrates the database
-y --upgrade         upgrades the database
-z --history         retrieves the database history
-d --development     runs the application in development mode
-p --production      runs the application in production mode
-i --install         installs the application
-r --remove          removes the application
-t --test            tests the application
-u --update          updates the dependencies

Examples:
  run.py -i
  run.py --test

"""

import datetime

now = datetime.datetime.now().strftime("%Y-%m-%d_%Hh%Mm%Ss")

from subprocess import call

from docopt import docopt


if __name__ == '__main__':

    args = docopt(__doc__,
                  version='0.0',
                  options_first=True)

    #print('global arguments:')
    #print(args)

    try:
      if args['--migrate']:
          print("Migrating database...")
          #call('~/ENV/bin/initialize_oasysusa_db development.ini', shell=True)
          #call('~/ENV/bin/initialize_oasysusa_db production.ini', shell=True)
          call(['~/ENV/bin/alembic revision --autogenerate -m %s' % now], shell=True)
      elif args['--upgrade']:
          print("Upgrading database...")
          call('~/ENV/bin/alembic upgrade head', shell=True)
      elif args['--history']:
          print("Database history...")
          call('~/ENV/bin/alembic history', shell=True)
          print('Run this: "alembic downgrade revision" to downgrade to the specified revision')
      elif args['--development']:
          print("Running in DEV mode...")
          # In case subcommand is a script in some other programming language:
          print("Starting application...")
          exit(call('~/ENV/bin/pserve development.ini', shell=True))
      elif args['--production']:
          print("Running in PROD mode...")
          # In case subcommand is a script in some other programming language:
          print("Starting application...")
          #exit(call('ulimit -u unlimited && ~/ENV/bin/uwsgi --ini-paste production.ini', shell=True))
          exit(call('~/ENV/bin/uwsgi --ini-paste production.ini', shell=True))
          #exit(call('~/ENV/bin/pserve production.ini', shell=True))
      elif args['--install']:
          print("Installing application...")
          # In case subcommand is a script in some other programming language:
          exit(call('~/ENV/bin/pip install -e .', shell=True))
      elif args['--remove']:
          print("Removing application...")
          # In case subcommand is a script in some other programming language:
          exit(call('~/ENV/bin/pip uninstall PyAlchemy', shell=True))
      elif args['--test']:
          print("Testing application...")
          # In case subcommand is a script in some other programming language:
          exit(call('~/ENV/bin/nosetests', shell=True))
      elif args['--update']:
          print("Updating dependencies...")
          # In case subcommand is a script in some other programming language:
          exit(call('~/ENV/bin/pip install -r ../requirements.txt --upgrade', shell=True))
      else:
          print("See run.py --help")
    except KeyboardInterrupt:
        print "\nExiting..."

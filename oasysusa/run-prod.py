#! /usr/bin/env python
"""
Usage: run.py [-myzdpirtbu] [--migrate | --upgrade | --history | --development | --production | --install | --remove | --test | --behave | --update]

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
-b --behave          verifies the application's behavior
-u --update          updates the dependencies

Examples:
  run.py -i
  run.py --test

"""

import gevent
# from gevent import monkey; monkey.patch_all()

import datetime

from multiprocessing import Pool, Process

now = datetime.datetime.now().strftime("%Y-%m-%d_%Hh%Mm%Ss")

import os

def numCPUs():
    if not hasattr(os, "sysconf"):
        raise RuntimeError("No sysconf detected.")
    return os.sysconf("SC_NPROCESSORS_ONLN")

cpus = numCPUs() * 4 + 1

def start_prod_server(prop_ini):
    print("Starting application...")
    # exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/python gevent_server.py', shell=True))
    # exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/python tornado_server.py', shell=True))
    # exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/pserve production.ini', shell=True))
    # exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/gunicorn_paster --backlog=2048 --worker-class=gevent --workers=%d production.ini' % cpus, shell=True))
    # exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/gunicorn_paster --backlog=2048 --worker-class=tornado --workers=%d %s' % (cpus, prop_ini), shell=True))
    # exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/gunicorn_paster --backlog=2048 --worker-class=gevent --workers=%d %s' % (cpus, prop_ini), shell=True))
    pid = '/home/oasysusa/oasys_corp/logs/oasysusa.pid'
    exit(call('ulimit -n 16384 && /home/oasysusa/ENV/bin/pserve --monitor-restart --pid-file=%s %s' % (pid, prop_ini), shell=True))

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
          #call('/home/oasysusa/ENV/bin/initialize_oasysusa_db development.ini', shell=True)
          #call('/home/oasysusa/ENV/bin/initialize_oasysusa_db production.ini', shell=True)
          call(['/home/oasysusa/ENV/bin/alembic --config alembic-prod.ini revision --autogenerate -m "%s"' % now], shell=True)
      elif args['--upgrade']:
          print("Upgrading database...")
          call('/home/oasysusa/ENV/bin/alembic --config alembic-prod.ini upgrade head', shell=True)
      elif args['--history']:
          print("Database history...")
          call('/home/oasysusa/ENV/bin/alembic --config alembic-prod.ini history', shell=True)
          print('Run this: "alembic downgrade revision" to downgrade to the specified revision')
      elif args['--development']:
          print("Running in DEV mode...")
          # In case subcommand is a script in some other programming language:
          print("Starting application...")
          exit(call('/home/oasysusa/ENV/bin/pserve development.ini', shell=True))
      elif args['--production']:
          print("Running in PROD mode...")
          # In case subcommand is a script in some other programming language:
          # gevent.joinall(map(lambda prop_ini : gevent.spawn(start_prod_server, prop_ini),
          #                    ['production0.ini', 'production1.ini', 'production2.ini', 'production3.ini']))
          # props = [['production0.ini'], ['production1.ini'], ['production2.ini'], ['production3.ini']]
          # pool = Pool(processes=len(props))
          # pool.map(lambda prop_ini : start_prod_server(prop_ini), props)
          start_prod_server('production.ini')
      elif args['--install']:
          print("Installing application...")
          # In case subcommand is a script in some other programming language:
          exit(call('/home/oasysusa/ENV/bin/pip install -e .', shell=True))
      elif args['--remove']:
          print("Removing application...")
          # In case subcommand is a script in some other programming language:
          exit(call('/home/oasysusa/ENV/bin/pip uninstall oasysusa', shell=True))
      elif args['--test']:
          print("Testing application...")
          # In case subcommand is a script in some other programming language:
          exit(call('/home/oasysusa/ENV/bin/nosetests', shell=True))
      elif args['--behave']:
          print("Verifying application's behavior...")
          # In case subcommand is a script in some other programming language:
          exit(call('~/ENV/bin/behave oasysusa/tests/features', shell=True))
      elif args['--update']:
          print("Updating dependencies...")
          # In case subcommand is a script in some other programming language:
          exit(call('/home/oasysusa/ENV/bin/pip install -r ../requirements.txt --upgrade', shell=True))
      else:
          print("See run.py --help")
    except KeyboardInterrupt:
        print "\nExiting..."

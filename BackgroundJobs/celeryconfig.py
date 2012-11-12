
## Broker settings.
BROKER_URL = 'redis://localhost:6379/0'

## Pool
CELERYD_POOL = 'gevent'
#CELERYD_CONCURRENCY = 16

# List of modules to import when celery starts.
CELERY_IMPORTS = ("tasks.tasks", )

## Using the database to store task state and results.
CELERY_RESULT_BACKEND = 'redis://localhost:6379/0'

CELERY_REDIS_MAX_CONNECTIONS = 4000

CELERY_ANNOTATIONS = {"tasks.add": {"rate_limit": "10/s"},
                      "tasks.fib": {"rate_limit": "10/s"},
                      "tasks.retry_stream": {"rate_limit": "10/s"},}

from datetime import timedelta

CELERYBEAT_SCHEDULE = {
    'runs-add-every-1-seconds': {
        'task': 'tasks.tasks.add',
        'schedule': timedelta(seconds=1),
        'args': (16, 16)
    },
    'runs-fib-every-1-seconds': {
        'task': 'tasks.tasks.fib',
        'schedule': timedelta(seconds=1),
        'args': (1024,)
    },
    'runs-devide_and_conquer-every-1-seconds': {
        'task': 'tasks.tasks.retry_stream',
        'schedule': timedelta(seconds=1),
        'args': (10,20,2,)
    },
}

CELERY_TIMEZONE = 'UTC'


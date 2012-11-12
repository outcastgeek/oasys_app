
## Broker settings.
BROKER_URL = 'redis://localhost:6379/0'

## Pool
CELERYD_POOL='gevent'

# List of modules to import when celery starts.
CELERY_IMPORTS = ("tasks", )

## Using the database to store task state and results.
CELERY_RESULT_BACKEND = 'redis://localhost:6379/0'

CELERY_REDIS_MAX_CONNECTIONS = 4000

CELERY_ANNOTATIONS = {"add": {"rate_limit": "10/s"},
                      "fib": {"rate_limit": "10/s"}}

from datetime import timedelta

CELERYBEAT_SCHEDULE = {
    'runs-add-every-0.4-seconds': {
        'task': 'add',
        'schedule': timedelta(seconds=0.4),
        'args': (16, 16)
    },
    'runs-fib-every-0.4-seconds': {
        'task': 'fib',
        'schedule': timedelta(seconds=0.4),
        'args': (1024,)
    },
}

CELERY_TIMEZONE = 'UTC'



import pyximport; pyximport.install()

## Broker settings.
BROKER_URL = 'redis://localhost:6379/0'

# List of modules to import when celery starts.
CELERY_IMPORTS = ("tasks.tasks", )

## Using the database to store task state and results.
CELERY_RESULT_BACKEND = 'redis://localhost:6379/0'

CELERY_REDIS_MAX_CONNECTIONS = 4000

CELERY_ANNOTATIONS = {"tasks.add": {"rate_limit": "10/s"},
                      "tasks.fib": {"rate_limit": "10/s"}}

from datetime import timedelta

CELERYBEAT_SCHEDULE = {
    'runs-add-every-0.4-seconds': {
        'task': 'tasks.tasks.add',
        'schedule': timedelta(seconds=0.4),
        'args': (16, 16)
    },
    'runs-fib-every-0.4-seconds': {
        'task': 'tasks.tasks.fib',
        'schedule': timedelta(seconds=0.4),
        'args': (1024,)
    },
}

CELERY_TIMEZONE = 'UTC'


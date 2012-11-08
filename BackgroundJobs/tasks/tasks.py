
from celery import Celery
from celery.contrib import rdb

celery = Celery()
celery.config_from_object('celeryconfig')

from celery.utils.log import get_task_logger

logger = get_task_logger(__name__)

@celery.task
def add(x, y):
    logger.debug('Adding %s + %s' % (x, y))
    return x + y

@celery.task
def fib(n):
    logger.debug('Return Fibonacci series up to %s' % n)
    result = []
    a, b = 0, 1
    while b < n:
        result.append(b)
        a, b = b, a+b
    return result

if __name__ == '__main__':
    celery.worker_main()


from celery import Celery
from celery.contrib import rdb

celery = Celery()
#celery.config_from_object('celeryconfig')

@celery.task
def add(x, y):
    return x + y

@celery.task
def fib(n): # return Fibonacci series up to n
    result = []
    a, b = 0, 1
    while b < n:
        result.append(b)
        a, b = b, a+b
    return result

if __name__ == '__main__':
    celery.worker_main()

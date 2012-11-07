
#import pyximport; pyximport.install()
#import cython

from celery import Celery
from celery.contrib import rdb

celery = Celery()
#celery.config_from_object('celeryconfig')

@celery.task
#@cython.cfunc
#@cython.locals(x=cython.int, y=cython.int)
def add(x, y):
    return x + y

@celery.task
#@cython.cfunc
#@cython.returns(cython.int)
#@cython.locals(n=cython.int)
def fib(n): # return Fibonacci series up to n
    result = []
    a, b = 0, 1
    while b < n:
        result.append(b)
        a, b = b, a+b
    return result

if __name__ == '__main__':
    celery.worker_main()

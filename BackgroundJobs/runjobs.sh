
..\..\ENV\Scripts\celery.exe worker --config=celeryconfig --loglevel=debug

..\..\ENV\Scripts\celery.exe beat --config=celeryconfig

..\..\ENV\Scripts\celery.exe flower --config=celeryconfig

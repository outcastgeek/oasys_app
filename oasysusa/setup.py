import os

from setuptools import setup, find_packages

here = os.path.abspath(os.path.dirname(__file__))
README = open(os.path.join(here, 'README.txt')).read()
CHANGES = open(os.path.join(here, 'CHANGES.txt')).read()

requires = [
    'pyramid',
    'pyramid_jinja2',
    'pyramid_webassets',
    'pyramid_beaker',
    'pyramid_simpleform',
    'pyramid_deform',
    'SQLAlchemy',
    'transaction',
    'pyramid_tm',
    'pyramid_mailer',
    'pyramid_debugtoolbar',
    'pyramid_exclog',
    'zope.sqlalchemy',
    'alembic',
    'waitress',
    'tornado',
    'passlib',
    'velruse',
    'pyramid_persona',
    'requests',
    'mock',
    'behave',
    ]

setup(name='oasysusa',
      version='0.0',
      description='oasysusa',
      long_description=README + '\n\n' + CHANGES,
      classifiers=[
        "Programming Language :: Python",
        "Framework :: Pyramid",
        "Topic :: Internet :: WWW/HTTP",
        "Topic :: Internet :: WWW/HTTP :: WSGI :: Application",
        ],
      author='',
      author_email='',
      url='',
      keywords='web wsgi bfg pylons pyramid',
      packages=find_packages(),
      include_package_data=True,
      zip_safe=False,
      test_suite='oasysusa',
      install_requires=requires,
      entry_points="""\
      [beaker.backends]
      mongodb = oasysusa.sessions_storage:MongoDBNamespaceManager
      [paste.app_factory]
      main = oasysusa:main
      [paste.server_runner]
      run = oasysusa:serve_paste
      [console_scripts]
      initialize_oasysusa_db = oasysusa.scripts.initializedb:main
      """,
      )

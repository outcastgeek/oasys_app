#!/bin/python

from cuisine import *
#from cuisine_postgresql import postgresql_role_ensure, postgresql_database_ensure
from fabric.api import *
from fabric.context_managers import *
from fabric.utils import puts
from fabric.colors import red, green

env.hosts = ['vagrant@127.0.0.1:2222']

puts(green('Provisionning server...'))

def setup_packages():
    #Ubuntu
    puts(green('Installing Ubuntu packages'))
    sudo('apt-get update')
    package_ensure('python-software-properties')
    sudo('add-apt-repository --yes ppa:gophers/go')
    sudo('apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10')
    sudo('echo "deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen" | tee -a /etc/apt/sources.list')
    sudo('apt-get update')
    package_ensure('build-essential')
    package_ensure('nginx')
    package_ensure('python-dev')
    package_ensure('vim')
    package_ensure('emacs')
    package_ensure('postgresql')
    package_ensure('postgis')
    package_ensure('postgresql-contrib')
    package_ensure('postgresql-server-dev-all')
    package_ensure('mongodb-10gen')
    package_ensure('git-core')
    package_ensure('ufw') # may have to install by hand
    package_ensure('tree')
    package_ensure('libzmq-dev')
    
    #Python
    puts(green('Installing Python packages'))
    package_ensure('python-setuptools')
    package_ensure('python-pip')
    python_package_install_easy_install('supervisor')
    python_package_install_easy_install('psycopg2')
    python_package_install_easy_install('pil')
    python_package_install_easy_install('cython')
    python_package_install_easy_install('mercurial')
    python_package_install_easy_install('pyzmq')
    python_package_install_easy_install('fabric')
    python_package_install_easy_install('watchdog')
    python_package_install_easy_install('requests')
    
    #JVM
    puts(green('Installing JVM packages'))
    package_ensure('openjdk-7-jdk')
    package_ensure('ant')
    package_ensure('maven')
    sudo('update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java')

    #Other
    puts(green('Installing additional software'))
    package_ensure('golang-stable')
    package_ensure('sbcl')
    package_ensure('chicken-bin')
    package_ensure('libchicken-dev')
    package_ensure('ecl')
    package_ensure('gambc')
    package_ensure('libgambc4-dev')

def setup_users():
    puts(green('Creating Ubuntu users'))   
    user_ensure(name='oasystech', passwd='OasysTech2013!')

    #def configure_database():
    #    postgresql_role_ensure('postgres', 'PgSQL2012!', createdb=True)

def bootstrap():
    puts(green('Boostrapping...'))
    setup_packages()
    setup_users()

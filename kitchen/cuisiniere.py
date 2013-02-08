#!/bin/python

from cuisine import *
from fabric.api import *
from fabric.context_managers import *
from fabric.utils import puts
from fabric.colors import red, green

env.hosts = ['vagrant@127.0.0.1:2222']

def setup_packages():
    puts(green('Installing Ubuntu packages'))
    sudo('apt-get update')
    package_ensure('python-software-properties')
    #sudo('add-apt-repository ppa:webupd8team/java')
    #sudo('apt-get update')
    
    package_ensure('build-essential')
    package_ensure('nginx')
    package_ensure('postgresql')
    package_ensure('postgis')
    package_ensure('postgresql-contrib')
    package_ensure('supervisor')
    package_ensure('git-core')
    package_ensure('ufw') # may have to install by hand
    
    package_ensure('python-pip')
    package_ensure('python-psycopg2')
    package_ensure('python-imaging')

    package_ensure('cython')
    package_ensure('mercurial')
    package_ensure('tree')
    package_ensure('openjdk-7-jdk')
    package_ensure('ant')
    package_ensure('maven')

def setup_users():
    puts(green('Creating Ubuntu users'))
    
    user_ensure('oasystech')
    

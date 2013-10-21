#!/bin/python

import logging
import os

from ConfigParser import ConfigParser

from cuisine import *
from cuisine_postgresql import *
from fabric.api import *
from fabric.context_managers import *
from fabric.utils import puts
from fabric.colors import *
from fabric.operations import local as lrun

# Setup a logger
log = logging.getLogger(__file__)
log.addHandler(logging.StreamHandler())
log.setLevel(logging.DEBUG)

# Configuration
__location__ = os.path.realpath(os.path.join(os.getcwd(), os.path.dirname(__file__)))
config = ConfigParser()
config.read(os.path.join(__location__, 'fab.ini'))
def read_value(local, value):
    try:
        log.info("Reading value: <<%s>> from section: <<%s>>...", value, local)
        return config.get(local, value)
    except:
        log.error("Could not read value: <<%s>> or section: <<%s>>...", value, local)
        raise

# Roles definition
env.roledefs = {
    'local': ['vagrant@127.0.0.1:2222'],
    'polyglot': ['root@166.78.121.77'],
    'poly': ['root@198.61.175.146'],
    'og': ['root@outcastgeek.com']
}
# Use qxYnD6LnRS4H
# Use jPYM53shyN87

def check_VM_Specs():
    sudo('dmesg | grep CPU')
    sudo('dmidecode --type memory')
    sudo('df -kP')
    sudo('cat /proc/meminfo')
    sudo('cat /proc/stat')

def setup_packages(local='retina'):
    #Ubuntu
    puts(green('Installing Ubuntu packages'))
    sudo('apt-get update')
    package_ensure('python-software-properties')
    sudo('add-apt-repository --yes ppa:gophers/go')
    # sudo('apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10')
    # sudo('echo "deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen" | tee -a /etc/apt/sources.list')
    sudo('apt-get update')
    package_ensure('build-essential')
    package_ensure('curl')
    package_ensure('wget')
    package_ensure('nginx')
    package_ensure('python-dev')
    package_ensure('vim')
    package_ensure('emacs')
    #package_ensure('postgresql')
    #package_ensure('postgis')
    #package_ensure('postgresql-contrib')
    package_ensure('postgresql-server-dev-all')
    # package_ensure('mongodb-10gen')
    package_ensure('redis-server')
    package_ensure('libmemcached-dev')
    package_ensure('memcached')
    package_ensure('git-core')
    package_ensure('ufw') # may have to install by hand
    package_ensure('tree')
    package_ensure('zsh')
    package_ensure('libzmq-dev')
    package_ensure('libssl-dev')
    package_ensure('libreadline6-dev')
    package_ensure('libyaml-dev')
    package_ensure('libsqlite3-dev')
    package_ensure('sqlite3')
    package_ensure('libxml2-dev')
    package_ensure('libxslt1-dev')
    package_ensure('autoconf')
    package_ensure('libgdbm-dev')
    package_ensure('libncurses5-dev')
    package_ensure('automake')
    package_ensure('libtool')
    package_ensure('bison')
    package_ensure('pkg-config')
    package_ensure('libffi-dev')
    
    #Python
    puts(green('Installing Python SetupTools and Pip'))
    package_ensure('python-setuptools')
    package_ensure('python-pip')
    install_python_packages(local)
    
    #JVM
    # puts(green('Installing JVM packages'))
    # package_ensure('openjdk-7-jdk')
    # package_ensure('ant')
    # package_ensure('maven')
    # sudo('update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java')

    #JRuby
    #sudo('curl -L https://get.rvm.io | bash -s stable --ruby=2.0.0')
    #sudo('rvm use ruby')
    #sudo('gem update --system')
    #sudo('gem update')
    #sudo('gem install bundler foreman')
    #sudo('rvm install 1.7.3')
    #sudo('rvm use 1.7.3')
    #sudo('gem update --system')
    #sudo('gem update')
    #sudo('gem install therubyrhino jruby-openssl bundler')

    #Other
    #puts(green('Installing additional software'))
    #package_ensure('golang-stable')
    #package_ensure('sbcl')
    #package_ensure('chicken-bin')
    #package_ensure('libchicken-dev')
    #package_ensure('ecl')
    #package_ensure('gambc')
    #package_ensure('libgambc4-dev')
    #package_ensure('haskell-platform')

def setup_users():
    puts(green('Creating Ubuntu users'))   
    user_ensure(name='oasysusa', passwd='OasysTech2013!')
    puts(green('Installing Python Base ENV Packages for App'))
    sudo('virtualenv /home/oasysusa/ENV', user='oasysusa')
    sudo('/home/oasysusa/ENV/bin/pip install -r /etc/requirements.txt --upgrade', user='oasysusa')

def install_python_packages(local='retina'):
    puts(green('Installing Python packages'))
    puts(green('Putting new requirements.txt file'))
    py_requirements_tpl = open(read_value(local, 'py_requirements_tpl'), 'r')
    log.info(py_requirements_tpl)
    py_requirements_location = '/etc/requirements.txt'
    sudo('touch ' + py_requirements_location)
    file_write(py_requirements_location, py_requirements_tpl.read(), owner='root', sudo=True)
    sudo('pip install --upgrade setuptools', user='root')
    sudo('pip install -r /etc/requirements.txt --upgrade', user='root')

def configure_database():
    puts(green('Creating PostgreSQL users'))  
    postgresql_role_ensure('oasysusa', 'OasysTech2013!', createdb=True)
    postgresql_database_ensure('oasysusa_storage',
                                   owner='oasysusa',
                                   template='template0',
                                   encoding='LATIN1')

def check_tables():
    puts(green('Checking Tables'))
    sudo('psql -d oasysusa_storage -l', user='oasysusa')
    sudo('psql -d oasysusa_storage -c "select * from information_schema.tables where table_schema = \'public\';"', user='oasysusa')

def get_nginx():
    puts(green('Getting existing nginx.conf'))
    get('/etc/nginx/nginx.conf', '/Users/outcastgeek/workspace/oasys_corp/conf')

def put_nginx(local='retina'):
    puts(green('Putting new nginx.conf'))
    nginx_tpl = open(read_value(local, 'nginx_tpl'),'r')
    log.info(nginx_tpl)
    nginx_location = '/etc/nginx/nginx.conf'
    sudo('touch ' + nginx_location)
    file_write(nginx_location, nginx_tpl.read(), owner='root', sudo=True)
    #run('nginx -s stop && nginx')

def put_service(local='retina'):
    puts(green('Putting new service script'))
    service_tpl = open(read_value(local, 'service_tpl'), 'r')
    log.info(service_tpl)
    service_location = '/sbin/service'
    sudo('touch ' + service_location)
    file_write(service_location, service_tpl.read(), owner='oasysusa', sudo=True)

def put_functions(local='retina'):
    puts(green('Putting new functions script'))
    functions_tpl = open(read_value(local, 'functions_tpl'), 'r')
    log.info(functions_tpl)
    functions_location = '/etc/init.d/functions'
    sudo('touch ' + functions_location)
    file_write(functions_location, functions_tpl.read(), owner='oasysusa', sudo=True)

def put_oasysusa(local='retina'):
    puts(green('Putting new oasysusa script'))
    oasysusa_tpl = open(read_value(local, 'oasysusa_tpl'), 'r')
    log.info(oasysusa_tpl)
    oasysusa_location = '/etc/init.d/oasysusa'
    sudo('touch ' + oasysusa_location)
    file_write(oasysusa_location, oasysusa_tpl.read(), mode='a+rx', owner='oasysusa', sudo=True)
    #sudo('chmod a=x ' + oasysusa_location)
    oasysusa_conf = '/etc/init/oasysusa.conf'
    sudo('touch ' + oasysusa_conf)
    file_write(oasysusa_conf, "# this is an abstract oasysusa job containing only a comment", owner='oasysusa', sudo=True)

def put_system_health():
    puts(green('Putting new health script'))
    health_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/system-health.py','r')
    health_location = '/home/vagrant/system-health.py'
    sudo('touch ' + health_location)
    file_write(health_location, health_tpl.read(), owner='oasysusa', sudo=True)

def put_Zsh_Conf(local='retina'):
    puts(green('Putting new Zsh Configuration'))
    zsh_tpl = open(read_value(local, 'zsh_tpl'), 'r')
    log.info(zsh_tpl)
    # zsh_location = '/home/oasysusa/.zshrc'
    # zsh_location = '/home/vagrant/.zshrc'
    #zsh_location = '/home/root/.zshrc'
    zsh_location = '/root/.zshrc'
    sudo('touch ' + zsh_location)
    file_write(zsh_location, zsh_tpl.read(), owner='oasysusa', sudo=True)
    # file_write(zsh_location, zsh_tpl.read(), owner='vagrant', sudo=True)
    # file_write(zsh_location, zsh_tpl.read(), owner='root', sudo=True)

def bootstrap(local='retina'):
    puts(green('Provisionning server...'))
    setup_packages(local)
    setup_users()
    #configure_database()
    put_service(local)
    put_functions(local)
    put_nginx(local)
    put_oasysusa(local)
    #put_system_health()
    put_Zsh_Conf(local)

def check_JMV_Processes():
    run('ps -ef | grep java')

def check_processes():
    run('service nginx status')
    run('service mongodb status')
    run('service oasysusa status')

def migrate_oasys_db():
    with cd('/home/oasysusa/oasys_corp/oasysusa'):
        sudo('/home/oasysusa/ENV/bin/python run-prod.py -y', user='oasysusa')

def update_dependencies():
    # with cd('/home/oasysusa/oasys_corp/oasysusa'):
    #     sudo('/home/oasysusa/ENV/bin/python run-prod.py -u', user='oasysusa')
    with cd('/home/oasysusa/oasys_corp'):
        sudo('/home/oasysusa/ENV/bin/pip install -r requirements.txt --upgrade', user='oasysusa')

def install_app():
    with cd('/home/oasysusa/oasys_corp/oasysusa'):
        sudo('/home/oasysusa/ENV/bin/python run-prod.py -i', user='oasysusa')

def remove_app():
    with cd('/home/oasysusa/oasys_corp/oasysusa'):
        sudo('/home/oasysusa/ENV/bin/python run-prod.py -r', user='oasysusa')

def get_oasys():
    try:
        with cd('/home/oasysusa'):
            sudo('hg clone https://outcastgeek@bitbucket.org/outcastgeek/oasys_corp -r pyramid', user='oasysusa')
        update_dependencies()
        install_app()
        migrate_oasys_db()
    except:
        refresh_oasys()

def refresh_oasys():
    with cd('/home/oasysusa/oasys_corp'):
        sudo('hg pull && hg update pyramid', user='oasysusa')
    update_dependencies()
    install_app()
    migrate_oasys_db()
    sudo('/etc/init.d/oasysusa restart &', user='oasysusa')

def hard_refresh_oasys():
    remove_app()
    refresh_oasys()

def refresh_oasys_from_local(local='retina'):
    remove_app()
    app_project = read_value(local, 'app_project')
    put(app_project, '/tmp')
    sudo('cd /home/oasysusa/oasys_corp/oasysusa && rm -r * && cp -r /tmp/oasysusa/* .', user='oasysusa')
    sudo('rm -r /tmp/oasysusa')
    install_app()

def up_start():
    upstart_ensure('nginx')
    # upstart_ensure('mongodb')
    upstart_ensure('redis-server')
    # upstart_ensure('memcached')
    #upstart_ensure('oasysusa')
    try:
        run('kill -9 $(ps -ef | grep uwsgi | awk \'{print $2}\')')
    except:
        print 'Oops!!!!'
    with cd('/home/oasysusa/oasys_corp'):
        sudo('memcached &', user='oasysusa')
        sudo('/etc/init.d/oasysusa restart', user='root')

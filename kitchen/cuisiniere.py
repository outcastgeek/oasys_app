#!/bin/python

from cuisine import *
#from cuisine_postgresql import postgresql_role_ensure, postgresql_database_ensure
from fabric.api import *
from fabric.context_managers import *
from fabric.utils import puts
from fabric.colors import red, green


env.roledefs = {
    'local': ['vagrant@127.0.0.1:2222'],
    'og': ['root@outcastgeek.com']
}

def check_VM_Specs():
    run('dmesg | grep CPU')
    run('dmidecode --type memory')

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
    package_ensure('curl')
    package_ensure('wget')
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
    package_ensure('zsh')
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
    python_package_install_easy_install('monitoring')
    python_package_install_easy_install('requests')
    python_package_install_easy_install('honcho')
    
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
    user_ensure(name='oasysusa', passwd='OasysTech2013!')

    #def configure_database():
    #    postgresql_role_ensure('postgres', 'PgSQL2012!', createdb=True)

def get_nginx():
    puts(green('Getting existing nginx.conf'))
    get('/etc/nginx/nginx.conf', '/Users/outcastgeek/workspace/oasys_corp/conf')

def put_nginx():
    puts(green('Putting new nginx.conf'))
    put('/Users/outcastgeek/workspace/oasys_corp/conf', '/etc/nginx/nginx.conf')
    run('nginx -s stop && nginx')

def put_service():
    puts(green('Putting new service script'))
    service_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/service','r')
    service_location = '/sbin/service'
    sudo('touch ' + service_location)
    file_write(service_location, service_tpl.read(), owner='vagrant', sudo=True)

def put_functions():
    puts(green('Putting new functions script'))
    functions_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/functions','r')
    functions_location = '/etc/init.d/functions'
    sudo('touch ' + functions_location)
    file_write(functions_location, functions_tpl.read(), owner='vagrant', sudo=True)

def put_oasysusa():
    puts(green('Putting new oasysusa script'))
    oasysusa_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/oasysusa','r')
    oasysusa_location = '/etc/init.d/oasysusa'
    sudo('touch ' + oasysusa_location)
    file_write(oasysusa_location, oasysusa_tpl.read(), owner='oasysusa', sudo=True)
    sudo('chmod +x ' + oasysusa_location)
    oasysusa_conf = '/etc/init/oasysusa.conf'
    sudo('touch ' + oasysusa_conf)
    file_write(oasysusa_conf, "# this is an abstract oasysusa job containing only a comment", owner='oasysusa', sudo=True)

def put_system_health():
    puts(green('Putting new health script'))
    health_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/system-health.py','r')
    health_location = '/home/vagrant/system-health.py'
    sudo('touch ' + health_location)
    file_write(health_location, health_tpl.read(), owner='vagrant', sudo=True)

def bootstrap():
    puts(green('Provisionning server...'))
    setup_packages()
    setup_users()
    put_service()
    put_functions()
    put_oasysusa()
    #put_system_health()

def check_JMV_Processes():
    run('ps -ef | grep java')

def check_processes():
    run('service nginx status')
    run('service mongodb status')
    run('service oasysusa status')

def get_oasys():
    with cd('/home/oasysusa'):
        sudo('hg clone https://outcastgeek@bitbucket.org/outcastgeek/oasys_corp -r jvm')

def refresh_oasys():
    with cd('/home/oasysusa/oasys_corp'):
        sudo('hg pull -r jvm && hg update')

def up_start():
    upstart_ensure('nginx')
    upstart_ensure('mongodb')
    upstart_ensure('oasysusa')

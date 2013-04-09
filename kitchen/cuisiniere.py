#!/bin/python

from cuisine import *
from cuisine_postgresql import postgresql_role_ensure, postgresql_database_ensure
from fabric.api import *
from fabric.context_managers import *
from fabric.utils import puts
from fabric.colors import red, green


env.roledefs = {
    'local': ['vagrant@127.0.0.1:2222'],
    'polyglot': ['root@166.78.121.77'],
    'og': ['root@outcastgeek.com']
}
# Use qxYnD6LnRS4H

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
    #package_ensure('postgresql')
    #package_ensure('postgis')
    #package_ensure('postgresql-contrib')
    #package_ensure('postgresql-server-dev-all')
    package_ensure('mongodb-10gen')
    package_ensure('redis-server')
    package_ensure('git-core')
    package_ensure('ufw') # may have to install by hand
    package_ensure('tree')
    package_ensure('zsh')
    package_ensure('libzmq-dev')
    package_ensure('libssl-dev')
    
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
    python_package_install_easy_install('virtualenv')
    python_package_install_easy_install('monitoring')
    python_package_install_easy_install('requests')
    python_package_install_easy_install('honcho')
    
    #JVM
    puts(green('Installing JVM packages'))
    package_ensure('openjdk-7-jdk')
    package_ensure('ant')
    package_ensure('maven')
    sudo('update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java')

    #JRuby
    sudo('curl -L https://get.rvm.io | bash -s stable --ruby=1.9.3')
    sudo('rvm use ruby')
    sudo('gem update --system')
    sudo('gem update')
    sudo('gem install bundler foreman')
    #sudo('rvm install 1.7.3')
    #sudo('rvm use 1.7.3')
    #sudo('gem update --system')
    #sudo('gem update')
    #sudo('gem install therubyrhino jruby-openssl bundler')

    #Other
    puts(green('Installing additional software'))
    package_ensure('golang-stable')
    package_ensure('sbcl')
    #package_ensure('chicken-bin')
    #package_ensure('libchicken-dev')
    #package_ensure('ecl')
    #package_ensure('gambc')
    #package_ensure('libgambc4-dev')
    #package_ensure('haskell-platform')

def setup_users():
    puts(green('Creating Ubuntu users'))   
    user_ensure(name='oasysusa', passwd='OasysTech2013!')
    sudo('virtualenv ~/ENV', user='oasysusa')
    sudo('~/ENV/bin/pip install docopt --upgrade', user='oasysusa')
    sudo('~/ENV/bin/python run.py -u', user='oasysusa')

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

def put_nginx():
    puts(green('Putting new nginx.conf'))
    # nginx_tpl = open('/Users/outcastgeek/workspace/oasys_corp/conf/nginx.conf','r')
    nginx_tpl = open('/Users/a998807/ossworkspace/oasys_corp/conf/nginx.conf','r')
    nginx_location = '/etc/nginx/nginx.conf'
    sudo('touch ' + nginx_location)
    file_write(nginx_location, nginx_tpl.read(), owner='root', sudo=True)
    run('nginx -s stop && nginx')

def put_service():
    puts(green('Putting new service script'))
    # service_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/service','r')
    service_tpl = open('/Users/a998807/ossworkspace/oasys_corp/scripts/service','r')
    service_location = '/sbin/service'
    sudo('touch ' + service_location)
    file_write(service_location, service_tpl.read(), owner='oasysusa', sudo=True)

def put_functions():
    puts(green('Putting new functions script'))
    # functions_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/functions','r')
    functions_tpl = open('/Users/a998807/ossworkspace/oasys_corp/scripts/functions','r')
    functions_location = '/etc/init.d/functions'
    sudo('touch ' + functions_location)
    file_write(functions_location, functions_tpl.read(), owner='oasysusa', sudo=True)

def put_oasysusa():
    puts(green('Putting new oasysusa script'))
    # oasysusa_tpl = open('/Users/outcastgeek/workspace/oasys_corp/scripts/oasysusa','r')
    oasysusa_tpl = open('/Users/a998807/ossworkspace/oasys_corp/scripts/oasysusa','r')
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

def bootstrap():
    puts(green('Provisionning server...'))
    setup_packages()
    setup_users()
    #configure_database()
    put_service()
    put_functions()
    put_nginx()
    put_oasysusa()
    #put_system_health()

def check_JMV_Processes():
    run('ps -ef | grep java')

def check_processes():
    run('service nginx status')
    run('service mongodb status')
    run('service oasysusa status')

def migrate_oasys_db():
    with cd('/home/oasysusa/oasys_corp/oasysusa'):
        sudo('~/ENV/bin/python run.py -y', user='oasysusa')

def update_dependencies():
    with cd('/home/oasysusa/oasys_corp/oasysusa'):
        sudo('~/ENV/bin/python run.py -u', user='oasysusa')

def get_oasys():
    try:
        with cd('/home/oasysusa'):
            sudo('hg clone https://outcastgeek@bitbucket.org/outcastgeek/oasys_corp -r pyramid', user='oasysusa')
        update_dependencies()
        migrate_oasys_db()
    except:
        refresh_oasys()

def refresh_oasys():
    with cd('/home/oasysusa/oasys_corp'):
        sudo('hg pull -r jvm && hg update', user='oasysusa')
    update_dependencies()
    migrate_oasys_db()
    sudo('/etc/init.d/oasysusa restart &', user='oasysusa')

def up_start():
    upstart_ensure('nginx')
    upstart_ensure('mongodb')
    upstart_ensure('redis-server')
    #upstart_ensure('oasysusa')
    with cd('/home/oasysusa/oasys_corp'):
        sudo('/etc/init.d/oasysusa start', user='oasysusa')

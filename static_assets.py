
from os import path
from webassets import Bundle, Environment
from ConfigParser import ConfigParser

import sys
import requests
import logging

# Setup a logger
log = logging.getLogger('static_assets')
log.addHandler(logging.StreamHandler())
log.setLevel(logging.DEBUG)

requests_log = logging.getLogger("requests")
requests_log.setLevel(logging.DEBUG)

config = ConfigParser()
config.read('config.ini')

VENDOR = path.join(path.dirname(__file__), 'resources/public/static/javascript/src/vendor')
DEPLOY = path.join(path.dirname(__file__), 'resources/public/static/javascript')

def concat_string(coll):
    string = ''.join(coll)
    return string

def resolve_external_deps(name, url):
    full_url = concat_string(['http://', url, '/', name])
    my_config = {'verbose': sys.stderr,
                 'timeout' : 0.01}
    file = requests.get(full_url, config=my_config)
    with open(path.join(VENDOR, name),'w') as output:
        output.writelines(file.content)

def resolve():
    section = 'remotejs'
    for lib in config.options(section):
        root = config.get(section, lib)
        log.info("%s/%s <<== http://%s/%s", VENDOR, lib, root, lib)
        resolve_external_deps(lib, root)

def create_and_register_bundles():
    environment = Environment('.')
    section = 'js'
    for option in config.options(section):
        deps = [v.strip() for v in config.get(section, option).split(',')]
        log.info("%s.js has dependencies: %s", option, deps)
        bndl = Bundle(*deps, filters='jsmin',
                      output=concat_string([DEPLOY, '/', option, '.js']))
        environment.register(option, bndl)
        bndlgz = Bundle(*deps, filters='jsmin, gzip',
                      output=concat_string([DEPLOY, '/', option, '.js.gz']))
        environment.register(concat_string([option, 'gz']), bndlgz)
        bndl.build()
        bndlgz.build()

if __name__ == '__main__':
    resolve()
    create_and_register_bundles()

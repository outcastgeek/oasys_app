__author__ = 'outcastgeek'

def includeme(config):
    config.scan(__name__)
    config.add_route('employees', '/employees')
    config.add_route('bootstrap_data', '/bootstrap_data')
    config.add_route('clean_bootstrap_data', '/clean_bootstrap_data')

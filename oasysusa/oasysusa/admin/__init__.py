__author__ = 'outcastgeek'

def includeme(config):
    config.scan(__name__)
    config.add_route('employees', '/employees')
    config.add_route('employees_es', '/employees_search')
    config.add_route('bootstrap_data', '/bootstrap_data')
    config.add_route('refresh_search_index', '/refresh_search_index')
    config.add_route('clean_bootstrap_data', '/clean_bootstrap_data')



#kprinceton
#Harry777

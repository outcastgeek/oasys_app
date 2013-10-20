__author__ = 'outcastgeek'

def includeme(config):
    config.scan(__name__)
    ###### API Paths #########
    config.add_route('employee', '/employee')
    config.add_route('project', '/project')
    config.add_route('week', '/week')



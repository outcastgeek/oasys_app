__author__ = 'outcastgeek'


def includeme(config):
    config.scan(__name__)
    config.add_route('project-form', '/timesheet/project')
    config.add_route('timesheet_report', '/timesheet/report')
    #### trying deform ######
    config.add_route('try_deform', '/deform')
    config.add_static_view('deform_static', 'deform:static')

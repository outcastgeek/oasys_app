__author__ = 'outcastgeek'

from pyramid.renderers import render
from pyramid.security import has_permission

from pyramid_jinja2 import IJinja2Environment

from pyramid_webassets import get_webassets_env

def hello(name):
    greeting = "Hello %s!!!!" % name
    response = render('templates/admin/hello.jinja2', dict(greeting=greeting))
    return response

def has_cred(permission, request):
    return has_permission(permission, request.context, request)

def _get_or_build_default_environment(registry):
    environment = registry.queryUtility(IJinja2Environment)
    if environment is not None:
        return environment

def includeme(config):
    jinja2_env = _get_or_build_default_environment(config.registry)
    # jinja2_env = get_webassets_env(config)
    assets_env = config.get_webassets_env()
    jinja2_env.assets_environment = assets_env



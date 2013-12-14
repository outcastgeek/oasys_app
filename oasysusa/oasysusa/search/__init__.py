__author__ = 'outcastgeek'

import logging
from pyramid.settings import asbool
from pyelasticsearch import ElasticSearch
from pyelasticsearch.exceptions import InvalidJsonResponseError
from requests.exceptions import ConnectionError

log = logging.getLogger('oasysusa')

def get_es_client(settings):
    search_settings = _get_search_settings(settings)
    es_client = ElasticSearch(
        'http://%(host)s:%(port)s/' % search_settings
    )
    return SafeEs(es_client)


# Got it from here: https://github.com/sontek/notaliens.com/blob/master/notaliens/notaliens/search/__init__.py
class SafeEs(object):
    def __init__(self, es):
        self.es = es

    def index(self, index, descriptor, body, **kwargs):
        try:
            self.es.index(index, descriptor, body, **kwargs)
        except (InvalidJsonResponseError, ConnectionError):
            log.exception("Couldn't index data to ElasticSearch")

    def bulk_index(self, index, descriptor, docs, **kwargs):
        try:
            self.es.bulk_index(index, descriptor, docs, **kwargs)
        except (InvalidJsonResponseError, ConnectionError):
            log.exception("Couldn't index data to ElasticSearch")

    def create_index(self, index, settings=None, mapping=None):
        try:
            self.es.create_index(index, settings=settings)
        except (InvalidJsonResponseError, ConnectionError):
            log.exception("Couldn't create the index in ElasticSearch")

    def delete_index(self, index):
        try:
            self.es.delete_index(index)
        except (InvalidJsonResponseError, ConnectionError):
            log.exception("Couldn't delete index from ElasticSearch")

    def update(self, index, doc_type, id=None, script=None, **kwargs):
        try:
            self.es.update(index, doc_type, id, script, **kwargs)
        except (InvalidJsonResponseError, ConnectionError):
            log.exception("Couldn't index data to ElasticSearch")

    def search(self, query, fallback=None, **kwargs):
        try:
            return self.es.search(query, **kwargs)
        except (InvalidJsonResponseError, ConnectionError):
            if fallback:
                log.exception("Couldn't search from ElasticSearch")
                return fallback(query, **kwargs)
            else:
                log.warn("No fallback registered")
                raise

    def put_mapping(self, index, descriptor, body):
        try:
            return self.es.put_mapping(index, descriptor, body)
        except (InvalidJsonResponseError, ConnectionError):
            log.exception("Couldn't set the mapping for ElasticSearch")


def _get_search_settings(settings, prefix='search.'):
    options = dict(
        (key[len(prefix):], settings[key])
        for key in settings if key.startswith(prefix)
    )

    config_mappings = [
        (asbool, ['enabled']),
        (int, ['port'])
    ]

    for converter, keys in config_mappings:
        for key in keys:
            if key in options:
                options[key] = converter(options[key])

    return options


def get_search_settings(request, prefix='search.'):
    """
    This will construct a dictionary of cache settings from an ini
    file and convert them to their proper type (bool, int, etc)
    """
    settings = request.registry.settings

    return _get_search_settings(settings, prefix)


# we are making this global to take advantage of connection pooling
# in pyelasticsearch
_es_client = None


def includeme(config):
    settings = config.registry.settings

    search_enabled = asbool(settings.get('search.enabled', False))
    log.info('elastic_search_enabled=%s' % search_enabled)

    # Enable searching?
    if not search_enabled:
        config.add_request_method(
            lambda request: {'enabled': False}, 'search_settings', reify=True
        )
        return

    search_settings = _get_search_settings(settings)

    global _es_client

    if _es_client is None:
        _es_client = ElasticSearch(
            'http://%(host)s:%(port)s/' % search_settings
        )

    config.add_request_method(
        get_search_settings,
        'search_settings',
        reify=True
    )

    config.add_request_method(
        lambda request: SafeEs(_es_client),
        'es',
        reify=True
    )






import unittest
from pyramid_debugtoolbar.panels import settings
import transaction

from pyramid import testing

from .models import DBSession

# from paste.deploy.loadwsgi import appconfig
# settings = appconfig('config:development.ini', relative_to='.')

class TestMyView(unittest.TestCase):
    def setUp(self):
        self.config = testing.setUp()
        from sqlalchemy import create_engine
        engine = create_engine('sqlite://')
        from .models import (
            Base,
            MyModel,
            )
        DBSession.configure(bind=engine)
        Base.metadata.create_all(engine)
        with transaction.manager:
            model = MyModel(name='one', value=55)
            DBSession.add(model)

    def tearDown(self):
        DBSession.remove()
        testing.tearDown()

    def test_it(self):
        from .views.public_views import my_view
        request = testing.DummyRequest()
        info = my_view(request)
        self.assertEqual(info['one'].name, 'one')
        self.assertEqual(info['project'], 'oasysusa')

    # def test_login(self):
    #     from .views.auth_views import login
    #     class FakeRequest(testing.DummyRequest):
    #         # def __init__(self, settings):
    #         #     self.settings = settings
    #         def route_url(self, name):
    #             return 'http://localhost/login'
    #         # registry = testing.setUp(settings={'login_providers':['github', 'google']})
    #     request = FakeRequest()
    #     # request = FakeRequest(settings)
    #     # with testing.testConfig() as config:
    #     #     config.
    #     info = login(request)
    #     # message = message,
    #     # url = request.application_url + '/login',
    #     # came_from = came_from,
    #     # login = login,
    #     # logged_in = login,
    #     # password = password,
    #     # providers_info = providers_info,
    #     self.assertIsNotNone(info['message'])
    #     self.assertIsNotNone(info['url'])
    #     self.assertIsNotNone(info['came_from'])
    #     self.assertIsNotNone(info['login'])
    #     self.assertIsNotNone(info['logged_in'])
    #     self.assertIsNotNone(info['password'])
    #     self.assertIsNotNone(info['providers_info'])
    #     # self.assertEqual(info['one'].name, 'one')
    #     # self.assertEqual(info['project'], 'oasysusa')

import unittest

from pyramid import testing

from base_test_case import BaseTestCase

class TestMyView(BaseTestCase):
    def setUp(self):
        super(TestMyView, self).setUp()
        from ..models import (
            MyModel,
            )
        model = MyModel(name='one', value=55)
        self.save(model)

    def test_it(self):
        from ..views.public_views import my_view
        request = testing.DummyRequest()
        info = my_view(request)
        self.assertEqual(info['one'].name, 'one')
        self.assertEqual(info['project'], 'oasysusa')

class TestAuthView(unittest.TestCase):

    def setUp(self):
        self.config = testing.setUp(settings={'login_providers':[]})

    def tearDown(self):
        testing.tearDown()

    def test_login(self):
        from ..views.auth_views import login
        class FakeRequest(testing.DummyRequest):
            def route_url(self, name):
                return 'http://localhost/login'
        request = FakeRequest()
        info = login(request)
        self.assertIsNotNone(info['message'])
        self.assertIsNotNone(info['url'])
        self.assertIsNotNone(info['came_from'])
        self.assertIsNotNone(info['login'])
        self.assertIsNotNone(info['logged_in'])
        self.assertIsNotNone(info['password'])
        self.assertIsNotNone(info['providers_info'])




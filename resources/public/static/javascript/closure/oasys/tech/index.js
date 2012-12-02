
goog.provide('oasys.tech.index');

oasys.tech.index.MainCtrl = function($scope, $location) {
    $scope.navigateTo = function(page) {
        $location.path("/" + page);
    };
};

oasys.tech.index.setup = function() {
  angular.module("oasys.tech.index", []).
      config(function($routeProvider) {
          $routeProvider.
              when('/industries', {templateUrl:'views/industries.html'}).
              when('/practice', {templateUrl:'views/practice.html'}).
              when('/staffing', {templateUrl:'views/staffing.html'}).
              when('/service', {templateUrl:'views/service.html'}).
              otherwise({redirectTo:'/service', templateUrl:'views/service.html'});
      });
};

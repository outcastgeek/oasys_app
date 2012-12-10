
function MainCtrl($scope, $location) {
    $scope.navigateTo = function(page) {
        $location.path("/" + page);
    };
};

angular.module("main", []).
      config(function($routeProvider) {
          $routeProvider.
              when('/industries', {templateUrl:'views/industries.html'}).
              when('/practice', {templateUrl:'views/practice.html'}).
              when('/staffing', {templateUrl:'views/staffing.html'}).
              when('/service', {templateUrl:'views/service.html'}).
              otherwise({redirectTo:'/service', templateUrl:'views/service.html'});
      });
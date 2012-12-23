
var mainModule = angular.module("main", []).
      config(function($routeProvider) {
          $routeProvider.
              when('/industries', {templateUrl:'views/industries.html'}).
              when('/practice', {templateUrl:'views/practice.html'}).
              when('/staffing', {templateUrl:'views/staffing.html'}).
              when('/service', {templateUrl:'views/service.html'}).
              otherwise({redirectTo:'/service', templateUrl:'views/service.html'});
      });

mainModule.factory('userService', function($rootScope) {
    var userService = {};

    userService.message = '';

    userService.refreshProfile = function(profile) {
        console.log('Refreshing profile with: ');
        console.log(profile);
        this.profile = profile;
        this.broadCastProfile();
    };

    userService.broadCastProfile = function() {
        console.log('Broadcasting profile...');
        $rootScope.$broadcast('profileRefreshed');
    };

    return userService;
});

function MainCtrl($scope, $location, userService) {

    $scope.profile = userService.profile;

    $.get('/username', function(profile) {
        userService.refreshProfile(profile);
    });

    $scope.$on('profileRefreshed', function(profile) {
        $scope.profile = userService.profile;
    });

    $scope.navigateTo = function(page) {
        $location.path("/" + page);
    };
}

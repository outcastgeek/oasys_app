/**
* OUTCASTGEEK 2012
**/

//Angular Controllers

function MainCtrl($scope, $location) {
    $scope.navigateTo = function(page) {
        $location.path("/" + page);
//        refreshSession();
    };

    $scope.customLog = function(msg) {
        outcastgeek.core.customLog(msg);
    };
}

// Session Handling

function refreshSession() {
    jQuery.ajax({
        url: "/",
//        xhrFields: {
//            withCredentials: true
//        },
//        headers: {
//            Cookie: document.cookie
//        },
        success: function(response){
          console.log("All Good!");
          console.log(response);
        },
        error: function(error) {
            console.log("No Bueno!");
            console.log(error);
        }
    });
}

// Bootstrap Client Side

YUI({
      groups: {
        'cdn': {
            async: false,
            modules: {
                'jquery': {
                    fullpath: 'http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js'
                },
                'jquery-ui': {
                    fullpath: 'http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js',
                    requires: ['jquery', 'jquery-ui-css']
                },
                'swfobject': {
                    fullpath: 'http://ajax.googleapis.com/ajax/libs/swfobject/2.2/swfobject.js'
                },
                'angular': {
                    fullpath: 'http://ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js'
                },
                'angular-resource': {
                    fullpath: 'http://ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular-resource.min.js'
                },
                'modernizr': {
                    fullpath: 'http://cdnjs.cloudflare.com/ajax/libs/modernizr/2.5.3/modernizr.min.js'
                }
            }
        },
        'outcastgeek': {
            base: '/static/',
            async: false,
            modules: {
                'underscore': {
                    path: 'javascript/outcastgeek/underscore.min.js'
                },
                'backbone': {
                    path: 'javascript/outcastgeek/backbone.min.js'
                },
                'bootstrap': {
                    //path: 'javascript/outcastgeek/bootstrap.min.js'
                    fullpath: 'http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.0.4/bootstrap.min.js'
                },
                'outcastgeek': {
                    path: 'javascript/outcastgeek/outcastgeek.js'
                }
            }
        }
    }
}).use('modernizr', 'angular', 'angular-resource', 'jquery', 'bootstrap', 'outcastgeek', function(Y) {

        angular.element(document).ready(function() {
            angular.bootstrap(document);
        });

        jQuery.noConflict();

        angular.module("main", []).
            config(function($routeProvider) {
                $routeProvider.
                    when('/industries', {templateUrl:'views/industries.html'}).
                    when('/practice', {templateUrl:'views/practice.html'}).
                    when('/staffing', {templateUrl:'views/staffing.html'}).
                    when('/service', {templateUrl:'views/service.html'}).
                    otherwise({redirectTo:'/service', templateUrl:'views/service.html'});
            });

//        setInterval("refreshSession()", 450000); // Every 7.5 mn
//        Detect idle and away: http://www.bedroomlan.org/coding/detecting-%E2%80%98idle%E2%80%99-and-%E2%80%98away%E2%80%99-timeouts-javascript
    });

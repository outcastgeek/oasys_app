/**
* OUTCASTGEEK 2012
**/

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
                    fullpath: 'http://code.angularjs.org/angular-1.0.1.min.js'
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
}).use('modernizr', 'angular', 'jquery', 'bootstrap', function(Y) {

        angular.element(document).ready(function() {
            angular.bootstrap(document);
        });

        jQuery.noConflict();
    });

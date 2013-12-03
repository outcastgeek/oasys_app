/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 12/2/13
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */

var dateStandAlone = angular.module('dateStandAlone', []);

dateStandAlone.config(function($interpolateProvider) {
    $interpolateProvider.startSymbol('{@~');
    $interpolateProvider.endSymbol('~@}');
}).controller('dateStandAloneCtrl', function($scope, $timeout) {
    $scope.today = function() {
        $scope.dt = new Date();
    };
    $scope.today();

    $scope.showWeeks = true;
    $scope.toggleWeeks = function () {
        $scope.showWeeks = ! $scope.showWeeks;
    };

    $scope.clear = function () {
        $scope.dt = null;
    };

    // Disable weekend selection
    $scope.disabled = function(date, mode) {
        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    };

    $scope.toggleMin = function() {
        $scope.minDate = ( $scope.minDate ) ? null : new Date();
    };
    $scope.toggleMin();

    $scope.open = function() {
        $timeout(function() {
            $scope.opened = true;
        });
    };

    $scope.dateOptions = {
        'year-format': "'yy'",
        'starting-day': 1
    };

//    $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'shortDate'];
//    $scope.format = $scope.formats[0];
    $scope.format = 'MM/dd/yyyy';
});





oasysUsaApp.directive('ngInitial', function() {
    return {
        restrict: 'A',
        controller: [
            '$scope', '$element', '$attrs', '$parse', function($scope, $element, $attrs, $parse) {
                var getter, setter, val;
                val = $attrs.value || $attrs.ngInitial;
                getter = $parse($attrs.ngModel);
                setter = getter.assign;
                setter($scope, val);
            }
        ]
    };
});

oasysUsaApp.controller('FormValidationController', function($scope) {

    $scope.master = {};

    $scope.update = function(data) {
        $scope.master = angular.copy(data);
    };

    $scope.reset = function() {
        $scope.data = angular.copy($scope.master);
    };

    $scope.isUnchanged = function(data) {
        return angular.equals(data, $scope.master);
    };

    $scope.reset();

    /*    Calendar Stuff     */

    $scope.today = function() {
        $scope.data.date_of_birth = new Date();
    };
    $scope.today();

    $scope.showWeeks = true;
    $scope.toggleWeeks = function () {
        $scope.showWeeks = ! $scope.showWeeks;
    };

    $scope.clear = function () {
        $scope.data.date_of_birth = null;
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


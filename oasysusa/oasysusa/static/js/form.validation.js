

oasysUsaApp.directive('ngInitial', function() {
    return {
        restrict: 'A',
        controller: [
            '$scope', '$element', '$attrs', '$parse', function($scope, $element, $attrs, $parse) {
                var getter, setter, val;
                val = $attrs.ngInitial || $attrs.value || $attrs.$$element.val();
//                console.log($attrs.name + " => " + val);
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
});


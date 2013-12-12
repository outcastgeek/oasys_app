
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


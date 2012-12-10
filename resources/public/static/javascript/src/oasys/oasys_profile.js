
function ProfileCtrl($scope, $http) {

    $scope.getProfile = function() {
        $http.get('/profileData').success(function(data) {
            $scope.profile = data;
        });
    };

    $scope.getProfile();

//    $scope.updateProfile = function(data) {
//
//        console.log("Updating profile with data" + data);
//        $http.post('/profile', $scope.profile).success(function(data) {
//            console.log(data);
//            $scope.getProfile();
//        }).error(function(data) {
//            console.log("Could not update profile: " + data);
//        });
//    };
};

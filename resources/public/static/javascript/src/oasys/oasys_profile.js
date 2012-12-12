
function ProfileCtrl($scope, $http) {

    $scope.getProfile = function() {
        $http.get('/profileData').success(function(data) {
            $scope.profile = data;
        });
    };

    $scope.getProfile();

    $scope.updateProfile = function(data) {

        $.ajax({
            type: "POST",
//            headers: {'Cookie':document.cookie},
            url: '/profile',
            data: $scope.profile,
            dataType: 'application/json',
            success: function(data){
                //console.log(data);
                console.log("Successfully updated profile.");
            },
//            xhrFields: {
//                withCredentials: true
//            },
            error: function(data){
                //console.log("Could not update profile: " + data);
                console.log("Could not update profile.");
            }});

//        //console.log("Updating profile with data" + data);
//        console.log("Updating profile...");
//        $http({method:'POST', url:'/profile',
//               data:$scope.profile, headers: {'Cookie':document.cookie},
//               withCredentials:true}).success(function(data) {
//            //console.log(data);
//            console.log("Successfully updated profile.");
//            $scope.getProfile();
//        }).error(function(data) {
//            //console.log("Could not update profile: " + data);
//            console.log("Could not update profile.");
//        });
    };
}

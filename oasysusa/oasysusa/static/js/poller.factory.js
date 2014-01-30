/**
 * Created by outcastgeek on 1/29/14.
 */
// http://stackoverflow.com/questions/19155069/writing-an-angularjs-poller
oasysUsaApp.factory('$poller', function($http,$q){
    return {
        poll : function(api){
            var deferred = $q.defer();
            $http.get(api).then(function (response) {
                deferred.resolve(response.data);
            });
            return deferred.promise;
        }

    }
});




/**
 * Created by outcastgeek on 1/29/14.
 */

oasysUsaApp.factory("$socketio", function($rootScope) {
    var socket = io.connect('/streaming', {
        reconnect: true,
        maxReconnectionAttempts: Infinity
    });
    return {
        on: function (eventName, callback) {
            socket.on(eventName, function () {
                var args = arguments;
                $rootScope.$apply(function () {
                    callback.apply(socket, args);
                });
            });
        },
        emit: function (eventName, data, callback) {
            socket.emit(eventName, data, function () {
                var args = arguments;
                $rootScope.$apply(function () {
                    if (callback) {
                        callback.apply(socket, args);
                    }
                });
            })
        }
    };
});







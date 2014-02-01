/**
 * Created by outcastgeek on 1/31/14.
 */

oasysUsaApp.controller('myCtrl', function ($scope) {
    $scope.myModel = {
        message: 'World'
    };
}).directive('myMessage', function () {
    return {
        link: function (scope, element) {
            var Hello = React.createClass({
                render: function() {
                    return React.DOM.div({}, 'Hello ' + this.props.name);
                }
            });
            scope.$watch('myModel.message', function (newVal, oldVal) {
                //if(oldVal !== newVal) return;

                React.renderComponent(Hello({
                    name: scope.myModel.message
                }), document.getElementById('example'));
            });
        }
    }
});




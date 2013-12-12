/**
 * Created with IntelliJ IDEA.
 * User: outcastgeek
 * Date: 12/11/13
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */

var oasysUsaApp = angular.module('oasysUsaApp', ['ui.bootstrap']);

oasysUsaApp.config(function($interpolateProvider) {
    $interpolateProvider.startSymbol('{@~');
    $interpolateProvider.endSymbol('~@}');
});





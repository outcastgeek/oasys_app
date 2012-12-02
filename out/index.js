goog.provide('outcastgeek.index');
goog.require('cljs.core');
goog.require('og.common');
outcastgeek.index.navigateTo = (function navigateTo(location){
return (function (page){
return location.path(cljs.core.format.call(null,"/%",page));
});
});
outcastgeek.index.CMainCtrl = (function CMainCtrl($scope,$location){
$scope.navigateTo = outcastgeek.index.navigateTo.call(null,$scope,$location);
});
outcastgeek.index.MainCtrl = outcastgeek.index.CMainCtrl;
(outcastgeek.index.MainCtrl["$inject"] = ["$scope","$location"]);
og.common.bootstrap.call(null);

goog.provide('phone_cat.app');
goog.require('cljs.core');
goog.require('phone_cat.controllers');
phone_cat.app.ng_route = (function ng_route(provider,path,route_spec){
return provider.when(path,cljs.core.clj__GT_js.call(null,route_spec));
});
phone_cat.app.ng_route_otherwise = (function ng_route_otherwise(provider,route_spec){
return provider.otherwise(cljs.core.clj__GT_js.call(null,route_spec));
});
phone_cat.app.start_symbol = (function start_symbol(provider,start){
return provider.startSymbol(start);
});
phone_cat.app.end_symbol = (function end_symbol(provider,end){
return provider.endSymbol(end);
});
var G__3451_3454 = angular.module.call(null,"phonecat",["phonecatFilters","phonecatServices"]);
G__3451_3454.config(["$routeProvider","$interpolateProvider",(function ($routeProvider,$interpolateProvider){
var G__3452_3455 = $interpolateProvider;
phone_cat.app.start_symbol.call(null,G__3452_3455,"{@");
phone_cat.app.end_symbol.call(null,G__3452_3455,"@}");
var G__3453 = $routeProvider;
phone_cat.app.ng_route.call(null,G__3453,"/phones",cljs.core.PersistentArrayMap.fromArray(["\uFDD0:templateUrl","/partials/phone-list","\uFDD0:controller",phone_cat.controllers.phone_list_ctrl], true));
phone_cat.app.ng_route.call(null,G__3453,"/phones/:phoneId",cljs.core.PersistentArrayMap.fromArray(["\uFDD0:templateUrl","/partials/phone-detail","\uFDD0:controller",phone_cat.controllers.phone_detail_ctrl], true));
phone_cat.app.ng_route_otherwise.call(null,G__3453,cljs.core.PersistentArrayMap.fromArray(["\uFDD0:redirectTo","/phones"], true));
return G__3453;
})]);

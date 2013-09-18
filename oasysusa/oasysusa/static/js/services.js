goog.provide('phone_cat.services');
goog.require('cljs.core');
var G__3458_3459 = angular.module.call(null,"phonecatServices",["ngResource"]);
G__3458_3459.factory("Phone",["$resource",(function ($resource){
return $resource.call(null,"app/phones/:phoneId",cljs.core.clj__GT_js.call(null,cljs.core.ObjMap.EMPTY),cljs.core.clj__GT_js.call(null,cljs.core.PersistentArrayMap.fromArray(["\uFDD0:query",cljs.core.PersistentArrayMap.fromArray(["\uFDD0:method","GET","\uFDD0:params",cljs.core.PersistentArrayMap.fromArray(["\uFDD0:phoneId",""], true),"\uFDD0:isArray",true], true)], true)));
})]);

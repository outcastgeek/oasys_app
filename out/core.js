goog.provide('oasys.core');
goog.require('cljs.core');
goog.require('oasys.tech.index');
goog.require('og.common');
og.common.bootstrap.call(null);
oasys.core.IndexMainCtrl = oasys.tech.index.MainCtrl;
(oasys.core.IndexMainCtrl["$inject"] = ["$scope","$location"]);
oasys.tech.index.setup.call(null);

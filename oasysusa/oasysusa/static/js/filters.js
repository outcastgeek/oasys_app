goog.provide('phone_cat.filters');
goog.require('cljs.core');
var G__3456_3457 = angular.module.call(null,"phonecatFilters",[]);
G__3456_3457.filter("checkmark",(function (){
return (function (input){
if(cljs.core.truth_(input))
{return "\u2713";
} else
{return "\u2718";
}
});
}));

//= require modernizr-2.5.3
//= require jquery
//= require jquery_ujs
//= require bootstrap
//= require jquery.data
//= require jquery.MetaData
//= require jquery.rating
//= require jquery.watermark
//= require guiders-1.2.0
//= require underscore
//= require backbone

/* ==================================
 * Coffeescript seems really cool!!!!
 * ================================== */
var cube, square;

square = function(x) {
    return x * x;
};

cube = function(x) {
    return square(x) * x;
};
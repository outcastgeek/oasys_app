//= require jquery
//= require jquery_ujs
//= require bootstrap

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
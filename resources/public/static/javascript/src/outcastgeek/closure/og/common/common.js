
goog.provide('og.common');

og.common.bootstrap = function() {
  // Bootstrap Client Side

  angular.element(document).ready(function() {
      angular.bootstrap(document);
  });

  jQuery.noConflict();

};

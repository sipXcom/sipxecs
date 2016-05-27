(function() {
  'use strict';
  var strophe = angular.module('strophe', []);

  /**
   * angular DI for Strophe.js
   * @return {Object} Strophe.js object
   */
  strophe.factory('strophe', function() {
    return window.Strophe;
  });

})();

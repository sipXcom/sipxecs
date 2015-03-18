(function() {
  'use strict';
  var underscore = angular.module('underscore', []);

  /**
   * angular DI for Underscore.js
   * @return {Object} Underscore.js object
   */
  underscore.factory('_', function() {
    return window._;
  });
})();

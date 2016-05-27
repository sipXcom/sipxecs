(function() {
  'use strict';
  var notify = angular.module('notify', []);

  /**
   * angular DI for Strophe.js
   * @return {Object} Strophe.js object
   */
  notify.factory('notify', function() {
    return window.Notify;
  });
})();

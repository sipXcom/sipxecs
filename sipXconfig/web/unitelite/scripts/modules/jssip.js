(function() {
  'use strict';
  var sip = angular.module('sip', []);

  /**
   * angular DI for Strophe.js
   * @return {Object} Strophe.js object
   */
  sip.factory('sip', function() {
    return window.JsSIP;
  });
})();

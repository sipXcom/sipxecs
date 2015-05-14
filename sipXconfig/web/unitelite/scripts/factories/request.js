(function(){
  'use strict';

  uw.factory('request', [
    '$http',
    function ($http) {
      /**
       * request generator with custom config
       * e.g.
       *     request({
       *       ...
       *       angular.js $http conf object
       *       ...
       *     })
       *
       * @param  {Object} conf    angular.js $http configuration object
       * @return {Object}         promise  response data || error
       */
      return function (conf) {
        return $http(conf).
          success(function(data) {
            return data;
          }).
          error(function(data, status, headers) {
            return new Error();
          });
      };

    }
  ]);
})();

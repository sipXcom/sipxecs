(function() {

  "use strict";

  angular.module("config", [])

  .constant("CONFIG", {
   "debug": true,
   "chatstateGoneTimeout": 600000,
   "keyPhotos": "uw:roster:photos",
   "prefix": "ouc",
   "authCookie": "oucunitewebauth",
   "version": "0.7.8",
   "baseRest": "/sipxconfig/rest"
  })

  ;

})();

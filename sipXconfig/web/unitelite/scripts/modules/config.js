(function() {

  "use strict";

  angular.module("config", [])

  .constant("CONFIG", {
   "debug": true,
   "chatstateGoneTimeout": 600000,
   "keyPhotos": "uw:roster:photos",
   "prefix": "ouc",
   "authCookie": "oucunitewebauth",
   "version": "1.1.0-beta",
   "baseRest": "/sipxconfig/rest"
  })

  ;

})();

(function(){
'use strict';

uw.directive('vcardPhoto', [
  'rosterService',
  'sharedFactory',
  '$parse',
  function (rosterService, sharedFactory, $parse) {
    return {
      compile: function (elem, attr) {
        var defaultImg  = sharedFactory.defaultImg;
        var main        = $parse(attr.vcardPhoto);
        return function link (scope, elem) {
          scope.$watchCollection(main, function (list) {
            var obj = rosterService.photos[scope.entry.jid];
            if (!obj)
              angular.element(elem).attr('src', defaultImg);
            else
              angular.element(elem).attr('src', 'data:' + obj.TYPE + ';base64,' + obj.BINVAL);
          })
        }
      }
    }
  }
])
})();

(function(){
'use strict';

uw.directive('maxHeight', [
  'uiService',
  function (uiService) {
    return {
      compile: function () {

        return function link (scope, elem) {
          scope.$watch(
            function() {
              return uiService.groupChat.modal;
            },
            function(newValue, oldValue) {
              if (newValue) {
                elem[0].scrollTop = 0;
                angular.element(elem).css({'height': '100%', 'overflow': 'hidden'})
              } else {
                angular.element(elem).css({'height': 'auto', 'overflow': 'auto'})
              }
            }
          );
        }

      }
    }
  }
]);
})();

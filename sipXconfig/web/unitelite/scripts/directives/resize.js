(function() {

  'use strict';

  uw.directive('resize', [
    '$window',
    function ($window) {
      return {
        scope: false,
        link: function (scope, element, attrs) {
          var heightDifference  = (attrs.resize === 'full') ? 42 : 75;
          var $                 = angular.element;
          var w                 = $(window);
          var height;

          scope.getWindowDimensions = function () {
            return { h: $window.innerHeight, w: $window.innerWidth };
          };

          scope.$watch(scope.getWindowDimensions, function (newValue, oldValue) {

            setTimeout(resize, 0);

            scope.$on('$viewContentLoaded', function(){
              setTimeout(resize, 0);
            });

            function resize() {
              height = newValue.h - heightDifference;
              $(element).css('max-height', height + 'px');
              return
            }
          }, true);

          w.bind('resize',function(){
            scope.$apply();
          });

          return true;
        }
      }
    }
  ]);

})();

(function(){
'use strict';

uw.directive('ngEnter', function() {
  return {
    restrict: 'A',
    link: function(scope, element, attrs, ctrl) {
      element.bind('keydown keypress', function(event) {
        if(event.which === 13) {
          scope.$watch(attrs.ngModel, function (v) {
            console.log('value changed, new value is: ' + v);
          });
          scope.$apply(function(){
            scope.$eval(attrs.ngEnter);
          });
          event.preventDefault();
        }
      });
    }
  }
});
})();

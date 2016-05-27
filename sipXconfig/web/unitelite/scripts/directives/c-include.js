(function(){
'use strict';

uw.directive('cInclude', function ($http, $templateCache, $compile, $parse, $sce) {
  return {
    compile: function (elem, attr) {
      var templatePath = attr.cInclude;
      var $ = angular.element;

      return function link (scope, elem) {
        scope.$watchCollection($sce.parseAsResourceUrl(templatePath), function (src) {
          if (src) {
            $http.get(src, {cache: $templateCache}).success(function(response) {
              var contents = $('<div/>').html(response).contents();
              elem.html(contents);
              $compile(contents)(scope);
            });
          }
        })
      }
    }
  }
})
})();

(function () {

  'use strict';

  uw.directive('minimizeGroup', [
    '$animate',
    function ($animate) {
      return {
        restrict: 'A',
        link: function (scope, el, attrs) {
          var $     = angular.element;
          var list  = $($(el).parent().parent().children()[1]);
          var icon  = $($(el).children()[0]);

          el.on('click', function (e) {
            if (list.hasClass('slideUp')) {
              $animate.removeClass(list, 'slideUp');
              icon.removeClass('icon-arrow_collapse_down');
              icon.addClass('icon-arrow_collapse_up');
            }
            else {
              $animate.addClass(list, 'slideUp');
              icon.addClass('icon-arrow_collapse_down');
              icon.removeClass('icon-arrow_collapse_up');
            }
          })
        }
      }
    }
  ])

})();

(function () {

  'use strict';

  /**
   * scroll document element into view
   * useful when body has overflow:hidden
   */
  uw.directive('clickView', function () {
    return {
      restrict: 'A',
      link: function (scope, elem, attrs) {
        elem.on('click', function () {
          if (scope.item) {
            if (scope.item.type === 'right') {
              document.querySelector('.right-side-view').scrollIntoView();
            }
            return;
          } else if (attrs.clickView === 'true') {
            document.querySelector('.right-side-view').scrollIntoView();
            return;
          } else {
            document.querySelector('.left-side-view').scrollIntoView();
            return;
          }
        })

      }
    }
  })

})();

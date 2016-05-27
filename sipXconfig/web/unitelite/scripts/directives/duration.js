(function () {

  'use strict';

  uw.directive('duration', function () {
    return {
      restrict: 'A',
      link: function (scope, elem, attrs) {
        // Minutes and seconds
        var time  = attrs.duration;
        var mins  = ~~(time / 60);
        var secs  = time % 60;
        var ret   = '';

        // Output like '1:01' or '4:03:59' or '123:03:59'
        ret += mins + 'min ' + (secs < 10 ? '0' : '');
        ret += '' + secs + 's';
        elem.text(ret);
      }
    }
  })

})();

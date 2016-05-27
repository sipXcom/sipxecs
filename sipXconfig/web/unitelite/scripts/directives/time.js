(function() {

  'use strict';

  uw.filter('time', [
    '$filter',
    function ($filter) {
      /**
       * returns formatted date based on UTC
       * e.g.
       *   if it's today, show 12:16 PM
       *   if it's yesterday, show Yesterday
       *   if it's anything else show dd/MM/yyyy
       *
       * @param  {String} input       UTC date
       * @return {String}             formatted date
       */
      return function (input) {
        var day       = new Date(input).getUTCDate().toString();
        var mth       = new Date(input).getUTCMonth().toString()
        var today     = new Date().getUTCDate().toString();
        var todayMth  = new Date().getUTCMonth().toString();

        if ((mth === todayMth) && (day === today)) {
          return $filter('date')(input, 'h:mm a');
        } else {
          return $filter('date')(input, 'short');
        }

      }
    }
  ])

})();

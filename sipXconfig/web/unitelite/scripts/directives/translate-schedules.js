(function() {

  'use strict';

  uw.filter('translateSchedules', function () {
    return function (input, array) {
      var translated = input;

      if (input === null) {
        return 'Always'
      }

      _.find(array, function (obj) {
        if (obj.scheduleId === input) {
          translated = obj.name;
          return true;
        }
      })

      return translated;
    }
  })

})();

(function() {

  'use strict';

  uw.filter('translateDays', function () {
    return function (input) {
      var translated = input;

      var dict = [
        { day: -2, human: 'Weekend' },
        { day: -1, human: 'Weekdays' },
        { day: 0, human: 'Every day' },
        { day: 1, human: 'Sunday' },
        { day: 2, human: 'Monday' },
        { day: 3, human: 'Tuesday' },
        { day: 4, human: 'Wednesday' },
        { day: 5, human: 'Thursday' },
        { day: 6, human: 'Friday' },
        { day: 7, human: 'Saturday' }
      ];

      _.find(dict, function (obj) {
        if (translated === obj.day) {
          translated = obj.human;
          return true
        }
      })

      return translated;
    }
  })

})();

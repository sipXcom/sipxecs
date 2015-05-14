(function() {

  'use strict';

  uw.filter('csearch', [
    function () {

      /*
        searches for either name or phone number
       */
      return function (searchArr, keyword) {
        return _.filter(searchArr, function (el) {
          return el.name.toLowerCase().indexOf(keyword) > -1 ||
            (el.number && el.number.toString().indexOf(keyword) > -1);
        })

      }
    }
  ])

})();

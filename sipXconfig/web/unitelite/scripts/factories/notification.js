(function () {

  'use strict';

  uw.factory('notification', [
    'notify',
    '$timeout',
    function (notify, $timeout) {
      return function (obj) {

        try {
          // IE
          if (window.createPopup) {
            var oPopup = window.createPopup();
            var oPopBody = oPopup.document.body;
            var w = window.innerWidth;
            var h = window.innerHeight;
            oPopBody.style.backgroundColor = 'white';
            oPopBody.style.border = 'solid black 1px';
            oPopBody.style.fontFamily = 'Arial';
            oPopBody.style.padding = '20px';
            oPopBody.innerHTML = '<strong>Unite - ' + obj.title + '</strong><br/>' + obj.body;
            oPopup.show(w - 200, h - 90, 180, 80, document.body);

            return true;
          }

          if (document.hasFocus()) {
            return true
          }

          var myNotification = new Notify(obj.title, {
              body: obj.body,
              notifyShow: onDisplay,
              notifyClick: onClk
          });

          // if ((myNotification.isSupported()) && (myNotification.needsPermission())) {
          //   myNotification.requestPermission();
          // }

          if (!document.hasFocus()) {
            myNotification.show();
          }

          if ($cookies.audio) {
            var audio = new Audio();
            if (audio.canPlayType('audio/mpeg;codecs="mp3"') !== '') {
              audio.src = 'styles/short_ping.mp3';
            } else if (audio.canPlayType('audio/ogg;codecs="vorbis"') !== '') {
              audio.src = 'styles/short_ping.ogg';
            }
            audio.play();
          }

          if (updateBadge && updateBadge !== false) {
            favicoService.badge(1);
          }

        } catch(err) {
          console.log(err);
        }

        function onDisplay(event) {
          $timeout(function() {
            if (event.currentTarget.cancel) {
              event.currentTarget.cancel()
            } else {
              event.currentTarget.close()
            };
          }, 3000);
        }

        function onClk(event) {
          if (event.currentTarget.cancel) {
            event.currentTarget.cancel()
          } else {
            event.currentTarget.close()
          };
          window.focus();
        }

        return true
      }
    }
  ]);

})();

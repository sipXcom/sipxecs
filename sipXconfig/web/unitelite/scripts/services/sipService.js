(function(){
'use strict';

uw.service('sipService', [
  '$rootScope',
  'notify',
  'sip',
  'CONFIG',
  function ($rootScope, notify, sip, CONFIG) {
    this.src = null;
    this.uri = null;
    this.pass = null;
    this.phone = null;
    var self = this;
    this.login = function () {
      var configuration = {
        'ws_servers': 'ws://bridge.ossapp.com:5062',
        'uri': 'sip:'+this.uri+'@'+CONFIG.domain,
        'password': this.pass
      };

      this.phone = new sip.UA(configuration);

      this.phone.on('registered', function(e){ /* Your code here */
        $rootScope.$broadcast('services.sipService.registered', {});
        console.log('all good');
        var myNotification = new notify('Unite Web', {
          body: 'SIP connected',
          icon: 'icon.png'
        });
        myNotification.show();
      });

      this.phone.on('newRTCSession', function(e){
        console.log('new call');
      });
      this.phone.on('unregistered', function(e){ /* Your code here */
        $rootScope.$broadcast('services.sipService.unregistered', {});
      });
      this.phone.on('registrationFailed', function(e){ /* Your code here */
        $rootScope.$broadcast('services.sipService.registrationFailed', {});
      });

      this.phone.start();

    }

    this.call = function () {
      // Register callbacks to desired call events
      var eventHandlers = {
        'progress': function(e){
          console.log('call is in progress');
        },
        'failed': function(e){
          console.log('call failed with cause: '+ e.data.cause);
        },
        'ended': function(e){
          console.log('call ended with cause: '+ e.data.cause);
        },
        'started': function(e){
          var rtcSession = e.sender;

          console.log('call started');

          // Attach local stream to selfView
          if (rtcSession.getLocalStreams().length > 0) {
            // selfView.src = window.URL.createObjectURL(rtcSession.getLocalStreams()[0]);
          }

          // Attach remote stream to remoteView
          if (rtcSession.getRemoteStreams().length > 0) {
            self.src = window.URL.createObjectURL(rtcSession.getRemoteStreams()[0]);
          }

          $rootScope.$broadcast('services.sipService.startedCall', {src: self.src});
        }
      };
      var options = {
        'eventHandlers': eventHandlers,
        'mediaConstraints': {'audio': true, 'video': false}
      };

      this.phone.call('sip:32054@openuc.ezuce.com', options);
    }

    this.stop = function () {
      this.phone.stop();
    }
  }
]);
})();

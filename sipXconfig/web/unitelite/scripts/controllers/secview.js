(function(){
'use strict';

uw.controller('secview', [
  '$scope',
  'uiService',
  function ($scope, uiService) {

    $scope.template           = uiService.secondary.template;
    $scope.chat               = uiService.secondary.chat;
    $scope.voicemail          = uiService.secondary.voicemail;
    $scope.conf               = uiService.secondary.conference;
    $scope.myprofile          = uiService.secondary.profile;

    // ngOptions
    // $scope.voicemail.folder   = $scope.voicemail.folders[0];

    $scope.$on('services.uiservice.changeview', function (e, obj) {
      if (obj.type) {
        // $scope.$destroy();
      }
    });

    $scope.$on('services.chat.receivedPresence', function () {
      $scope.$apply();
    });

    $scope.$on('services.ui.queryOccupants', function () {
      $scope.$apply();
    });

    $scope.$on('services.chat.receivedChatstate', function () {
      $scope.$apply();
    })

  }
]);
})();

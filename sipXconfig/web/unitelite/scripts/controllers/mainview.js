(function () {

  'use strict';

  uw.controller('mainview', [
    '$rootScope',
    '$scope',
    'uiService',
    'sharedFactory',
    function ($rootScope, $scope, uiService, sharedFactory) {

      // bindings
      //
      $scope.activityList         = uiService.activityList;
      $scope.phonebook            = uiService.rosterList;

      $scope.root                 = uiService.ui.root;
      $scope.selectConversation   = uiService.ui.activityList.selectConversation;
      $scope.removeConversation   = uiService.ui.activityList.removeConversation;

      $scope.search               = uiService.search;

      $scope.muc                  = {
        showModal:  uiService.ui.groupChat.showModal,
        hideModal:  uiService.ui.groupChat.hideModal,
        conf:       uiService.groupChat
      };

      $scope.entry                = {
        show:     false
      };

      $scope.showEntry = function (entry) {
        $scope.entry.profile = angular.copy(entry);
        $scope.muc.showModal();
      }

      $scope.hideEntry = function () {
        $scope.muc.hideModal();
        delete $scope.entry.profile;
      }

      $scope.callDialpad = function (number) {
        $rootScope.$broadcast('controller.mainview.callDialpad', { number: number });
      }

      $scope.$on('services.chat.receivedMessage', function (e, obj) {
        $scope.$apply();

        return
      });

      $scope.$on('services.chat.sentMessage', function (e, obj) {

        return
      });

      $scope.$on('services.chat.receivedPresence', function (e, obj) {
        $scope.$apply();

        return

      });

      $scope.$on('services.chat.receivedContactVCard', function () {
        $scope.$apply();

        return
      });

      $scope.$on('controller.secview.showDefault', function (e, obj) {
        uiService.util.openChat = null;

        return
      });

      $scope.$on('services.uiservice.changeview', function (e, obj) {
        if (!obj.type) {
          // $scope.$destroy();
        }
      });

    }
  ]);

})();

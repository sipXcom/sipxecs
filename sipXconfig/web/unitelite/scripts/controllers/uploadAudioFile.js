(function(){
'use strict';

uw.
  controller('UploadMoHFile', [
  '$rootScope',
  '$scope',
  'restService',
  'uiService',
  'CONFIG',
  'FileUploader',
  function ($rootScope, $scope, restService, uiService, CONFIG, FileUploader) {
    $scope.settings          = uiService.secondary.settings;
    var baseRestNew = CONFIG.baseRest.replace("rest","api");
    //var tokenHeader = 'Basic ' + btoa(restService.user + ':' + restService.pass);
    var uploader = $scope.uploader = new FileUploader({
        url: baseRestNew + '/my/moh/prompts'
        //headers: { "Authorization": tokenHeader }
       // withCredentials: true
    });

    // FILTERS

    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
          var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
          return '|wav|mp3|'.indexOf(type) !== -1;
            //return this.queue.length < 10;
        }
    });

    // CALLBACKS

    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function(fileItem) {
      console.info('onAfterAddingFile', fileItem);
      //do not upload the same file
      var size = $scope.settings.user.moh.selectMoh.length;
      var sameFile = false;
      for(var i = 0; i < size; i++){
        if ($scope.settings.user.moh.selectMoh[i].name === fileItem.file.name)
        {
          sameFile = true;
        }
      }
      if(sameFile === false){
        fileItem.upload();
      }
    };
    uploader.onAfterAddingAll = function(addedFileItems) {
        console.info('onAfterAddingAll', addedFileItems);
    };
    uploader.onBeforeUploadItem = function(item) {
        console.info('onBeforeUploadItem', item);
    };
    uploader.onProgressItem = function(fileItem, progress) {
        console.info('onProgressItem', fileItem, progress);
    };
    uploader.onProgressAll = function(progress) {
        console.info('onProgressAll', progress);
    };
    uploader.onSuccessItem = function(fileItem, response, status, headers) {
        console.info('onSuccessItem', fileItem, response, status, headers);
    };
    uploader.onErrorItem = function(fileItem, response, status, headers) {
        console.info('onErrorItem', fileItem, response, status, headers);
    };
    uploader.onCancelItem = function(fileItem, response, status, headers) {
        console.info('onCancelItem', fileItem, response, status, headers);
    };
    uploader.onCompleteItem = function(fileItem, response, status, headers) {
        console.info('onCompleteItem', fileItem, response, status, headers);
        $scope.settings.user.moh.addMoh(fileItem);
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
    };
  }]);
})();

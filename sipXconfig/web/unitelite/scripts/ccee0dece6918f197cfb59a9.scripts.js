(function() {

  "use strict";

  angular.module("config", [])

  .constant("CONFIG", {
   "debug": true,
   "chatstateGoneTimeout": 600000,
   "keyPhotos": "uw:roster:photos",
   "prefix": "ouc",
   "authCookie": "oucunitewebauth",
   "version": "0.7.8",
   "baseRest": "/sipxconfig/rest"
  })

  ;

})();

(function() {

  "use strict";

  angular.module("emoji", [])

  .constant("EMOJI", [
   {
    "name": "Happy",
    "equivalents": [
     ":-)",
     ":)"
    ],
    "src": "media/emoticons/Default/happy.png"
   },
   {
    "name": "Angry",
    "equivalents": [
     ">:o",
     ">:-o",
     "x("
    ],
    "src": "media/emoticons/Default/angry.png"
   },
   {
    "name": "Blushing",
    "equivalents": [
     ":-[",
     ":\">"
    ],
    "src": "media/emoticons/Default/blush.png"
   },
   {
    "name": "Confused",
    "equivalents": [
     "?:|",
     ":-/"
    ],
    "src": "media/emoticons/Default/confused.png"
   },
   {
    "name": "Cool",
    "equivalents": [
     "B-)"
    ],
    "src": "media/emoticons/Default/cool.png"
   },
   {
    "name": "Crying",
    "equivalents": [
     ":\"(",
     ":(("
    ],
    "src": "media/emoticons/Default/cry.png"
   },
   {
    "name": "Devil",
    "equivalents": [
     "]:)",
     ">:)"
    ],
    "src": "media/emoticons/Default/devil.png"
   },
   {
    "name": "Big Grin",
    "equivalents": [
     ":-D",
     ":D"
    ],
    "src": "media/emoticons/Default/grin.png"
   },
   {
    "name": "Laughing",
    "equivalents": [
     ":^0",
     ":))"
    ],
    "src": "media/emoticons/Default/laugh.png"
   },
   {
    "name": "Love Struck",
    "equivalents": [
     ":x"
    ],
    "src": "media/emoticons/Default/love.png"
   },
   {
    "name": "Mischief",
    "equivalents": [
     ";\\",
     ":>"
    ],
    "src": "media/emoticons/Default/mischief.png"
   },
   {
    "name": "Sad",
    "equivalents": [
     ":-(",
     ":("
    ],
    "src": "media/emoticons/Default/sad.png"
   },
   {
    "name": "Straight Face",
    "equivalents": [
     ":|",
     ":-|"
    ],
    "src": "media/emoticons/Default/plain.png"
   },
   {
    "name": "Shocked",
    "equivalents": [
     ":0",
     ":-O",
     ":O"
    ],
    "src": "media/emoticons/Default/shocked.png"
   },
   {
    "name": "Silly",
    "equivalents": [
     ":-p",
     ":p",
     ":-P",
     ":p"
    ],
    "src": "media/emoticons/Default/silly.png"
   },
   {
    "name": "Wink",
    "equivalents": [
     ";-)",
     ";)"
    ],
    "src": "media/emoticons/Default/wink.png"
   },
   {
    "name": "Alert",
    "equivalents": [
     "(!)"
    ],
    "src": "media/emoticons/Default/alert.png"
   },
   {
    "name": "Info",
    "equivalents": [
     "(i)"
    ],
    "src": "media/emoticons/Default/info.png"
   },
   {
    "name": "Minus",
    "equivalents": [
     "(-)"
    ],
    "src": "media/emoticons/Default/minus.png"
   },
   {
    "name": "Plus",
    "equivalents": [
     "(+)"
    ],
    "src": "media/emoticons/Default/plus.png"
   },
   {
    "name": "Heart",
    "equivalents": [
     "(heart)"
    ],
    "src": "media/emoticons/Default/heart.png"
   },
   {
    "name": "Sleepy",
    "equivalents": [
     "(:|"
    ],
    "src": "media/emoticons/Default/sleepy.gif"
   },
   {
    "name": "Let\"s Party",
    "equivalents": [
     "<:-P"
    ],
    "src": "media/emoticons/Default/party.gif"
   },
   {
    "name": "Rolling Eyes",
    "equivalents": [
     "8-|"
    ],
    "src": "media/emoticons/Default/eyeroll.gif"
   }
  ])

  ;
})();

(function() {
  'use strict';
  var notify = angular.module('notify', []);

  /**
   * angular DI for Strophe.js
   * @return {Object} Strophe.js object
   */
  notify.factory('notify', function() {
    return window.Notify;
  });
})();

(function() {
  'use strict';
  var underscore = angular.module('underscore', []);

  /**
   * angular DI for Underscore.js
   * @return {Object} Underscore.js object
   */
  underscore.factory('_', function() {
    return window._;
  });
})();

(function() {
    'use strict';

    var Base64 = (function () {
        var keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

        var obj = {
            /**
             * Encodes a string in base64
             * @param {String} input The string to encode in base64.
             */
            encode: function (input) {
                var output = "";
                var chr1, chr2, chr3;
                var enc1, enc2, enc3, enc4;
                var i = 0;

                do {
                    chr1 = input.charCodeAt(i++);
                    chr2 = input.charCodeAt(i++);
                    chr3 = input.charCodeAt(i++);

                    enc1 = chr1 >> 2;
                    enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                    enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                    enc4 = chr3 & 63;

                    if (isNaN(chr2)) {
                        enc3 = enc4 = 64;
                    } else if (isNaN(chr3)) {
                        enc4 = 64;
                    }

                    output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) +
                        keyStr.charAt(enc3) + keyStr.charAt(enc4);
                } while (i < input.length);

                return output;
            },

            /**
             * Decodes a base64 string.
             * @param {String} input The string to decode.
             */
            decode: function (input) {
                var output = "";
                var chr1, chr2, chr3;
                var enc1, enc2, enc3, enc4;
                var i = 0;

                // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
                input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

                do {
                    enc1 = keyStr.indexOf(input.charAt(i++));
                    enc2 = keyStr.indexOf(input.charAt(i++));
                    enc3 = keyStr.indexOf(input.charAt(i++));
                    enc4 = keyStr.indexOf(input.charAt(i++));

                    chr1 = (enc1 << 2) | (enc2 >> 4);
                    chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                    chr3 = ((enc3 & 3) << 6) | enc4;

                    output = output + String.fromCharCode(chr1);

                    if (enc3 != 64) {
                        output = output + String.fromCharCode(chr2);
                    }
                    if (enc4 != 64) {
                        output = output + String.fromCharCode(chr3);
                    }
                } while (i < input.length);

                return output;
            }
        };

        return obj;
    })();

    window.Base64 = Base64;
})();


(function(window, document, undefined) {
'use strict';

var uniteWeb = angular.module('uw', ['ngAnimate', 'ngSanitize', 'ngCookies', 'ngRoute', 'config', 'emoji', 'underscore', 'LocalStorageModule', 'notify', 'xml', 'dragAndDrop', 'ngOrderObjectBy', 'angularFileUpload', 'angularUtils.directives.dirPagination', 'ui.bootstrap']);

uniteWeb.config([
  '$routeProvider',
  '$httpProvider',
  '$compileProvider',
  '$provide',
  function ($routeProvider, $httpProvider, $compileProvider, $provide) {
  $routeProvider.
    when('/voicemail', {
      templateUrl: 'views/main.html',
      controller: [
        '$scope',
        '$location',
        'restService',
        'uiService',
        function ($scope, $location, restService, uiService) {
          $scope.received = true;

          restService.getLogindetails().
            then(function (data) {
              restService.updateCredentials(data['login-details']['userName']);
              restService.connected = true;
            }, function (err) {
              console.log(err);
            }).
            then(function () {
              return restService.getPhonebook();
            }).
            then(function (data) {
              restService.phonebook = data.phonebook;
              restService.phonebook.forEach(function (el, i) {
                restService.phonebook[i].name = (restService.phonebook[i]['contact-information'].imDisplayName) || (restService.phonebook[i]['first-name'] + ' ' + restService.phonebook[i]['last-name']);
              })
              uiService.util.populateContactList();
              uiService.util.changeView(uiService.ui.root.templates[3]);
            }).
            catch(function (err) {
              console.log(err);
            })
        }
      ]
    }).
    when('/', {
      templateUrl: 'views/main.html',
        controller: [
          '$scope',
          '$location',
          'restService',
          'uiService',
          function ($scope, $location, restService, uiService) {
            $scope.received = true;

            restService.getLogindetails().
              then(function (data) {
                restService.updateCredentials(data['login-details']['userName']);
                restService.connected = true;
              }, function (err) {
                console.log(err);
              }).
              then(function () {
                return restService.getPhonebook();
              }).
              then(function (data) {
                restService.phonebook = data.phonebook;
                restService.phonebook.forEach(function (el, i) {
                  restService.phonebook[i].name = (restService.phonebook[i]['contact-information'].imDisplayName) || (restService.phonebook[i]['first-name'] + ' ' + restService.phonebook[i]['last-name']);
                })
                uiService.util.populateContactList()
              }).
              catch(function (err) {
                console.log(err);
              })
          }
        ]
    }).
    otherwise({redirectTo:'/'});

    $httpProvider.interceptors.push(function($q) {
      return {
        'request': function(config) {
          return config || $q.when(config);
        },
        'response': function (response) {
          return response || $q.when(response);
        }
      };
    });

    $compileProvider.aHrefSanitizationWhitelist(/^\s*(http|https|blob|mailto):/);
    $compileProvider.imgSrcSanitizationWhitelist(/^\s*(https?|ftp|file|blob):|data:image\//);

    // https://github.com/angular/angular.js/issues/1404
    $provide.decorator('ngModelDirective', function($delegate) {
      var ngModel = $delegate[0], controller = ngModel.controller;
      ngModel.controller = ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
        var $interpolate = $injector.get('$interpolate');
        attrs.$set('name', $interpolate(attrs.name || '')(scope));
        $injector.invoke(controller, this, {
          '$scope': scope,
          '$element': element,
          '$attrs': attrs
        });
      }];
      return $delegate;
    });
    // https://github.com/angular/angular.js/issues/1404
    $provide.decorator('formDirective', function($delegate) {
      var form = $delegate[0], controller = form.controller;
      form.controller = ['$scope', '$element', '$attrs', '$injector', function(scope, element, attrs, $injector) {
        var $interpolate = $injector.get('$interpolate');
        attrs.$set('name', $interpolate(attrs.name || attrs.ngForm || '')(scope));
        $injector.invoke(controller, this, {
          '$scope': scope,
          '$element': element,
          '$attrs': attrs
        });
      }];
      return $delegate;
    });
  }
]);

uniteWeb.run([
  '$cookies',
  '$templateCache',
  function ($cookies, $templateCache) {

    angular.element(window).on('beforeunload', function (e) {
      //show warning in case close/reload unite lite
      e = e || window.event;

      // For IE and Firefox prior to version 4
      if (e) {
        e.returnValue = 'Are you sure that you want to leave uniteme?';
      }

      // For Safari
      return 'Are you sure that you want to leave uniteme?';
    });
  }
]);

window.uw = uniteWeb;

// expose uw as an AMD module
if (typeof define === 'function' && define.amd) {
    define(uw);
}

})(window, document);

(function(){
'use strict';

/**
 * creates user profile based on vCard
 * @param  {Object} strophe  angularjs DI of Strophe.js
 * @return {Object}          profile object
 */
uw.factory('profileFactory', [
  'strophe',
  function (strophe) {
    var profile = {};

    /**
     * initializez profile
     * @param  {String} jid       XMPP JID received from roster
     * @return {Object}           {jid: XMPP JID}
     */
    profile.init = function (jid) {
      return {
        jid: jid
      }
    }

    /**
     * returns .vCard Object
     * @param {NamedNodeMap} vCard      received from XMPP server
     * @param {Boolean} empty used for setting profiles
     */

    profile.set = function (vCard, special) {
      var $       = angular.element;
      var self    = this;
      var parsed  = {};
      var jqEl;
      var text;
      var temp = special;

      if (special) {
        _.each($(vCard).children(), function (el, idx) {
          jqEl = $(el);
          if (special === 'TEL') {
            if (idx < 2) {
              temp += '-'+el.nodeName;
            } else {
              temp += '-'+el.nodeName;
              text = jqEl.text();
              if (text.length === 0) {
                return;
              }

              parsed[temp] = text;
            }
          }

          if (special === 'ADR') {
            if (idx === 0) {
              temp += '-'+el.nodeName;
            } else {
              text = jqEl.text();
              if (text.length === 0) {
                return;
              }
              parsed[temp + '-' + el.nodeName] = text;
            }
          }
        });
      } else {
        _.each($(vCard).children(), function (el, idx) {
          jqEl = $(el);
          if (jqEl.children().length > 0) {
            if (el.nodeName === 'TEL') {
              parsed[el.nodeName+'-'+$(jqEl[0].children[0])[0].nodeName + '-' + $(jqEl[0].children[1])[0].nodeName] = self.set(el, el.nodeName);
            } else if (el.nodeName === 'ADR') {
              parsed[el.nodeName+'-'+$(jqEl[0].children[0])[0].nodeName] = self.set(el, el.nodeName);
            } else {
              parsed[el.nodeName] = self.set(el);
            }
          } else {

            text = jqEl.text();
            if (text.length === 0) {
              return;
            }

            parsed[el.nodeName] = text;

          }
        });
      }

      // console.log(parsed);
      return parsed;
    }

    profile.util = {
      getFullName : function () {

      },
      getPhotoSrc : function () {

      },
      translate : function (text) {
        return makeHumanReadable(text);
        // var parsed = {};

        // _.each(obj.vCard, function (val, key) {
        //   k = makeHumanReadable(key);
        //   console.log(val);
        //   console.log(key);
        //   // if (typeof val === 'object')
        //   //   parsed[k]
        // })
      }
    }

    function makeHumanReadable(text) {
      var translated = text;
      var dict = [
        {vcard: 'ADR', human: 'Address'},
        {vcard: 'CTRY', human: 'Country'},
        {vcard: 'EXTADD', human: 'Extended-Address'},
        {vcard: 'HOME', human: 'Home'},
        {vcard: 'LOCALITY', human: 'Locality'},
        {vcard: 'PCODE', human: 'Postal-Code'},
        {vcard: 'REGION', human: 'Region'},
        {vcard: 'STREET', human: 'Street'},
        {vcard: 'BDAY', human: 'Birth-Day'},
        {vcard: 'DESC', human: 'Description'},
        {vcard: 'EMAIL', human: 'Email'},
        {vcard: 'INTERNET', human: 'Internet'},
        {vcard: 'PREF', human: 'Pref'},
        {vcard: 'USERID', human: 'User'},
        {vcard: 'FN', human: 'Full-Name'},
        {vcard: 'JABBERID', human: 'JabberID'},
        {vcard: 'N', human: 'Names'},
        {vcard: 'FAMILY', human: 'Family'},
        {vcard: 'GIVEN', human: 'Given'},
        {vcard: 'MIDDLE', human: 'Middle'},
        {vcard: 'NICKNAME', human: 'Nickname'},
        {vcard: 'ORG', human: 'Organization'},
        {vcard: 'ORGUNIT', human: 'Organizational Unit'},
        {vcard: 'PHOTO', human: 'Photo'},
        {vcard: 'BINVAL', human: 'Binary-Value'},
        {vcard: 'TYPE', human: 'Type'},
        {vcard: 'ROLE', human: 'Role'},
        {vcard: 'TEL', human: 'Telephone'},
        {vcard: 'HOME', human: 'Home-Phone'},
        {vcard: 'MSG', human: 'Messaging'},
        {vcard: 'NUMBER', human:'Number'},
        {vcard: 'TITLE', human: 'Title'},
        {vcard: 'URL', human: 'Url'},
        {vcard: 'X-ALT-EMAIL', human: 'Alternative-Email'},
        {vcard: 'X-ALT-JABBERID', human: 'Alternative-JabberID'},
        {vcard: 'X-ASSISTANT', human: 'Assistant'},
        {vcard: 'X-ASSISTANT-PHONE', human: 'Assistant-Phone'},
        {vcard: 'X-DID', human: 'DID'},
        {vcard: 'X-FACEBOOK', human: 'Facebook-URL'},
        {vcard: 'X-INTERN', human: 'Internal-Number'},
        {vcard: 'X-LINKEDIN', human: 'LinkedIn-URL'},
        {vcard: 'X-LOCATION', human: 'Location'},
        {vcard: 'X-MANAGER', human: 'Manager'},
        {vcard: 'X-SALUTATION', human: 'Salutation'},
        {vcard: 'X-TWITTER', human: 'Twitter-URL'},
        {vcard: 'X-XING', human: 'Xing-URL'}
      ];

      _.find(dict, function (obj) {
        if ((translated === obj.vcard) || (translated === obj.human)) {
          translated = (translated === obj.vcard) ? obj.human : obj.vcard;
          return
        }
      })

      return translated;
    }

  return profile;


    // function Profile(jid) {
    //   var $             = angular.element;
    //   var self          = this;
    //   this.jid          = jid;
    //   this.fn           = '';
    //   this.nGiven       = '';
    //   this.nMiddle      = '';
    //   this.nFamily      = '';
    //   this.photoType    = '';
    //   this.photoBinval  = '';
    //   this.photo = '';

    //   /**
    //    * returns the full name of the profile
    //    * @return {String}       the full name
    //    */
    //   this.fullName = function () {
    //     if (this.fn) return this.fn;

    //     var arr = [];
    //     if (this.nGiven) arr.push(this.nGiven);
    //     if (this.nMiddle) arr.push(this.nMiddle);
    //     if (this.nFamily) arr.push(this.nFamily);

    //     if (arr.length > 0) return arr.join(' ');

    //     return strophe.getNodeFromJid(this.jid);
    //   };

    //   /**
    //    * returns the base64-encoded photo
    //    * @return {String}       base64 photo
    //    */
    //   this.photoSrc = function () {
    //     if (!this.photoType || !this.photoBinval) return 'styles/images/white_no_pic_provided.png';
    //     return 'data:' + this.photoType + ';base64,' + this.photoBinval;
    //   };

    //   /**
    //    * sets the internal vCard Object
    //    * @param {NamedNodeMap} vCard      received NamedNodeMap
    //    */
    //   this.set = function (vCard) {
    //     var internalProp  = '';
    //     var childName     = '';
    //     var parentName    = '';
    //     var trimmed       = '';
    //     var vchildren     = $(vCard).children().children();
    //     var tchildren

    //     // cycles every vCard children, then searching (if avail.) one level deep
    //     _.each(vchildren, function (tag) {
    //       parentName  = $(tag)[0].tagName.toString().toLowerCase();
    //       tchildren   = $(tag).children();

    //       if (tchildren.length > 0)
    //         _.each(tchildren, function (child) {
    //           childName   = $(child)[0].tagName.toString().toLowerCase();
    //           trimmed     = $(child).text().trim();

    //           if (parentName === 'photo') {

    //             if (childName === 'type')
    //               self.photo = 'data:' + trimmed + ';base64,' + self.photo;
    //             else if (childName === 'binval')
    //               self.photo = self.photo + trimmed;

    //           } else {
    //             childName           = childName[0].toUpperCase() + childName.slice(1);
    //             internalProp        = parentName + childName;
    //             self[internalProp]  = trimmed;
    //           }
    //         })
    //       else {
    //         internalProp = parentName.replace(new RegExp('-','gm'),'');
    //         self[internalProp] = $(tag).text().trim();
    //       };
    //     });
    //   };
    // }

    // return Profile;
  }
]);
})();

(function(){
  'use strict';

  uw.factory('request', [
    '$http',
    function ($http) {
      /**
       * request generator with custom config
       * e.g.
       *     request({
       *       ...
       *       angular.js $http conf object
       *       ...
       *     })
       *
       * @param  {Object} conf    angular.js $http configuration object
       * @return {Object}         promise  response data || error
       */
      return function (conf) {
        return $http(conf).
          success(function(data) {
            return data;
          }).
          error(function(data, status, headers) {
            return new Error();
          });
      };

    }
  ]);
})();

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

(function() {
'use strict';

uw.service('sharedFactory', [
  'localStorageService',
  '_',
  'CONFIG',
  function (localStorage, _, CONFIG) {
    var keyConversationList   = CONFIG.keyConversationList;
    var keyArchiveObj         = CONFIG.keyArchiveObj;
    var jid                   = '';

    this.archive = {};

    var archive = this.archive;

    this.init = function (ownJid) {
      jid = ownJid;
    }

    this.archiveMessage = function (o) {
      var obj = angular.copy(o);
      // console.log(obj);
      var done = false;
      var addr = (obj.from && obj.to) ? (obj.to) : (obj.from);
      // let's check for trailing/leading whitespace
      addr = addr.replace(/^\s+|\s+$/g,'');
      if (!archive[addr]) archive[addr] = [];
      if (archive[addr].length === 0) archive[addr].push(obj);
      else {
        for (var i = 0; i < archive[addr].length; i++) {
          if (archive[addr][i].timestamp.toString() === obj.timestamp.toString()) {
            archive[addr][i] = obj;
            done = true;
          }
        }
        if (!done) {
          archive[addr].push(obj);
        }
      };


      this.saveArchiveObj();

    };

    this.flushContactMessageList = function (jid) {
      archive[jid] = [];
      console.log('flush');
      this.saveArchiveObj(archive);
    };

    this.savePhotos = function (photos) {
      this.save(CONFIG.keyPhotos, photos);
    }

    this.getPhotos = function () {
      var json = this.get(CONFIG.keyPhotos);
      return _.isEmpty(json) ? [] : json;
    }

    this.saveRoster = function (roster) {
      this.save(CONFIG.keyRoster, roster);
    }

    this.getRoster = function () {
      var json = this.get(CONFIG.keyRoster);
      return _.isEmpty(json) ? [] : json;
    }

    this.getGroupChats = function () {
      var json = this.get(CONFIG.keyGroupChats);
      return _.isEmpty(JSON.parse(json)) ? [] : json;
    }

    this.saveGroupChats = function (groups) {
      this.save(CONFIG.keyGroupChats, groups);
    }

    this.getGroups = function () {
      var json = this.get(CONFIG.keyRosterGroups);
      return _.isEmpty(json) ? [] : json;
    }

    this.saveGroups = function (groups) {
      this.save(CONFIG.keyRosterGroups, groups);
    }

    this.saveGroupsContact = function (obj) {
      var roster = this.getRoster();
      var groups = this.getGroups();
      _.extend(roster[obj.jid], obj);

      for (var i = 0; i < groups.length; i++) {
        for (var j = 0; j < groups[i].length; j++) {
          if (groups[i][j].jid = obj.jid) {
            _.extend(groups[i][j], obj);
          }
        }
      }

      this.saveGroups(groups);
      this.saveRoster(roster);
    };

    this.getContactMessageList = function (contactJid) {
      // console.log(contactJid);
      // console.log(archive[contactJid]);
      // console.log(this.archive[contactJid]);
      if (!archive[contactJid]) archive[contactJid] = [];
      return archive[contactJid];
      // var bareJid = strophe.getBareJidFromJid(contactJid);
      // var list = _.find(archive, function (messageList, key) {
      //   return key === bareJid;
      // });
      // return list ? list : [];
    };

    this.delContactMessageList = function (contactJid) {
      if (!archive[contactJid]) return;
      delete archive[contactJid];
      this.saveArchiveObj();
    }

    this.getConversationList = function () {
      var json = this.get(CONFIG.keyConversationList);
      return _.isEmpty(json) ? [] : json; // return a list if empty
    };

    this.saveConversationList = function (list) {
      this.save(CONFIG.keyConversationList, list);
    };

    this.saveConversationListContact = function (obj) {
      // console.log(groups);
    }

    this.saveArchiveObj = function () {
      this.save(CONFIG.keyArchiveObj, archive);
    };

    this.getArchiveObj = function () {
      // this.archive = ;
      // this.archive = archive;
      this.archive = this.get(CONFIG.keyArchiveObj);
      if (typeof this.archive !== 'string')
        _.extend(archive, this.archive);
      // archive = this.archive || {};
    };

    this.save = function (key, json) {
      var suff = this.getUserSuffix();
      if (!suff) return;
      var str = angular.toJson(json);
      localStorage.add(key + suff, str);
    };

    this.get = function (key) {
      var str = localStorage.get(key + this.getUserSuffix()) || '{}';
      return str;
    };

    this.getUserSuffix = function () {
      var user = jid.split('#')[0];
      if (user === '') return false;

      return '.' + user;
    };

    // original code: https://github.com/eligrey/canvas-toBlob.js/blob/master/canvas-toBlob.js#L25
    // license: https://raw.github.com/eligrey/canvas-toBlob.js/master/LICENSE.md
    this.decodeBase64 = function(base64, cb) {
      var base64Ranks = new Uint8Array([
          62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1
        , -1, -1,  0, -1, -1, -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9
        , 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25
        , -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35
        , 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
      ]);
      var len     = base64.length;
      var buffer  = new Uint8Array(len / 4 * 3 | 0);
      var i       = 0;
      var outptr  = 0;
      var last    = [0, 0];
      var state   = 0;
      var save    = 0;
      var rank
      var code
      var undef
      while (len--) {
        code = base64.charCodeAt(i++);
        rank = base64Ranks[code-43];
        if (rank !== 255 && rank !== undef) {
          last[1] = last[0];
          last[0] = code;
          save = (save << 6) | rank;
          state++;
          if (state === 4) {
            buffer[outptr++] = save >>> 16;
            if (last[1] !== 61 /* padding character */) {
              buffer[outptr++] = save >>> 8;
            }
            if (last[0] !== 61 /* padding character */) {
              buffer[outptr++] = save;
            }
            state = 0;
          }
        }
      }
      // 2/3 chance there's going to be some null bytes at the end, but that
      // doesn't really matter with most image formats.
      // If it somehow matters for you, truncate the buffer up outptr.
      cb(buffer);
      return buffer;
    }

    this.settings = {
      mainMenu: [
        {
          name: 'Activity List',
          url: 'views/activitylist.html',
          iconClass:  'icon-activity_stream',
          show: 'false'
        },
        {
          name: 'Contacts',
          url: 'views/rosterlist.html',
          iconClass:  'icon-presence_meeting',
          show: 'true'
        },
        {
          name: 'Conference Bridge',
          url: 'views/conference-bridge.html',
          iconClass: 'icon-conference_call',
          type: 'right',
          show: 'true',
          fn: 'conference'
        },
        {
          name: 'Voicemails',
          url: 'views/voicemails.html',
          iconClass: 'icon-voicemail',
          type: 'right',
          show: 'true',
          fn: 'voicemail'
        },
        {
          name: 'My profile',
          url: 'views/my-profile.html',
          iconClass: 'icon-my-profile',
          type: 'right',
          show: 'true',
          fn: 'profile'
        },
        {
          name: 'Call history',
          url: 'views/call-history.html',
          iconClass: 'icon-call_history',
          type: 'right',
          show: 'true'
        },
        {
          name: 'Settings',
          url: 'views/settings.html',
          iconClass: 'icon-settings_cogs',
          type: 'right',
          show: 'true'
        },
        {
          name: 'Logout',
          url: '/',
          iconClass: 'icon-logout',
          show: 'true',
          type: 'right',
          fn: 'logout'
        },
        {
          name: 'Search',
          url: 'views/search.html',
          iconClass: 'icon-settings_cogs',
          show: 'false'
        },
        {
          name: 'Chat',
          url: 'views/chat.html',
          iconClass: 'icon-settings',
          type: 'right',
          show: 'false'
        },
        {
          name: 'default',
          url: 'views/default-right.html',
          iconClass: 'icon-settings_cogs',
          type: 'right',
          show: 'false'
        }
      ],
      miniTemplates: [
        {
          name: 'Profile',
          url: 'views/profile.html',
        }
      ],
      defaultPresences: [
        {
          icon: 'icon-status_available',
          show: 'Available',
          status: '',
          color: '#38b427'
        },
        {
          icon: 'icon-clock_fill_away',
          show: 'Away',
          status: 'away',
          color: '#f18703'
        },
        {
          icon: 'icon-presence_dnd',
          show: 'Do not disturb',
          status: 'dnd',
          color: '#e3352d'
        },
        {
          icon: 'icon-clock_fill_xa',
          show: 'Extended away',
          status: 'xa',
          color: '#0d73b5'
        },
        {
          icon: 'icon-presence_invisible',
          show: 'Invisible',
          status: 'invisible',
          color: '#bdbfc0'
        }
      ],
      defaultLocations: [
        {
          icon: '',
          text: 'Home'
        },
        {
          icon: '',
          text: 'At work'
        },
        {
          icon: '',
          text: 'On the road'
        }
      ],
    };

    this.defaultImg = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAIAAABMXPacAAAQOUlEQVR42uVdaVsbuw7m//+xW9q0lKVNgIQtoSEsIWEv630Pas0wtmVJ9kw4z/GHPnSi8XgsW8srWbPUeW1fvnzppJqEpoUWG8aX16Yiq92ifVnhhDBkGxsbS609z8w/1cxWJzeTJjngfAasr68vaadGMrIgmeSK5B2YWwxki93xP3/+XOr8t5tqEdhWHsPLMAMyxVE766sdGV2TSEmRFSOL0TSyAz7OBi+1riUzqyJzV378+LEkn4jkWFXqK0cnf7R1wMsD5td/GNCmpWhWm4tlAGi+fv2q7VZC2RQDiisMXphS+/z5c00aZPoBtcUR3NwxGmZhVQeZYECwF6FVLlwLcvlbnV/+rarEBXnv35sUyMkHFVDCTYt7uvfbt28rKyvwG/v9/sHBweFrG1caXcFPIAAZiCE0hAqpHWHbnh+g9YaCNFjCy8vL+Pf79++bm5v7+/uY5dPT08vLy9+/fz+8tse/7enpia7gJxCADMS4ZWtrC7e7rsxaKp+FNfnh/vvGgCaWiXCH1l7182sjN317e3s0Gs1ms7u7O8zyi6w9Pz+DGMzAjbgdnaArYmpzu8HW85sOSBpSMR8k0+QIKrHV1VVIkul0innHusaEvpgabsTt6ARdoUN0m2kdSaCkoKqP0fzxA2J6ldH4QtBNpQNxBbK+1+tNJpPr62u35B0DhJzw6dEVOkS36Jx0g2QdSPyezM3xZwf4jG0Bna69M+QDNOfe3t58Pr+/v+dnVssAaugWne/u7kIxJA0kM+yjonmnAxaCDbjnwnQZDofVhd9Eo62AB+Fxi3rlhBVkG1OmDsBOhHyILfziDQ/C4/DQhSNR7xiQ9LCEKldr4WEQmA4YLUFRw4id59eWFEdBiYTH4aF4NEk/uZEmVNRCsjclLDSEghrbN3KTLnt19o+Pj2GruMmiaXp+32KTHqQRkuGheDTxgIfyfRRTMumSqIAdC8p0MuleKMOjoyMIBLOhmS+LMAAM4wPpAPnkypGQ4FqAzQODBEb6y0IbBoBhYDBOELWpmf9hgDAwLQ+zJRmAV4W9D8/o8vKSsSNj4iUo5WM0jPhyDcPAYGhgEnniR1d4acyQiTzh4jAc3nN9ff3k5IREv0SdPntNQiakIWWAIdWwCkbb5WNHbyHJpM50sYhSjhg6hMMFO2RRot9nCQaDIQWdZC0ErfJhdSHJoDGqjQ1glXW73fPzc4kFqfJ7c8jwK4aEgUkAu/x9kMWA/FDGaDR6fHzMnNDiDYIIA5MHiJIWp0R8pcE4xq43ZMBhj6+trU2nU9UsB4l9wz9zc+BfDAzDkwdBkwRJmjAYZwb0k2sHDIC9cX19/fIhGwY2GAwwyKpvHFt8vASOWUG1qX5nBWkdK2ZPxIwf53nZVmtZ6eT3hoGNx2MKohkEvQGZ4CJipfJ2qqtgY2NjNpsZ5lHInkwyXMfwyB6VOKcxfSAkS0AR/nLOz8nd3Ny8ubkpslolbpc2YICG4fV6PUk4TD7dzLRkYUEqy3d5eZnMf8IenN6L4WX+HzGPjAHjgtAe70WTQwBHPQiRFg8ac1kR8ixwIQNWV1cnk4nzfmMMqAkKZspiNEG2BYWPz4Cnpyd4xRiqigFmcR1lAIMF2WIvYABkK5wdCngl3SI5ziMBHuRCCcM7OzuDMcoYQsJwMZ+oy8HRtpgw70yAAdDAEgdYaOM3pKjJJcZa+fTpUzKuIom9SJNzi/jDMXWEhvcBAy4uLszzkgTabICdTzmfzzFUYgAvbCV2J78J3sC4IlqF6Qc7ANyO4c8fp4EZjgHNxcbrUESp5IZgVwSmOgbE1KbERiwSGRYyAAM2JDtpxXiCAaUSN0gE4WEQQe17vLYdUGOA0NFhcsiDkxkG42I4lMFIdY10AN6tlBfGR+FztHdsB/BpCfxJsdiVd1aQ1vhJJvjVgCDeCmp/1cf4SmhElQFCx1i+Y6J5Qc35HRSGPD09bTTxrUjqHAYJPyAmgmxgXEw0KdLTMyOi5AmPx+OqJ/wB2+PjowNEC+bmxMhEUER+Ei41mEPVPBRbTIaBImJqgEGE/K5ccFgFiP4LwDiXB0euAA+fMY6SAYyT90YpKr1er7hA1u2AZIAsZnUl4RE5GtFEoCZJBgVAOIQWiTEfBSyWHS3UwysrK79+/VJlQRe0jpIGGAaG4TWBRavR0LLOdzWtdWtr6+rqSjXRSbQ5Fo2R0FT/wMAwvDbPDYgiYqqdyKfnUUIu7DyJG9UaGOeuY2Du8IxEl2qPTPtT9y49PWftC4E5CnYPh0OJFCqIBUl6w5AODg6SxxF5zFlom7yLCZcNvif1MHiAp8Y8MkmMxRaF5ynJ/8LAhOa/5EqZWhGG5ZBssLIHg8Ht7S0jSdpJWXQ0GAxlBMnRNz5WyNBUybICMlp0sEoAU+/o6EjrFZeyiGp7go6MBa1PYaqPWYmqlbDkYjLJjrxi+DvVELE5gCUnCxJgANPplJwvm48pnxa1GdqcQUYnZLDrbWlCBRsG0O/3YfvH5kt4Pi4WiOex6/QOyOcBs4uxD0ajEeTvoiIzeDQG4OqqSAK5ySixKoGugB+QyRtIXjifNR7EsrK0xj6floKH4tG+6FetuUw0tIwjZtZIdK/jQTAbJQaf8VZmkoZm3z8vL6zFUR6ME0JvfHiST5D3NyBVpiEeHB4e1ngQW7kxPgmTi/AHHoTHra2tudpajOueNDQYpR38KVwvqIgIsu1HV6JmZ2dnPp/7DlpBQBSd4xF4EB4nPOvSKhhn2IAqjc0cqqGzq9vb2/BI/fN7yWQWSSQH3aJzPILwzoJ1sMxkbydkiuQCyYsWBk8ckl2EMQ2Hw9lsxpwj0wIS6Aodolt0zh+F5FNDmFeT1M8Nm6FNgHESSIv3XzCy8Xh8dXWFZYvpM5TswEXciNvRCbqi2ihy75JfZ/l8agQLyiyFXrsdnhpE5P7+/tnZGdQmFefj1zv9CjIQ4xbciNvxkuhKKAkdWtUR1+E3Z4g2DsblpPNRQjUa5DVGCcG9t7cHw3E6nWJFw33F/N7d3f1+bfgD/8VF/AQCkIEYt+BG3E79FAxyGY6G2RlgQH4MBy6Tb0vTB9MFe6Lf7w8Gg93d3b2/DX/DsMFFTDoIqPpJrdArj1bm5BvYJFuCAS0bZMK5oLX86bX9L9LwE633sjUFFhCSbJMN/FJ1a9mVFCUxXW3uSm3fxDght5gXDMYVmVwbA6oDxcxC/nS7XacMJpPJyckJ1CyEPv6FgY8rsHZga0IcgRKOLmGcwdr9BgGiAuOSFT9bBeNsuaQ07xsbGxDxmNbj42PY8peXl1C29/f3Dw8PziiiBsuHyrReX1/D3aUCxrgXzIAVlAyymzMB+dIMdiyoOPwgVFwkwTFlVDIay5xmPGmD1uxRMkZxL7bIaDTa2tqiyrlBi0hStzZ/i/uUIgY0B8b5vWHJkPmPJQ+bknfB5GgEmAFOYEP0er2qjdSJlLnigRlhvFcCxtVFUCYWlEPmyrceHBxAgFSrddti9z49thGYCv1BXrFvpxrWdeZbt4GGJh0FmggsTBjyEBfBSlrJk9mx6E0w/+f8/ByaHFq6hgtJ3KtSiEun+gWNsvpW9YEQMhYxF5A5ULDtHN8AY5xE6vyt1cdbPow4Ur17AAuyVflPOpaSx9ObQ+xALNjqV+ZUSIHJhA0Hu9aPCecbFIqSZQWdbC16hT+wBmEv1hKEhJUok8BcsjdsuIuLC/CgWjfU8JoFMuPKJmYlVbpTuZh9Oe7fxAk9qk3Q7/eDp2I64io1vEFlh6Mz4TnGpiLE31WNLjiz2hAmhWvgKPixmqZtpMJWkJxh0Lp+NspiD0eenZ1RqabMzNesmHA7YBwsThj7rX0tQM4DeN30uR9J+T2bFa4QQTZMn7f6yeSH0uNrFjA5JsEzjsIDMMwf1OCCjEYjUshyMC6nmr0djDPnTGD5Hx8fB3NPYgcZSx2DSVY2e3mtXEmGqTCCViwzLv+7fsmnUuLJzs5OrW4on+3s56RkJmbxmwAKGYKITsprkQY+ua1GRkCp7qB28uNOfE4AnRN2KekFTxeVtaNgGmCVuFhCDgjR0R5TbQ6MI+EDCVvVvUwWfykGGA6aYX3AKo0VrywbDil5TDVZMBnyLla1dYHfDwhSQhvDTiNNwJz7lIvfmCwqnJjFbMxaoYgP3sASuAV0Zk+ePyohU4NxRUwjKhYE44dQhzY/DGCWYzc3N4PBwCzoi4FxwrWftD6paPoHr1dW88smk4nk3KShZrfiGzKSzLhkhBKvMRwOa6CbsM5zc0ImSTCfz51jrA1J8jQlwbhkqgFVrcdqUrm+LRycT5Jhy25ubvq1O1o6plqqYfR0IrXImd7kMdWkgZssZlwt3uSQieJ5N1kBGe0WqXq/qmC6xDYtpX59MgfPOVso55TkIhOzoAD8SmWquYuhbCowjsfpggyYTqfwlXgGJLMTO3zJMsnB9hrGIGnVpx4eHkq+0Sx0jCVQT6aF6q5AD3e7XVvmloQsUbiVSVeKTbffYAJBA8fSHSSfGjTPtTxiHCO7urra3t7umIpGSBgQwIJyAInggKDEXIUmYZ2mhWBBwSu3t7dw4GPHyvLNoSgYZ8MngrfABo19OKzsp3maiOzTt1aZc32ZmrJevj55yC8ZDPLHBAbUbFDVN1KTu0RFoyWrfWNSWK1ZXtT5zw6Q6HHzPgADZrNZZmZn5hYxPw4M2N/fZxigFfodpnZ0MpSsjcm5GKTPgI8AAUmeCOOtyoDi4YHADtDibkl4lkRQWSChteZ2gMQZNhwUSJuhOQXteQYIvaGPwwDtJwQ67Ed1mvWEa2kQMREkmfqGSpYJYSUSQX49rZwM2sIhyeR28xkQhMNsn65qLl+xugNqgKgwe06S5Rj4gob8UyVmBshBoSIAQw4vaztAmPYarOOqQ0NVhXR4cML5AbHPCzJwsWr6JAWlGekU7KdqBeV8PS/GHg6ME36nJgnG+Y5YcoIaAuMksx9kgKsvJEmJUJ1VyfqgszBYGmSA5BOPBUuWGXKNXEzG1wESOECdmthELrRjAOOIfcDMuCAD/h1gnIEBLYNxWic5aYZ+RDAuyQD5hEoQNImeSBr+SR2QyYAYmcIPMHwmRY4FCb+Dl3Ni2/adPQYLkhdfsIBxHf2J35jnrdUBCwEkYk/MB+N0DEgmG6nMXqEOWGyGlnAHVM1QSWo+Ez8IV00seECjxrCYI9ZcfmfB8IAEiug0UbKsk/ElM98Rox0g/PJ7O2CcpKsFgHEFi+3xYJwkmN7y4eEgDeMJJyWzBC8KlKvJ2RBBAEMORSwwRhbjkAGMU1XhUlRN1OJx7roKC2p6H2jJgjugSGisHhEz9xhMzDLvADMPGkpLiSnhUolZ76wg+ck/n8DV+vU3YDIrogUw7kV8TNWABeWcWHpXM64he7TKANsC/y+CcZ0Sx1STDGg/M87mCSfN0FwGxL4BkPk8BowrtfBVJqbBEq1BEWUTs+jbLUsbrw3a4Odr+1FpPyut9t/qdfrJ/Vu9gtbr9S4uLhqSPE1vkYeHh/F43O12/WnxJyQ4df40uoZpHwwG/wc1kGJ/hI+0pQAAAABJRU5ErkJggg==';

    return this;
  }
]);
})();

(function(){
'use strict';

uw.service('restService', [
  '$rootScope',
  '$http',
  '$q',
  '$sce',
  'request',
  'xmlParser',
  'sharedFactory',
  'CONFIG',
  '_',
  function ($rootScope, $http, $q, $sce, request, xmlParser, sharedFactory, CONFIG, _) {
    var baseRest        = CONFIG.baseRest;
    var baseRestNew = baseRest.replace("rest","api");
    var sipRest         = initSipRest();
    var msgUrls         = {};
    var self            = this;

    this.cred = {
      user: '',
      pass: ''
    }
    this.connected      = false;
    this.phonebook      = null;
    this.activityList   = null;

    // UNIMPLEMENTED
    // set Basic HTTP auth headers for all requests
    // $http.defaults.headers.common.Authorization = 'Basic ' + b64;

    /**
     * updates credentials and sets sipRest $http abstract
     * called from chatService upon successful login
     * @param  {String} user      XMPP user
     * @param  {String} pass      XMPP password
     */
    this.updateCredentials = function (user, pass) {
      this.cred.user = user || null;
      this.cred.pass = pass || null;
    }

    /**
     * gets and parses the CDR log
     * @param  {String} limit       maximum number of call logs to return
     * @return {Promise}            parsed log
     */
    this.getCDRlog = function (limit) {
      var deferred = $q.defer();

      sipRest.getCDRlog(limit).
        success(function (data) {
          parseXml(data, function (parsed) {
            deferred.resolve(parsed);
          })
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    /**
     * gets and parses messages from a folder
     * @param  {String} folder  folder name
     * @return {Promise}        parsed messages
     */
    this.getMessages = function (folder) {

      var deferred  = $q.defer();

      sipRest.getMessages(folder).
        success(function (data) {
          parseXml(data, function (parsed) {
            deferred.resolve(parsed);
          });
        }).
        error(function(e) {
          deferred.reject(e);
        })

      return deferred.promise;

    }

    /**
     * gets one message based on id
     * @param  {String} id      message ID
     * @return {Object}         promise  audio blob URL || error
     */
    this.getMessage = function (id) {
      var deferred    = $q.defer();
      var blob;

      if (msgUrls[id])
        deferred.resolve(msgUrls[id])
      else
        sipRest.getMessage(id).
          success(function (data) {
            blob          = new Blob([data]); // the blob
            msgUrls[id]   = $sce.trustAsResourceUrl(window.URL.createObjectURL(blob)); // the blob URL
            deferred.resolve(msgUrls[id]); // resolved
          }).
          error(function (e) {
            deferred.reject(e);
          })

      return deferred.promise;
    }

    this.delMessage = function (id) {
      var deferred = $q.defer();

      sipRest.putTrashMsg(id).
        success(function (data) {
          delete msgUrls[id];
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.heardMessage = function (id) {
      var deferred = $q.defer();

      sipRest.putHeardMsg(id).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putMoveMsg = function (id, folder) {
      var deferred = $q.defer();
      id = id.trim();

      sipRest.putMoveMsg(id, folder).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getHeardMessage = function (id) {
      var deferred = $q.defer();

      sipRest.getHeardMsg(id).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putSubjectMessage = function (id, sub) {
      var deferred = $q.defer();

      sipRest.putSubjectMessage(id, sub).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.postBasicCall = function (to) {
      var deferred = $q.defer();

      sipRest.postBasicCall(to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getBasicCall = function (to) {
      var deferred = $q.defer();

      sipRest.getBasicCall(to).
        success(function (data) {
          parseXml(data, function (parsed) {
            deferred.resolve(parsed);
          });
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfList = function () {
      var deferred  = $q.defer();

      sipRest.getConfList().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putConfInvite = function (conf, to) {
      var deferred = $q.defer();

      sipRest.putConfInvite(conf, to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfPartList = function (conf) {
      var deferred = $q.defer();

      sipRest.getConfPartList(conf).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (data, status) {
          deferred.reject(status);
        })

      return deferred.promise;
    }

    this.getConfSettings = function (conf) {
      var deferred = $q.defer();

      sipRest.getConfSettings(conf).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (data, status) {
          deferred.reject(status);
        })

      return deferred.promise;
    }

    this.putConfSettings = function (conf, data) {
      var deferred = $q.defer();

      sipRest.putConfSettings(conf, data).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (data, status) {
          deferred.reject(status);
        })

      return deferred.promise;
    }

    this.getConfKick = function (conf, to) {
      var deferred = $q.defer();

      sipRest.getConfKick(conf, to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfMute = function (conf, to) {
      var deferred = $q.defer();

      sipRest.getConfMute(conf, to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfUnmute = function (conf, to) {
      var deferred = $q.defer();

      sipRest.getConfUnmute(conf, to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfDeaf = function (conf, to) {
      var deferred = $q.defer();

      sipRest.getConfDeaf(conf, to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfUndeaf = function (conf, to) {
      var deferred = $q.defer();

      sipRest.getConfUndeaf(conf, to).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getConfPin = function (conf, pin) {
      var deferred = $q.defer();

      sipRest.getConfPin(conf, pin).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getPhonebook = function () {
      var deferred = $q.defer();

      sipRest.getPhonebook().
        success(function (data) {
          $rootScope.$broadcast('services.rest.phonebook', {});
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putPassword = function (pass) {
      var deferred = $q.defer();

      sipRest.putPassword(pass).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getImBot = function () {
      var deferred = $q.defer();

      sipRest.getImBot().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putImBot = function (data) {
      var deferred = $q.defer();

      sipRest.putImBot(data).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getSpeeddial = function () {
      var deferred = $q.defer();

      sipRest.getSpeeddial().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putSpeeddial = function (data) {
      var deferred = $q.defer();

      sipRest.putSpeeddial(data).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getContactInfo = function () {
      var deferred = $q.defer();

      sipRest.getContactInfo().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putContactInfo = function (data) {
      var deferred = $q.defer();

      sipRest.putContactInfo(data).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getLogindetails = function (data) {
      var deferred = $q.defer();

      sipRest.getLogindetails().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getSchedule = function () {
      var deferred = $q.defer();

      sipRest.getSchedule().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putSchedule = function (id, d) {
      var deferred = $q.defer();

      sipRest.putSchedule(id, d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.postSchedule = function (d) {
      var deferred = $q.defer();

      sipRest.postSchedule(d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.delSchedule = function (id) {
      var deferred = $q.defer();

      sipRest.delSchedule(id).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getForward = function () {
      var deferred = $q.defer();

      sipRest.getForward().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putForward = function (d) {
      var deferred = $q.defer();

      sipRest.putForward(d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getPersonalAttendant = function () {
      var deferred = $q.defer();

      sipRest.getPersonalAttendant().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putPersonalAttendant = function (d) {
      var deferred = $q.defer();

      sipRest.putPersonalAttendant(d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getPersonalAttendantLang = function () {
      var deferred = $q.defer();

      sipRest.getPersonalAttendantLang().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getVmPrefs = function () {
      var deferred = $q.defer();

      sipRest.getVmPrefs().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putVmPrefs = function (d) {
      var deferred = $q.defer();

      sipRest.putVmPrefs(d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getKeepAlive = function () {
      var deferred = $q.defer();

      sipRest.getKeepAlive().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putVmPin = function (d) {
      var deferred = $q.defer();

      sipRest.putVmPin(d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getMohPermission = function () {
      var deferred = $q.defer();

      sipRest.getMohPermission().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getMohSettings = function () {
      var deferred = $q.defer();

      sipRest.getMohSettings().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.putMohSettings = function (d) {
      var deferred = $q.defer();

      sipRest.putMohSettings(d).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getMohFiles = function () {
      var deferred = $q.defer();

      sipRest.getMohFiles().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.deleteMohFile = function (name) {
     var deferred = $q.defer();

     sipRest.deleteMohFile(name).
       success(function (data) {
         deferred.resolve(data);
       }).
       error(function (e) {
         deferred.reject(e);
       })

     return deferred.promise;
    }

    this.listenMohFile = function (name) {
      var deferred = $q.defer();

      sipRest.listenMohFile(name).
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })
      return deferred.promise;
    }

    this.getIMproperties = function () {
      var deferred = $q.defer();

      sipRest.getIMproperties().
        success(function (data) {
          deferred.resolve(data);
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    this.getAllImages = function (phonebookList) {
      var promises    = createPromiseArray(phonebookList);
      var avatarUrl   = '';
      var url         = '';

      $q.all(promises).
        then(function (responses) {
          createObjectURL(phonebookList, responses);
        }).
        catch(function (err) {
          console.log(err);
        });

      function createPromiseArray(array) {
        var promises = [];

        _.each(array, function (entry) {
          // Production:
          // url = entry['contact-information'].avatar || '';
          if (entry['contact-information'] && entry['contact-information'].avatar) {
            if (entry['contact-information'].avatar.indexOf('/sipxconfig/rest') !== -1) {
              avatarUrl   = entry['contact-information'].avatar.split('/sipxconfig/rest')[1];
              url         = CONFIG.baseRest + avatarUrl;
            } else {
              url = '';
            }
          } else {
            url = '';
          };

          promises.push($http.get(url, {cache: true, responseType: 'arraybuffer'}));
        });

        return promises;
      }

      function createObjectURL(array, values) {
        var i = 0;
        var blob, url;

        array.forEach(function (el, i) {
          if (!_.isUndefined(values[i]) && values[i].config.url !== '') {
            blob              = new Blob([values[i].data], {type: 'image/png'});
            url               = window.URL.createObjectURL(blob)
            array[i].avatar   = url;
          } else if (values[i].config.url === '') {
            array[i].avatar   = sharedFactory.defaultImg;
          }
        })
      }
    }

    this.getCallHistory = function () {
      var deferred = $q.defer();

      sipRest.getCallHistory().
        success(function (data) {
          parseXml(data, function (parsed) {
            deferred.resolve(parsed);
          })
        }).
        error(function (e) {
          deferred.reject(e);
        })

      return deferred.promise;
    }

    /**
     * initializes sipRest methods
     * eZuce proprietary
     * @return {[type]} [description]
     */
    function initSipRest() {
      return {
        getCDRlog: function (limit) {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/cdr/' + self.cred.user + '?limit=' + limit
          }))
        },
        getUserInfo: function (user) {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/search/phonebook?query=' + self.cred.user
          }))
        },
        getMessages: function (folder) {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/' + folder
          }))
        },
        getMessage: function (id) {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/media/' + self.cred.user + '/inbox/' + id,
            responseType: 'arraybuffer'
          }))
        },
        putHeardMsg: function (id) {
          return request(authHeaders({
            method:  'PUT',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/message/' + id + '/heard',
            data:    {}
          }))
        },
        putMoveMsg: function (id, folder) {
          return request(authHeaders({
            method:  'PUT',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/message/' + id + '/move/' + folder,
            data:    {}
          }))
        },
        getHeardMsg: function (id) {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/message/' + id + '/heard'
          }))
        },
        putTrashMsg: function (id) {
          return request(authHeaders({
            method:  'PUT',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/message/' + id + '/delete',
            data:    {}
          }))
        },
        putSubjectMessage: function (id, sub) {
          return request(authHeaders({
            method:  'PUT',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/message/' + id + '/subject',
            data:    sub
          }))
        },
        getActiveGreeting: function () {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/preferences/activegreeting'
          }))
        },
        delActiveGreeting: function () {
          return request(authHeaders({
            method:  'DELETE',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/preferences/activegreeting'
          }))
        },
        putActiveGreeting: function (greeting) {
          return request(authHeaders({
            method:  'PUT',
            body:    '<activegreeting>' + greeting + '</activegreeting>',
            url:     baseRest + '/my/redirect/mailbox/' + self.cred.user + '/preferences/activegreeting'
          }))
        },
        postBasicCall: function (to) {
          return request(authHeaders({
            method:  'POST',
            url:     baseRest + '/my/redirect/callcontroller/' + self.cred.user + '/' + escape(to)
          }))
        },
        getBasicCall: function (to) {
          return request(authHeaders({
            method:  'GET',
            url:     baseRest + '/my/redirect/callcontroller/' + self.cred.user + '/' + escape(to)
          }))
        },
        getConfList: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conferences',
            headers: {
              'Accept': 'application/json'
            }
          }));
        },
        getConfSettings: function (conf) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conferences/' + conf,
            headers: {
              'Accept': 'application/json'
            }
          }));
        },
        putConfSettings: function (conf, data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/conferences/' + conf,
            data:   data
          }));
        },
        putConfLock: function (name) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/conference/' + name + '/lock'
          }));
        },
        putConfInvite: function (conf, to) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/invite\&' + to
          }));
        },
        getConfPartList: function (conf) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conferencedetails/' + conf,
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        getConfKick: function (conf, to) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/kick\&' + to
          }));
        },
        getConfMute: function (conf, to) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/mute\&' + to
          }));
        },
        getConfUnmute: function (conf, to) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/unmute\&' + to
          }));
        },
        getConfDeaf: function (conf, to) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/deaf\&' + to
          }));
        },
        getConfUndeaf: function (conf, to) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/undeaf\&' + to
          }));
        },
        getConfPin: function (conf, pin) {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/conference/' + conf + '/pin\&' + pin
          }));
        },
        getPhonebook: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/phonebook',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putPassword: function (pass) {
          return request(authHeaders({
            method:  'PUT',
            url:     baseRest + '/my/portal/password/' + encodeURIComponent(pass),
            data:    {}
          }))
        },
        getImBot: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/imbot/prefs'
          }))
        },
        putImBot: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/imbot/prefs',
            data:   data
          }))
        },
        getSpeeddial: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/speeddial'
          }))
        },
        putSpeeddial: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/speeddial',
            data:   data
          }))
        },
        getContactInfo: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/contact-information',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putContactInfo: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/contact-information',
            data:   data
          }))
        },
        getLogindetails: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/logindetailsunite',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        getSchedule: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/callfwdsched/',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putSchedule: function (id, data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/callfwdsched/' + id,
            data:   data
          }))
        },
        postSchedule: function (data) {
          return request(authHeaders({
            method: 'POST',
            url:    baseRest + '/my/callfwdsched/',
            data:   data
          }))
        },
        delSchedule: function (id, data) {
          return request(authHeaders({
            method: 'DELETE',
            url:    baseRest + '/my/callfwdsched/' + id
          }))
        },
        getForward: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/callfwd/',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putForward: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/callfwd/',
            data:   data
          }))
        },
        getPersonalAttendant: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/voicemail/attendant',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putPersonalAttendant: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/voicemail/attendant',
            data:   data
          }))
        },
        getPersonalAttendantLang: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/voicemail/attendant/lang',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        getVmPrefs: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/vmprefs',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putVmPrefs: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/vmprefs',
            data:   data
          }))
        },
        putVmPin: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRest + '/my/voicemail/pin/' + data,
            data:   data
          }))
        },

        getMohPermission: function () {
          return request(authHeaders({
            method: 'GET',
            url:   baseRestNew + '/my/moh/permission',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        getMohSettings: function () {
          return request(authHeaders({
            method: 'GET',
            url:   baseRestNew + '/my/moh/settings/moh/audio-source',
            headers: {
              'Accept': 'application/json'
            }
          }))
        },
        putMohSettings: function (data) {
          return request(authHeaders({
            method: 'PUT',
            url:    baseRestNew + '/my/moh/settings/moh/audio-source',
            headers: {
              'Content-type': 'text'
            },
            data: data
          }))
        },
        getMohFiles: function () {
          return request(authHeaders({
            method: 'GET',
            url:   baseRestNew + '/my/moh/prompts'
          }))
        },
        deleteMohFile: function (data) {
          return request(authHeaders({
            method: 'DELETE',
            url:   baseRestNew + '/my/moh/prompts/'+data
          }))
        },
        listenMohFile: function (data) {
          return request(authHeaders({
            method: 'GET',
            url:   baseRestNew + '/my/moh/prompts/'+data
          }))
        },
        getIMproperties: function () {
          return request(authHeaders({
            method: 'GET',
            url:    '/unite/im.properties'
          }))
        },

        getKeepAlive: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/keepalive'
          }))
        },

        getCallHistory: function () {
          return request(authHeaders({
            method: 'GET',
            url:    baseRest + '/my/redirect/cdr/'+ self.cred.user
          }))
        },

        // generates requests functions
        // like above, but instead passing through a list of objects
        // function genReqFn(list) {
        //   var res;
        //   _.each(list, function (obj, key) {
        //     res[key] = function (param) {
        //       return request(authHeaders({
        //         method: obj.method,
        //         url: obj.url,
        //         body: obj.body || null,
        //         responseType: obj.responseType || null
        //       }))
        //     }
        //   })
        //   return res;
        // }
      }
    }

    /**
     * parses general XML response from the server
     * @param  {String} xml         XML string
     * @param  {Function} cb        callback
     * @return {Array}              array of attributes
     */
    function parseXml (xml, cb) {
      var $             = angular.element;
      var children      = xmlParser.parse(xml).children ? ($(xmlParser.parse(xml).children)[0].children) : ($(xmlParser.parse(xml)).children().children()) ? ($(xmlParser.parse(xml)).children().children()) : null;
      var arr           = [];
      var attrs         = [];
      var obj           = {};
      var subChildren   = [];

      _.each(children, function (child) {
        attrs         = $(child)[0].attributes;
        subChildren   = $(child).children();
        if (!_.isEmpty(attrs)) {
          obj = parseAttrs(child);
          if (!_.isEmpty(obj)) {
            arr.push(obj);
            obj = {};
          }
        };

        if (!_.isEmpty(subChildren)) {
          _.each( subChildren, function (c) {
            switch ($(c)[0].tagName) {
              // CDR
              case 'caller_aor':
                obj['from']       = $(c)[0].textContent; break;
              case 'callee_aor':
                obj['to']         = $(c)[0].textContent; break;
              case 'start_time':
                obj['start']      = $(c)[0].textContent; break;
              case 'duration':
                obj['duration']   = $(c)[0].textContent; break;

              // Call Status
              case 'timestamp': case 'call-id': case 'method':
                break;
              case 'status-line':
                obj['status']     = $(c)[0].textContent; break;
              default:
                break;
            }
          })
          if (!_.isEmpty(obj)) {
            arr.push(obj);
            obj = {};
          }
        }

      });

      cb(arr);
    }

    /**
     * parses NamedNodeMaps attributes
     * @param  {NamedNodeMap} NamedNodeMap    DOM node
     * @return {Object}                       an object containing attribute names and their values
     */
    function parseAttrs (NamedNodeMap) {
      var attrs   = angular.element(NamedNodeMap)[0].attributes;
      var obj     = {};

      _.each(attrs, function (attr) {
        obj[attr.name.toString()] = filterXmlValue(attr.value.toString());
      });

      return obj;
    }

    /**
     * transforms string value into corresponding type
     * works only with Strings, Numbers & Booleans
     * e.g. "1001" into 1001
     *      "true" into true
     * @param  {String} val                the value to be converted
     * @return {String|Number|Boolean}     converted value
     */
    function filterXmlValue(val) {
      var value = val;

      switch (value.toLowerCase()) {
        case 'true': case 'yes': case '1':
          value = true; break;

        case 'false': case 'no': case '0':
          value = false; break;

        default:
          if (!_.isNaN(parseInt(value)))
            value = value.toString();
          else
            value = unescape(val);
      }

      return value;
    }

    /**
     * extends request factory configuration object with auth headers
     * @param  {Object} conf request factory ($http) configuration object
     * @return {Object}      extended object
     */
    function authHeaders(conf) {
      if (window.navigator.userAgent.indexOf('MSIE ') !== -1 || window.navigator.userAgent.indexOf('Trident/') !== -1) {
        conf.url = conf.url + '?invcache=' + Math.floor((Math.random() * 99999) + 10000);;
      }
      return _.extend(conf, {
          cache: false
        });
    }

  }
]);
})();

(function(){
  'use strict';

  uw.service('uiService', [
    '$rootScope',
    '$interval',
    '$sce',
    'sharedFactory',
    'restService',
    '_',
    'CONFIG',
    function ($rootScope, $interval, $sce, sharedFactory, restService, _, CONFIG) {

      var activityList = {
        main: null
      };

      var rosterList = {
        main:     null,
        noGroups: null
      };

      var search = {
        t: ''
      };

      var timers = {
        all: {}
      };

      var groupChat = {
        modal: false
      };

      var ui = {

        root: {
          templates:    sharedFactory.settings.mainMenu,
          template:     sharedFactory.settings.mainMenu[1],
          oldTemplate:  sharedFactory.settings.mainMenu[1]
        },

        groupChat: {

          showModal : function () {
            groupChat.modal = !groupChat.modal;

            return;
          },

          hideModal : function () {
            groupChat.modal           = !groupChat.modal;
            groupChat.serverRooms     = null;

            return;
          }

        },

        activityList: {

          selectConversation : function () {

          },

          removeConversation : function (entry) {

            var spliceFromActiList = function () {
              _.find(activityList.main, function (obj) {
                if (obj.jid === entry.jid) {
                  activityList.main.splice(_.indexOf(activityList.main, obj), 1);
                  sharedFactory.saveConversationList(angular.copy(activityList.main));
                  return true;
                }
              });
            };

            spliceFromActiList();

            sharedFactory.delContactMessageList(entry.jid);

            return;

          }

        }

      };

      var secondary = {

        template : {
          main: ui.root.templates[ui.root.templates.length - 1],
        },

        logout : {
          init : function () {
            window.location.assign('/sipxconfig/rest/my/logindetailsunite/logout');
          }
        },

        chat: {

          showDefault: function () {
            util.changeView(ui.root.templates[10]);
            secondary.conference.participants   = [];
            secondary.voicemail.messages        = [];
            delete secondary.voicemail.folder;
            secondary.conference.active         = false;
            secondary.conference.timers.cancelAll(true);
            $rootScope.leftSideView = '';
            return;
          }

        },

        voicemail: {

          messages: [],

          backup: [],

          folder: {},

          tooltip: {
            refresh: {
              title: 'Refresh',
              checked: false
            },
            download: {
              title: 'Download',
              checked: false
            },
            trash: {
              title: 'Trash',
              checked: false
            }
          },

          folders: [
            { name: 'inbox' },
            { name: 'saved' },
            { name: 'deleted' },
            { name: 'conference' }
          ],

          moveVoicemail: {},

          init: function () {
            var temp;
            secondary.voicemail.messages  = null;
            secondary.voicemail.messages  = [];
            secondary.voicemail.backup    = [];
            if (!_.isEmpty(secondary.voicemail.folder)) {
              if (secondary.voicemail.folder.name === 'all') {
                temp = 'messages';
              } else {
                temp = angular.copy(secondary.voicemail.folder.name);
              }
              restService.getMessages(temp).then(function (messages) {
                var msgs = {};
                _.map(messages, function (message) {
                  msgs[message.id] = message;
                })
                secondary.voicemail.messages  = angular.copy(msgs);
                secondary.voicemail.backup    = angular.copy(secondary.voicemail.messages)
              })
            } else {
              restService.getMessages('inbox').then(function (messages) {
                var msgs = {};
                _.map(messages, function (message) {
                  msgs[message.id] = message;
                })
                secondary.voicemail.messages  = angular.copy(msgs);
                secondary.voicemail.folder    = secondary.voicemail.folders[0];
                secondary.voicemail.backup    = angular.copy(secondary.voicemail.messages);
              }, function (er) {
                console.log(er);
              });
            }
          },

          changeFolder: function () {
            var folder = secondary.voicemail.folder.name;

            if (folder === 'all') {
              folder = 'messages';
            }

            secondary.voicemail.messages  = null;

            restService.getMessages(folder).then(function (messages) {
              var msgs = {};
              _.map(messages, function (message) {
                msgs[message.id] = message;
              })
              secondary.voicemail.messages  = angular.copy(msgs);
              secondary.voicemail.backup    = angular.copy(secondary.voicemail.messages);
            }, function (er) {
              console.log(er);
            });
          },

          getMessage: function (id, index) {
            var bId = angular.copy(id);
            var bIndex = angular.copy(index);
            secondary.voicemail.messages[bId]['loading'] = true;
            // secondary.voicemail.messages[index].href = CONFIG.baseRest + '/my/redirect/media/' + restService.user + '/inbox/' + id
            restService.getMessage(bId)
              .then(function (url) {
                // secondary.voicemail.messages[index].href   = url;
                secondary.voicemail.messages[bId].href = $sce.trustAsResourceUrl(CONFIG.baseRest +'/my/redirect/media/' + restService.cred.user + '/inbox/' + id);
                delete secondary.voicemail.messages[bId].loading;
                return restService.heardMessage(bId);
              })
              .then(function () {
                return restService.getHeardMessage(bId);
              })
              .then(function () {
                secondary.voicemail.messages[bId].heard  = true;
                delete secondary.voicemail.messages[bId].loading;
              })
          },

          delMessage: function (id, index) {
            var bId = angular.copy(id);
            var bIndex = angular.copy(index);
            secondary.voicemail.messages[bId]['loading'] = true;

            if (secondary.voicemail.folder.name === 'deleted') {
              restService.delMessage(bId).then(function () {
                delete secondary.voicemail.messages[bId];
              }, function (er) {
                console.log(er);
              });
            } else {
              restService.putMoveMsg(id, 'deleted').then(function () {
                delete secondary.voicemail.messages[bId];
              }, function (er) {
                console.log(er);
              });
            }

          },

          moveMessage: function (id, index) {
            var bId = angular.copy(id);
            var bIndex = angular.copy(index);
            var folder = angular.copy(secondary.voicemail.moveVoicemail[bId]);
            secondary.voicemail.messages[bId].loading = true;
            if (folder.name === 'deleted') {
              secondary.voicemail.messages[bId].href = '';
            }
            restService.putMoveMsg(id, folder.name).then(function () {
              secondary.voicemail.moveVoicemail = {};
              secondary.voicemail.messages[bId].loading = false;
              delete secondary.voicemail.messages[bId];
            }).catch(function (err) {
              console.log(err);
              secondary.voicemail.messages[bId].loading = false;
            })
          },

          changeSubject: function (index) {
            var subject   = angular.copy(secondary.voicemail.messages[index].subject);
            var id        = angular.copy(secondary.voicemail.messages[index].id);
            secondary.voicemail.messages[index].loading = true;
            restService.putSubjectMessage(id, subject).then(function () {
              secondary.voicemail.messages[index].loading = false;
            }).catch(function (err) {
              console.log(err);
              secondary.voicemail.messages[index].loading = false;
            })
          },

          cancelChangeSubject: function (index) {
            secondary.voicemail.messages[index] = angular.copy(secondary.voicemail.backup[index]);
            return;
          },

          isEmpty: function () {
            for (var key in secondary.voicemail.messages) {
              if(secondary.voicemail.messages.hasOwnProperty(key)) {
                return false
              }
            }
            return true
          },

          mp3Ie: function (audioFormat) {
            if (audioFormat === 'mp3') {
              return true;
            }

            if (window.navigator.userAgent.indexOf('MSIE ') !== -1 || window.navigator.userAgent.indexOf('Trident/') !== -1  || !!navigator.userAgent.match(/Version\/[\d\.]+.*Safari/)) {
              return false;
            }

            return true;
          }

        },

        callhistory: {
          startTime: new Date(),
          isOpenStartDate: false,
          openStartDate: function () {
            secondary.callhistory.isOpenStartDate = true;
          },
          endTime: new Date(),
          isOpenEndDate: false,
          openEndDate: function () {
            secondary.callhistory.isOpenEndDate = true;
          },
          calls:[],
          number:null,
          availableOptions: [
            {id: '1', name: '- all -'},
            {id: '2', name: 'From'},
            {id: '3', name: 'To'},
            {id: '4', name: 'From or To'}
          ],
          selectedOption: {id: '1', name: '- all -'},
          init: function () {
            secondary.callhistory.startTime.setHours(0);
            secondary.callhistory.startTime.setMinutes(0);
          },
          apply: function () {
            secondary.callhistory.calls = [];
            restService.getCallHistory().then(function (data) {
              //filter by date
              for(var i = 0; i < data.length; i++){
                var a=data[i].start.split(" ");
                var d=a[0].split("-");
                var t=a[1].split(":");
                var date = new Date(d[0],(d[1]-1),d[2],t[0],t[1],t[2]);
                if(date.getTime() < secondary.callhistory.endTime.getTime() && date.getTime() > secondary.callhistory.startTime.getTime()){
                  //filter by select box
                  if(secondary.callhistory.selectedOption.name === '- all -') {
                    data[i].from = data[i].from.match("sip:(.*)@");
                    data[i].from = data[i].from[1];
                    data[i].to = data[i].to.match("sip:(.*)@");
                    data[i].to = data[i].to[1];
                    secondary.callhistory.calls.push(data[i]);
                  }
                  else if(secondary.callhistory.selectedOption.name === 'From'){
                    if(data[i].from.indexOf(secondary.callhistory.number.toString()) > -1){
                      data[i].from = data[i].from.match("sip:(.*)@");
                      data[i].from = data[i].from[1];
                      data[i].to = data[i].to.match("sip:(.*)@");
                      data[i].to = data[i].to[1];
                      secondary.callhistory.calls.push(data[i]);
                    }
                  }
                  else if(secondary.callhistory.selectedOption.name === 'To'){
                    if(data[i].to.indexOf(secondary.callhistory.number.toString()) > -1){
                      data[i].from = data[i].from.match("sip:(.*)@");
                      data[i].from = data[i].from[1];
                      data[i].to = data[i].to.match("sip:(.*)@");
                      data[i].to = data[i].to[1];
                      secondary.callhistory.calls.push(data[i]);
                    }
                  }
                  else{
                    if(data[i].from.indexOf(secondary.callhistory.number.toString()) > -1 || data[i].to.indexOf(secondary.callhistory.number.toString()) > -1) {
                      data[i].from = data[i].from.match("sip:(.*)@");
                      data[i].from = data[i].from[1];
                      data[i].to = data[i].to.match("sip:(.*)@");
                      data[i].to = data[i].to[1];
                      secondary.callhistory.calls.push(data[i]);
                    }
                  }
                }
              }
            }, function (err) {
              console.log(err);
            });
          }
        },

        conference: {

          rooms:        null,

          select:       null,

          active:       null,

          refresh:      null,

          status:       null,

          pin:          null,

          err:          null,

          btnDisabled:  false,

          participants: [],

          tooltip: {
            title: 'Refresh',
            checked: true
          },

          init: function () {
            if (!secondary.conference.err) {
              secondary.conference.rooms    = null;
              secondary.conference.err      = null;
            }
            secondary.conference.status   = 'Searching for conference rooms...';
            secondary.conference.refresh  = null;
            restService.getConfList()
              .then(function (data) {
                if (_.isEmpty(data.conferences)) {
                  secondary.conference.status   = 'No conference rooms found. ';
                  secondary.conference.err      = true;
                  ui.root.templates[2].show = false;
                  return;
                } else {
                  secondary.conference.err      = null;
                }
                secondary.conference.rooms    = data.conferences;
                secondary.conference.select   = secondary.conference.rooms[0];
                secondary.conference.status   = 'Waiting for an active conference room...';
                secondary.conference.timers.start('confActive');
              })
              .catch(function (er) {
                ui.root.templates[2].show = false;
                secondary.conference.status   = 'Error!';
                console.log(er);
              });
          },

          changeRoom: function () {
            console.log(secondary.conference.select);
          },

          moveHere: function (item) {
            var entry     = angular.copy(item);
            var uniq      = _.find(secondary.conference.participants,
              function (part) {
                if (part.name === entry.name) {
                  return true;
                }
            });

            if (!_.isUndefined(uniq)) {
              return;
            }

            var confName  = angular.copy(secondary.conference.select.name);
            var intern;

            if (secondary.conference.active) {
              intern = entry.number;
              secondary.conference.participants.push(entry);
              restService.putConfInvite(confName, intern)
                .then(function (data) {
                  console.log(data);
                }, function (er) {
                  console.log(er);
                });
            } else {
              secondary.conference.participants.push(entry);
              return;
            }
          },

          remove: function (index) {
            secondary.conference.participants.splice(angular.copy(index), 1);

            return;
          },

          call: function () {
            var confName = angular.copy(secondary.conference.select.name);

            // if ((secondary.conference.pin !== null) && (secondary.conference.pin.length > 0)) {
            //   restService.getConfPin(confName, angular.copy(secondary.conference.pin))
            //     .then(function (data) {
            //       console.log(data);
            //     }, function (er) {
            //       console.log(er);
            //     })
            // };

            if (!secondary.conference.active) {
              secondary.conference.status  = 'Calling...';

              _.each(secondary.conference.participants, function (item) {
                restService.putConfInvite(confName, item.number || item.name)
                  .then(function () {

                  }, function (er) {
                    console.log(er);
                  });
              });
              secondary.conference.status       = 'Call in progress...';
              secondary.conference.btnDisabled  = true;
              secondary.conference.timers.start('confActive');
            } else {
              secondary.conference.status       = 'Kicking participants & hanging up...';
              secondary.conference.btnDisabled  = true;
              restService.getConfKick(confName, 'all')
                .then(function () {
                  secondary.conference.status       = 'Kicking participants & hanging up...';
                  secondary.conference.active       = false;
                  secondary.conference.btnDisabled  = false;
                  _.each(secondary.conference.participants, function (part) {
                    if (part.conf) {
                      part.conf.active = null;
                    }
                  });
                }, function (er) {
                  console.log(er);
                });
            }

            return;
          },

          mute: function (item) {
            var confName      = angular.copy(secondary.conference.select.name);
            var entry         = angular.copy(item);
            var getConfMute   = function (conf, id) {
              restService.getConfMute(conf, id)
                .then(function (data) {
                  console.log(data);
                }, function (er) {
                  console.log(er);
                });
            };
            var getConfUnmute = function (conf, id) {
              restService.getConfUnmute(conf, id)
                .then(function (data) {
                  console.log(data);
                }, function (er) {
                  console.log(er);
                });
            };

            if (entry.conf.canSpeak) {
              getConfMute(confName, entry.conf.id);
            } else {
              getConfUnmute(confName, entry.conf.id);
            }

          },

          deaf: function (item) {
            var confName      = angular.copy(secondary.conference.select.name);
            var entry         = angular.copy(item);
            var getConfDeaf   = function (conf, id) {
              restService.getConfDeaf(conf, id)
                .then(function (data) {
                  console.log(data);
                }, function (er) {
                  console.log(er);
                });
            };
            var getConfUndeaf = function (conf, id) {
              restService.getConfUndeaf(conf, id)
                .then(function (data) {
                  console.log(data);
                }, function (er) {
                  console.log(er);
                });
            };

            if (entry.conf.canHear) {
              getConfDeaf(confName, entry.conf.id);
            } else {
              getConfUndeaf(confName, entry.conf.id);
            }

          },

          keyPress: function (e) {
            if ( e.keyCode === 46 || e.keyCode === 8 ) {
            } else {
              if ((e.keyCode < 48 || e.keyCode > 57 ) && (secondary.conference.pin.length > 6)) {
                e.preventDefault();
              }
            }
          },

          kick: function (item) {
            var confName  = angular.copy(secondary.conference.select.name);
            var entry     = angular.copy(item);

            restService.getConfKick(confName, entry.conf.id)
              .then(function () {
                item.conf.active = null;
              }, function (er) {
                console.log(er);
              });
          },

          timers: {

            all: {},

            maxTimeouts: 20,
            currentTimeouts: 0,

            start: function (type) {
              var conf;
              var exists;
              var found;
              var interval;
              var backup = {};

              switch (type) {

                case 'confActive':
                  secondary.conference.refresh = null;
                  if (!timers.all.confActive) {
                    timers.all.confActive = [];
                  }

                  interval = $interval(function() {
                    restService.getConfPartList(secondary.conference.select.name)
                      .then(function (data) {

                        if (!_.isEmpty(backup)) {
                          _.each(_.filter(backup, function(obj){ return !_.findWhere(data.conference.members, obj); }), function (obj) {
                            _.find(secondary.conference.participants, function (part) {
                              if ((part.profile && part.profile.vCard && obj.name.toString() === part.profile.vCard['X-INTERN']) || (part.name === obj.imdId || part.imId === obj.name || part.imId === obj.imId)) {
                                part.conf.active = null;
                                return true;
                              }
                            })
                          })
                        }

                        backup = data.conference.members;

                        if (data !== null) {
                          secondary.conference.status  = 'Call in progress...';

                          _.each(data.conference.members, function (member) {
                            conf = {
                              'id':          member.id,
                              'canHear':     member.canHear,
                              'canSpeak':    member.canSpeak,
                              'muteDetect':  member.muteDetect,
                              'active':      true
                            };

                            exists = _.find(secondary.conference.participants, function (part) {
                              if ((part.number && part.number.toString() === member.name.toString()) || (part.name && part.name.toString() === member.name.toString()) || (part.name && part.name.toString() === member.imId.toString())){
                                part.conf = conf;
                                return true;
                              } else {
                                part.conf.active = null;
                              }
                            });

                            if (!exists) {
                              found = _.find(restService.phonebook, function (item) {
                                if (member.name.toString() === item.number) {
                                  _.extend(item, { conf: conf });
                                  secondary.conference.participants.push(item);
                                  return true;
                                }
                              });

                              if (!found) {
                                secondary.conference.participants.push({
                                  name: member.imId || member.name,
                                  conf: conf
                                });
                              }
                            }

                          });
                          secondary.conference.active       = true;
                          secondary.conference.btnDisabled  = false;
                        } else {
                          secondary.conference.status       = 'Waiting for an active conference room...';
                          secondary.conference.active       = false;
                          secondary.conference.btnDisabled  = false;
                          _.each(secondary.conference.participants, function (part) {
                            if (part.conf) {
                              part.conf.active = null;
                            }
                          });
                          secondary.conference.timers.cancelAll();
                        }
                      })
                      .catch(function () {
                        // secondary.conference.timers.cancelAll(true);
                        // secondary.conference.btnDisabled  = false;
                        // secondary.conference.status       = 'Error ' + status;

                        secondary.conference.status       = 'Waiting for an active conference room...';
                        secondary.conference.active       = false;
                        secondary.conference.btnDisabled  = false;
                        _.each(secondary.conference.participants, function (part) {
                          if (part.conf) {
                            part.conf.active = null;
                          }
                        });
                        secondary.conference.timers.cancelAll();
                      });
                  }, 10000);

                  timers.all.confActive.push(interval);
                  break;

              }

            },

            cancelAll: function (force) {
              var stopTimers = function () {
                var keys = Object.keys(timers.all);

                _.each(keys, function (key) {
                  if (key.indexOf('conf') !== -1 ) {
                    _.each(timers.all[key], function (timer) {
                      $interval.cancel(timer);
                    });
                  }
                });

                secondary.conference.refresh                  = true;
                secondary.conference.timers.currentTimeouts   = 0;
                secondary.conference.status = 'No active conference rooms found. ';

              };

              if (force) {

                stopTimers();
                return;
              }

              if (secondary.conference.timers.currentTimeouts < secondary.conference.timers.maxTimeouts) {
                secondary.conference.timers.currentTimeouts++;
                return;
              } else {
                stopTimers();
                return;
              }

            }

          }

        },

        settings: {

          personalAttendant: {

            main:     null,
            showNew:  false,
            numbers:  ['1', '2', '3', '4', '5', '6', '7', '8', '9'],
            emptyArr: [],
            lang:     ['Default'],

            init:     function () {
              secondary.settings.errors.notAvailable = null;
              restService.getPersonalAttendant().then(function (data) {
                if (data.language === null) {
                  data.language = 'Default';
                }
                secondary.settings.personalAttendant.lang = ['Default'];
                secondary.settings.personalAttendant.main = data;
                secondary.settings.success = false;
                secondary.settings.warning = false;
                var language = data.language;

                restService.getPersonalAttendantLang().then(function (data) {
                  secondary.settings.personalAttendant.lang = secondary.settings.personalAttendant.lang.concat(data);
                  secondary.settings.personalAttendant.main.language = language;
                })
              })
              .catch(function () {
                secondary.settings.errors.notAvailable = true;
              });
            },

            rem: function (d, i, key) {
              if (d.length) {
                d.splice(i, 1);
              } else {
                delete d[key]
              }
            },

            add: function () {
              var obj = {
                no: '',
                key: ''
              }
              secondary.settings.personalAttendant.showNew = true;
              secondary.settings.personalAttendant.emptyArr.push(obj);
            },

            save: function (formPa) {
              var main = angular.copy(secondary.settings.personalAttendant.main);
              var arr = angular.copy(secondary.settings.personalAttendant.emptyArr);

              //if (main.language === 'Default') {
              //  main.language = null;
              //}

              secondary.settings.personalAttendant.showNew = false;

              if (arr.length > 0) {
                _.each(arr, function (obj) {
                  main.menu[obj.key] = obj.no;
                })
              }

              secondary.settings.personalAttendant.emptyArr  = [];
              secondary.settings.loading                     = true;
              restService.putPersonalAttendant(angular.copy(main)).then(function () {
                secondary.settings.success   = true;
                secondary.settings.loading   = false;
                formPa.$setPristine();
                secondary.settings.personalAttendant.init();
              }).catch(function (err) {
                secondary.settings.success   = false;
                secondary.settings.warning   = err.toString();
                secondary.settings.loading   = false;
              });
            },

            cancel: function () {
              secondary.settings.personalAttendant.main       = null;
              secondary.settings.personalAttendant.emptyArr   = [];
              secondary.settings.personalAttendant.init();

            }

          },

          fwd: {

            visible: 'setup',

            show: function (name) {
              secondary.settings.fwd.visible = name;
              switch (name) {
                case 'setup':
                  secondary.settings.fwd.setup.init();
                  break;
                case 'schedule':
                  secondary.settings.fwd.sched.init();
                  break;
              }
              return;
            },

            setup: {

              resp:             null,
              selected:         null,
              showEmpty:        false,
              custom:           [],
              defaultSchedules: [null],
              defaultTypes: [
                'At the same time',
                'If no response',
              ],
              default: {
                'expiration': '',
                'type': 'At the same time',
                'enabled': '',
                'number': ''
              },

              init: function () {
                secondary.settings.fwd.setup.resp               = null;
                secondary.settings.success                      = false;
                secondary.settings.warning                      = false;
                secondary.settings.fwd.setup.showEmpty          = false;
                secondary.settings.errors.notAvailable          = null;
                secondary.settings.fwd.setup.defaultSchedules   = [null];
                secondary.settings.fwd.visible                  = 'setup';
                restService.getForward().then(function (data) {
                  secondary.settings.fwd.setup.resp = data;
                  return restService.getSchedule();
                }).then(function (data) {
                  secondary.settings.fwd.sched.main = data;
                  _.each(data, function (obj) {
                    secondary.settings.fwd.setup.defaultSchedules.push(obj.scheduleId);
                    secondary.settings.fwd.setup.defaultSchedules = _.uniq(secondary.settings.fwd.setup.defaultSchedules);
                  });
                }).catch(function () {
                  secondary.settings.errors.notAvailable = true;
                });
              },

              add: function () {
                secondary.settings.fwd.setup.showEmpty = true;
                secondary.settings.fwd.setup.resp.rings.push(angular.copy(secondary.settings.fwd.setup.default));
              },

              rem: function (d, i) {
                d.splice(i, 1);
              },

              moveRing: function (from, to) {
                if (to === -1) {
                  return;
                }

                arrayMove(secondary.settings.fwd.setup.resp.rings, from, to);

                function arrayMove(arrayVar, from, to) {
                  arrayVar.splice(to, 0, arrayVar.splice(from, 1)[0]);
                }
              },

              save: function (formSetup) {
                secondary.settings.success = false;
                secondary.settings.warning = false;
                secondary.settings.loading = true;
                restService.putForward(angular.copy(secondary.settings.fwd.setup.resp)).then(function (data) {
                  secondary.settings.loading = false;
                  secondary.settings.success = true;
                  formSetup.$setPristine();
                }, function (err) {
                  secondary.settings.loading = false;
                  secondary.settings.warning = true;
                })
              },

              cancel: function () {
                secondary.settings.fwd.setup.custom      = [];
                secondary.settings.fwd.setup.showEmpty   = false;
                secondary.settings.fwd.setup.init();
              }

            },

            sched: {

              backup:       null,
              showNew:      false,
              remArray:     [],
              main:         null,
              selected:     null,
              predefined:   [-2,-1,0,1,2,3,4,5,6,7],
              showNewEmpty: {
                'name':'',
                'description':'',
                'periods':[
                  {
                    'end': '',
                    'start': '',
                    'scheduledDay':''
                  }
                ],
                'scheduleId':''
              },

              init: function () {
                secondary.settings.errors.notAvailable = null;
                restService.getSchedule().then(function (data) {
                  secondary.settings.fwd.sched.convertTimeTo(data, 'normal');
                  secondary.settings.fwd.sched.backup    = angular.copy(data);
                  secondary.settings.fwd.sched.main      = data;
                  secondary.settings.fwd.sched.selected  = secondary.settings.fwd.sched.main[0];
                }).catch(function () {
                  secondary.settings.errors.notAvailable = true;
                });
              },

              add: function (to) {
                to.periods.push({
                  'end':'',
                  'start':'',
                  'scheduledDay':-2
                });
                return;
              },

              rem: function (to, index) {
                to.periods.splice(index, 1);
                return;
              },

              remSelected: function () {
                var selected = angular.copy(secondary.settings.fwd.sched.selected);
                secondary.settings.fwd.sched.remArray.push(selected.scheduleId);
                secondary.settings.fwd.sched.main = _.filter(angular.copy(secondary.settings.fwd.sched.main), function (obj) {
                  return (obj.name !== selected.name);
                })
                secondary.settings.fwd.sched.selected = secondary.settings.fwd.sched.main[0];
              },

              addNew: function () {
                var schedule = {};

                if (secondary.settings.fwd.sched.showNew) {
                  schedule = angular.copy(secondary.settings.fwd.sched.showNewEmpty);
                  secondary.settings.fwd.sched.main.push(schedule);
                  secondary.settings.fwd.sched.showNew = false;
                  secondary.settings.fwd.sched.convertTimeTo(schedule, 'sep');
                  postSchedule(schedule);
                } else {
                  secondary.settings.fwd.sched.showNew = true;
                }

                function postSchedule(schedule) {
                  restService.postSchedule(schedule).then(function (data) {
                    return restService.getSchedule();
                  }).then(function (data) {
                    _.find(data, function (o) {
                      if (schedule.name === o.name) {
                        schedule.scheduleId = o.scheduleId;
                        return true;
                      }
                    })
                    secondary.settings.fwd.sched.convertTimeTo(data, 'normal');
                    secondary.settings.fwd.sched.backup    = angular.copy(data);
                    secondary.settings.fwd.sched.main      = data;
                    secondary.settings.fwd.sched.selected  = secondary.settings.fwd.sched.main[0];
                  }).catch(function (err) {
                    console.log(err);
                  });
                }
              },

              hideNewEmpty: function () {
                secondary.settings.fwd.sched.showNew       = false;
                secondary.settings.fwd.sched.remArray      = [];
                secondary.settings.fwd.sched.showNewEmpty  = {
                  'name':'',
                  'description':'',
                  'periods':[
                    {
                      'end':'',
                      'start':'',
                      'scheduledDay':''
                    }
                  ]
                }
              },

              convertTimeTo: function (mainArr, type) {
                switch (type) {
                  case 'normal':

                    if (typeof mainArr === 'object' && mainArr.periods) {
                      _.each(mainArr.periods, function (time) {
                        time.end    = time.end.hrs + ':' + (time.end.min > -1 && time.end.min < 10 ? '0' + time.end.min : time.end.min);
                        time.start  = time.start.hrs + ':' + (time.start.min > -1 && time.start.min < 10 ? '0' + time.start.min : time.start.min);
                      })
                    } else {
                      _.each(mainArr, function (obj) {
                        _.each(obj.periods, function (time) {
                          time.end    = time.end.hrs + ':' + (time.end.min > -1 && time.end.min < 10 ? '0' + time.end.min : time.end.min);
                          time.start  = time.start.hrs + ':' + (time.start.min > -1 && time.start.min < 10 ? '0' + time.start.min : time.start.min);
                        })
                      });
                    }

                    return mainArr;
                  case 'sep':
                    if (typeof mainArr === 'object' && mainArr.periods) {
                      _.each(mainArr.periods, function (time) {
                        time.end = {
                          hrs: time.end.split(':')[0],
                          min: time.end.split(':')[1]
                        };
                        time.start = {
                          hrs: time.start.split(':')[0],
                          min: time.start.split(':')[1]
                        };
                      })
                    } else {
                      _.each(mainArr, function (obj) {
                        _.each(obj.periods, function (time) {
                          time.end = {
                            hrs: time.end.split(':')[0],
                            min: time.end.split(':')[1]
                          };
                          time.start = {
                            hrs: time.start.split(':')[0],
                            min: time.start.split(':')[1]
                          };
                        })
                      });
                    }

                    return mainArr;
                }
              },

              save: function (formSched) {
                var main    = angular.copy(secondary.settings.fwd.sched.main);
                var rem     = angular.copy(secondary.settings.fwd.sched.remArray);

                secondary.settings.fwd.sched.convertTimeTo(main, 'sep');

                secondary.settings.success = false;
                secondary.settings.warning = false;
                secondary.settings.loading = true;

                if (main.length > 0) {
                  _.each(main, function (obj) {
                    if (obj.scheduleId !== '') {
                      restService.putSchedule(obj.scheduleId, obj).then(function (data) {
                        secondary.settings.loading = false;
                        secondary.settings.success = true;
                      }, function (err) {
                        secondary.settings.loading = false;
                        secondary.settings.warning = true;
                        console.log(err);
                      })
                    } else {
                      restService.postSchedule(obj).then(function (data) {
                        secondary.settings.loading = false;
                        secondary.settings.success = true;
                        formSched.$setPristine();
                        return restService.getSchedule();
                      }).then(function (data) {
                        _.find(data, function (o) {
                          if (obj.name === o.name) {
                            obj.scheduleId = o.scheduleId;
                            return true;
                          }
                        })
                      }).catch(function (err) {
                        secondary.settings.loading = false;
                        secondary.settings.warning = true;
                        console.log(err);
                      });
                    }
                  });
                }

                if (rem.length > 0) {
                  _.each(rem, function (sched) {
                    restService.delSchedule(sched).then(function (data) {
                      secondary.settings.loading = false;
                      secondary.settings.success = true;
                      formSched.$setPristine();
                    }, function (err) {
                      secondary.settings.loading = false;
                      secondary.settings.warning = true;
                    })
                  })
                }
              },

              cancel: function () {
                secondary.settings.fwd.sched.showNew  = false;
                secondary.settings.fwd.sched.main     = null;
                secondary.settings.fwd.sched.init();
              }

            }
          },

          speed: {

            data: null,
            init: function () {
              secondary.settings.errors.speedDials = null;
              restService.getSpeeddial().then(function (data) {
                  for( var i = 0; i < data.buttons.length; i++)
                    data.buttons[i].pattern = /(^\d+|^([*][0-9]+)|^([+][0-9]+)|(^(\w+)@([a-zA-Z0-9_.]+)))$/;
                  secondary.settings.speed.data = data;
                }).catch(function () {
                  secondary.settings.errors.notAvailable = true;
                })
            },

            removeEntry: function (index) {
              secondary.settings.speed.data.buttons.splice(index, 1);
              return;
            },

            addEntry: function () {
              var obj = {
                number: '',
                label: '',
                pattern: /(^\d+|^([*][0-9]+)|^([+][0-9]+)|(^(\w+)@([a-zA-Z0-9_.]+)))$/,
                blf: false
              }
              secondary.settings.speed.data.buttons.push(obj);
              return;
            },

            move: function (from, to) {
              if (to === -1) {
                return;
              }
              arrayMove(secondary.settings.speed.data.buttons, from, to)
              function arrayMove(arrayVar, from, to) {
                arrayVar.splice(to, 0, arrayVar.splice(from, 1)[0]);
              }
            },

            save: function (formSpeed, updatePhones) {
              var data = angular.copy(secondary.settings.speed.data);
              if (updatePhones) {
                data.updatePhones = true;
              }
              secondary.settings.loading = true;
              secondary.settings.success = false;
              secondary.settings.warning = false;
              restService.putSpeeddial(data).then(function (data) {
                secondary.settings.success = true;
                formSpeed.$setPristine();
                restService.getSpeeddial().then(function (data) {
                  secondary.settings.loading = null;
                  secondary.settings.errors.speedDials = null;
                  for( var i = 0; i < data.buttons.length; i++)
                    data.buttons[i].pattern = /(^\d+|^([*][0-9]+)|^([+][0-9]+)|(^(\w+)@([a-zA-Z0-9_.]+)))$/;
                  secondary.settings.speed.data = data;
                }, function (err) {
                  secondary.settings.loading = null;
                  console.log(err);
                })
              }, function (err) {
                secondary.settings.warning = true;
                secondary.settings.loading = null;
                console.log(err);
                secondary.settings.errors.speedDials = true;
              })
            },

            cancel: function () {
              secondary.settings.speed.init();
            }

          },

          user: {
            pass:     '',
            loading:  null,
            buddy:    null,
            success:  false,
            warning:  false,
            vm: {
              main: null,
              selected: null,
              pass: null,
              select: [
                {
                  name: 'Default system',
                  val: 'NONE'
                },
                {
                  name: 'Standard',
                  val: 'STANDARD'
                },
                {
                  name: 'Out of office',
                  val: 'OUT_OF_OFFICE'
                },
                {
                  name: 'Extended absence',
                  val: 'EXTENDED_ABSENCE'
                }
              ]
            },

          moh: {
            audioData: null,
            addMoh: function (name) {
              var size = secondary.settings.user.moh.selectMoh.length;
              //name.upload();
              secondary.settings.user.moh.selectMoh[size] = { name: name.file.name, value:name.file.name };
            },
            deleteMoh: function(){
              restService.deleteMohFile(secondary.settings.user.moh.selectedMoh.name).then(function () {
                var index = 0;
                for(var i=0; i < secondary.settings.user.moh.selectMoh.length; i++ ){
                  if(secondary.settings.user.moh.selectMoh[i].name === secondary.settings.user.moh.selectedMoh.name)
                    index = i;
                }
                if(index > 0)
                  secondary.settings.user.moh.selectMoh.splice(index, 1);
                secondary.settings.user.moh.selectedMoh = secondary.settings.user.moh.selectMoh[0];
              }).catch(function (err) {
                console.log("error delete MoH file error");
              });
            },
            listenMoh: function(){
              restService.listenMohFile(secondary.settings.user.moh.selectedMoh.name).then(function (data) {
                window.open('/sipxconfig/api/my/moh/prompts/'+secondary.settings.user.moh.selectedMoh.name+'/stream', '_blank');
              }).catch(function (err) {
                console.log("error listen MoH file error");
              });
            },
            selected: null,
            select: [
              {
                name: 'Group Music Directory',
                val: 'GROUP_FILES_SRC'
              },
              {
                name: 'Personal Music Directory',
                val: 'PERSONAL_FILES_SRC'
              },
              {
                name: 'Use System Configuration',
                val: 'SYSTEM_DEFAULT'
              },
              {
                name: 'System Music Directory',
                val: 'FILES_SRC'
              },
              {
                name: 'None',
                val: 'NONE'
              },
              {
                name: 'Sound Card',
                val: 'SOUNDCARD_SRC'
              }
            ],
            selectedMoh: null,
            selectMoh: [
              {
                name: 'Select...',
                val: 'NONE'
              }
            ]
          },

          conf: {
            main: null,
            changeConf: function () {
              var name = angular.copy(secondary.settings.user.conf.selected.name);
              restService.getConfSettings(name).then(function (data) {
                secondary.settings.user.conf.main = data.setting;
              }).catch(function (err) {
                secondary.settings.errors.myBuddy = true;
              });
            }
          },

          init: function () {
            secondary.settings.errors.notAvailable   = null;
            secondary.settings.errors.myBuddy        = null;
            secondary.settings.errors.voicemail      = null;
            secondary.settings.errors.confBridge     = null;
            secondary.settings.errors.moh            = null;

            restService.getImBot().then(function (data) {
              secondary.settings.user.buddy = data;
            }).catch(function (err) {
              secondary.settings.errors.myBuddy = true;
            });

            restService.getVmPrefs().then(function (data) {
              secondary.settings.user.vm.main = data;
              setVmSelected();
            }).catch(function (err) {
              secondary.settings.errors.voicemail = true;
            });

              restService.getConfList().then(function (data) {
                if (data.conferences && data.conferences.length === 0) {
                  secondary.settings.errors.confBridge = true;
                  return;
                }
                secondary.settings.user.conf.select = data.conferences;
                secondary.settings.user.conf.selected = secondary.settings.user.conf.select[0];
                secondary.settings.user.conf.changeConf();
              }).catch(function (err) {
                secondary.settings.errors.confBridge = true;
              });

            restService.getMohPermission().then(function (data) {
              if( data == "false"){
                secondary.settings.errors.moh = true;
              }
              else{
                secondary.settings.errors.moh = false;
              }
            }).catch(function (err) {
              secondary.settings.errors.moh = true;
            });

            restService.getMohSettings().then(function (data) {
              setMohSettingsSelected(data);
            }).catch(function (err) {
                console.log(err);
            });

            restService.getMohFiles().then(function (data) {
              setMohFilesSelect(data);
            }).catch(function (err) {
                console.log(err);
            });


            function setVmSelected () {
              var i = 0;
              var greeting = angular.copy(secondary.settings.user.vm.main.greeting);
              for (i = 0; i < secondary.settings.user.vm.select.length; i++) {
                if (greeting === secondary.settings.user.vm.select[i].val) {
                  secondary.settings.user.vm.selected = secondary.settings.user.vm.select[i];
                }
              };
            }

            function setMohSettingsSelected (data) {
              var i = 0;
              for (i = 0; i < secondary.settings.user.moh.select.length; i++) {
                if (data.value === secondary.settings.user.moh.select[i].val) {
                  secondary.settings.user.moh.selected = secondary.settings.user.moh.select[i];
                }
              }
            }

            function setMohFilesSelect (data) {
              var i = 0;
              for (i = 0; i < data.files.length; i++) {
                //var length = secondary.settings.user.moh.selectMoh.length;
                secondary.settings.user.moh.selectMoh[i+1] = { name: data.files[i].name, val: data.files[i].name};
              }
              secondary.settings.user.moh.selectedMoh = secondary.settings.user.moh.selectMoh[0];
            }
          },

          save: function (formUserSettings) {

            savePassword();
            saveImBot();
            saveVm();
            saveVmPass();
            if(secondary.settings.errors.moh != true)
              saveMoh();
            if(secondary.settings.errors.confBridge != true)
              saveConf();

              function savePassword() {
                var pass = angular.copy(secondary.settings.user.pass);
                if (pass.length === 0) {
                  return;
                }
                if (pass.length < 8) {
                  secondary.settings.user.errors.pass = true;
                  return
                }
                secondary.settings.user.loading = true;
                restService.putPassword(pass).then(function (data) {
                  secondary.settings.user.loading = null;
                }, function (err) {
                  console.log(err);
                })
              }

              function saveImBot() {
                var buddy = angular.copy(secondary.settings.user.buddy);
                secondary.settings.user.loading = true;
                restService.putImBot(buddy).then(function (data) {
                  restService.getImBot().then(function (data) {
                    secondary.settings.user.loading = null;
                    secondary.settings.user.buddy = data;
                  }, function (err) {
                    secondary.settings.user.loading = null;
                    console.log(err);
                  })
                }, function (err) {
                  secondary.settings.user.loading = null;
                  console.log(err);
                })
              }

              function saveVm() {
                if (secondary.settings.user.vm.main.email) {
                  secondary.settings.user.vm.main.emailAttachType      = 'YES';
                  secondary.settings.user.vm.main.emailFormat          = 'FULL';
                }
                if (secondary.settings.user.vm.main.altEmail) {
                  secondary.settings.user.vm.main.altEmailAttachType   = 'YES';
                  secondary.settings.user.vm.main.altEmailFormat       = 'FULL';
                }
                secondary.settings.user.vm.main.greeting = secondary.settings.user.vm.selected.val;

                restService.putVmPrefs(angular.copy(secondary.settings.user.vm.main)).catch(function (err) {
                  console.log(err);
                })
              }

            function saveMoh() {
              restService.putMohSettings(angular.copy(secondary.settings.user.moh.selected.val)).catch(function (err) {
                console.log(err);
              })
            }

              function saveConf() {
                var data = angular.copy(secondary.settings.user.conf.main);
                var conf = angular.copy(secondary.settings.user.conf.selected.name);

                restService.putConfSettings(conf, {setting: data}).catch(function (err) {
                  console.log(err);
                })
              }

              function saveVmPass() {
                if (secondary.settings.user.vm.pass === null) {
                  return
                } else {
                  var pass = angular.copy(secondary.settings.user.vm.pass);
                  restService.putVmPin(pass).catch(function (err) {
                    console.log(err);
                  })
                }
              }
              formUserSettings.$setPristine();
            },

            cancel: function () {
              secondary.settings.user.init();
            },

            errors: {
              pass: null
            }
          },

          errors: {
            notAvailable: false,
            voicemail:    false,
            confBridge:   false,
            myBuddy:      false,
            speedDials:   false
          }
        },

        profile: {
          imageData: null,
          info: null,
          util: {
            success: null,
            failure: null,
            loading: null
          },
          init: function () {
            secondary.profile.uploadAvatar = false;
            secondary.profile.info = null;
            secondary.profile.util.success = null;
            secondary.profile.util.failure = null;
            secondary.profile.util.loading = null;
            restService.getContactInfo().then(function (data) {
              secondary.profile.info          = angular.copy(secondary.profile.default);
              secondary.profile.info.profile  = util.deepExtend(angular.copy(secondary.profile.info.profile), data['contact-information']);
            }, function (err) {
              console.log(err);
            });
          },

          save: function () {
            var info = {'contact-information': util.deepRemoveEmpty(angular.copy(secondary.profile.info.profile))};
            secondary.profile.util.loading = true;
            if(secondary.profile.imageData != null){
              secondary.profile.imageData.upload();
              secondary.profile.imageData = null;
            }
            restService.putContactInfo(info).then(function (data) {
              secondary.profile.util.loading = false;
              secondary.profile.util.success = true;
              secondary.profile.uploadAvatar = false;
              setTimeout(function() {
                secondary.profile.util.success = false;
              }, 1000);
            }, function (err) {
              secondary.profile.util.loading = false;
              secondary.profile.util.failure = true;
              console.log(err);
            });
          },

          default: {
            name:     null,
            location: null,
            photoSrc: null,
            uploadAvatar: false,
            profile:  {
              alternateEmailAddress: '',
              alternateImId: '',
              assistantName: '',
              assistantPhoneNumber: '',
              cellPhoneNumber: '',
              companyName: '',
              didNumber: '',
              emailAddress: '',
              facebookName: '',
              faxNumber: '',
              firstName: '',
              homeAddress: {
                street:'',
                city:'',
                state:'',
                country:'',
                zip:''
              },
              homePhoneNumber: '',
              imDisplayName: '',
              imId: '',
              jobDept: '',
              jobTitle: '',
              lastName: '',
              linkedinName: '',
              location: '',
              officeAddress: {
                street:'',
                city:'',
                state:'',
                country:'',
                zip:'',
                officeDesignation:''
              },
              twiterName: '',
              useBranchAddress: '',
              xingName: ''
            }
          }

        }

      };

      var util = {

        openChat : null,

        populateContactList : function () {
          rosterList.main     = restService.phonebook;
          activityList.main   = restService.activityList;
          // restService.getCDRlog(20).then(function (data) {
          // })
          restService.getAllImages(rosterList.main);

          // keep alive every 10 minutes
          $interval(function () {
            restService.getKeepAlive();
          }, 600000);
          return;
        },

        deepRemoveEmpty : function (source) {
          var destination = {};

          for (var property in source) {
            if (!_.isEmpty(source[property])) {
              if ((source.hasOwnProperty(property)) && (typeof source[property] === 'object')) {
                destination[property] = source[property] || {};
                util.deepRemoveEmpty(source[property]);
              } else {
                destination[property] = source[property];
              }
            }

          }

          return destination;
        },

        deepExtend : function (destination, source) {
          for (var property in source) {
            if ((source[property]) && (typeof source[property] === 'object')) {
              destination[property] = destination[property] || {};
              util.deepExtend(destination[property], source[property]);
            } else {
              destination[property] = source[property];
            }
          }

          return destination;
        },

        changeView : function (view) {

          if (view.type) {
            secondary.template.main             = view;
            secondary.voicemail.messages        = [];
            secondary.conference.participants   = [];
            secondary.conference.active         = false;
            secondary.conference.timers.cancelAll(true);
            if ((secondary[view.fn]) && (secondary[view.fn].init)) {
              secondary[view.fn].init();
            }
            $rootScope.leftSideView = 'hide-me';
          } else {
            groupChat.modal           = false;
            if (ui.root.oldTemplate.name !== view.name) {
              ui.root.oldTemplate = angular.copy(ui.root.template);
            }
            ui.root.template = view;
          }

          return;
        }
      };

      return {
        ui:           ui,
        secondary:    secondary,
        util:         util,
        rosterList:   rosterList,
        activityList: activityList,
        groupChat:    groupChat,
        search:       search
      };
    }
  ]);
})();


(function(){
'use strict';

uw.controller('loginController', [
  '$rootScope',
  '$scope',
  '$location',
  '$cookieStore',
  'restService',
  'uiService',
  'CONFIG',
  function ($rootScope, $scope, $location, $cookieStore, restService, uiService, CONFIG) {
    var authCookie    = CONFIG.authCookie;
    var auth          = $cookieStore.get(authCookie);
    $scope.error      = null;
    $scope.info       = null;
    $scope.username   = null;
    $scope.password   = null;
    $scope.remember   = false;

    if (($location.search()['unitexmppbind']) &&
        ($location.search()['unitexmppdomain']) &&
        ($location.search()['uniterestbase']) &&
        ($location.search()['uniteusername']) &&
        ($location.search()['unitepassword'])) {

      CONFIG.httpBindUrl  = $location.search()['unitexmppbind'];
      CONFIG.domain       = $location.search()['unitexmppdomain'];
      CONFIG.baseRest     = $location.search()['uniterestbase'];

      $scope.info         = 'Connecting...';
      $scope.error        = null;
      $scope.username     = $location.search()['uniteusername'];
      $scope.password     = $location.search()['unitepassword'];

      // chatService.connect(angular.copy($scope.username), angular.copy($scope.password));
    }


    $scope.submit = function () {
      var user = angular.copy($scope.username);
      var pass = angular.copy($scope.password);

      $scope.info   = 'Connecting...';
      $scope.error  = null;

      if ( (!_.isEmpty(user)) && (!_.isEmpty(pass)) ) {
        restService.updateCredentials(angular.copy($scope.username), angular.copy($scope.password));

        restService.getPhonebook().
          then(function (data) {
            if ($scope.remember) {
              var auth = Base64.encode(user + ':' + pass);
              $cookieStore.put(authCookie, auth);
            }

            restService.connected = true;
            restService.phonebook = data.phonebook;
            restService.phonebook.forEach(function (el, i) {
              restService.phonebook[i].name = (restService.phonebook[i]['contact-information'].imDisplayName) || (restService.phonebook[i]['first-name'] + ' ' + restService.phonebook[i]['last-name']);
            })


            uiService.util.populateContactList();

            // $scope.$apply(function () {
              $location.path('/main');
            // });
          }).
          catch(function (er) {
            $scope.info   = null;
            $scope.error  = 'Invalid credentials';
            console.log(er);
          })
      } else {
        $scope.info   = null;
        $scope.error  = 'Invalid credentials';
      }
    };

    function onCannotConnect() {
      $scope.$apply(function () {
        $scope.error  = 'Cannot connect';
        $scope.info   = null;
      });
    }

    function onInvalidCredentials() {
      $scope.$apply(function () {
        $scope.error  = 'Invalid credentials';
        $scope.info   = null;
      })
    }

    function onConnect() {
      if ($scope.remember) {
        var auth = Base64.encode($scope.username + ':' + $scope.password);
        $cookieStore.put(authCookie, auth);
      }

      // $scope.$apply(function () {
        $location.path('/main');
      // });
    }

    $scope.$on('services.chat.error', onCannotConnect);
    $scope.$on('services.chat.connfail', onCannotConnect);
    $scope.$on('services.chat.authfail', onInvalidCredentials);
    $scope.$on('services.chat.connected', onConnect);

    if (auth) {
      var arr = Base64.decode(auth).split(':');
      $scope.username = arr[0];
      $scope.password = arr[1];

      $scope.info = 'Connecting...';
      $scope.error = null;
      $scope.remember = true;// remember for another 7 days

      restService.updateCredentials(angular.copy($scope.username), angular.copy($scope.password));

      restService.getPhonebook().
        then(function (data) {
          if ($scope.remember) {
            var auth = Base64.encode(angular.copy($scope.username) + ':' + angular.copy($scope.password));
            $cookieStore.put(authCookie, auth);
          }

          restService.connected = true;
          restService.phonebook = data.phonebook;
          restService.phonebook.forEach(function (el, i) {
            restService.phonebook[i].name = (restService.phonebook[i]['contact-information'].imDisplayName) || (restService.phonebook[i]['first-name'] + ' ' + restService.phonebook[i]['last-name']);
          })

          uiService.util.populateContactList();

          $location.path('/main');
        }).
        catch(function () {
          $scope.info   = null;
          $scope.error  = 'Invalid credentials';
        })
      // chatService.connect(angular.copy($scope.username), angular.copy($scope.password));
    }
  }
]);
})();

(function(){
'use strict';

uw.controller('profile', [
  '$sce',
  '$rootScope',
  '$scope',
  '$interval',
  '$timeout',
  'uiService',
  'restService',
  'sharedFactory',
  function ($sce, $rootScope, $scope, $interval, $timeout, uiService, restService, sharedFactory) {

    var templates                 = sharedFactory.settings.miniTemplates;
    var profile                   = restService.cred;
    $scope.callhistory            = uiService.secondary.callhistory;
    var timeout;
    var sipMessages               = [
      '',
      {
        '100': 'Trying',
        '101': 'Dialogue Establishment',
        '180': 'Ringing',
        '181': 'Call is Being Forwarded',
        '182': 'Queued',
        '183': 'Session in Progress',
        '199': 'Early Dialog Terminated'
      },
      {
        '200': 'Answered',
        '202': 'Accepted',
        '204': 'No Notification'
      },
      {
        '300': 'Multiple Choices',
        '301': 'Moved Permanently',
        '302': 'Moved Temporarily',
        '305': 'Use Proxy',
        '380': 'Alternative Service'
      },
      {
        '400': 'Bad Request',
        '401': 'Unauthorized',
        '402': 'Payment Required',
        '403': 'Forbidden',
        '404': 'Number Not Found',
        '405': 'Method Not Allowed',
        '406': 'Not Acceptable',
        '407': 'Proxy Authentication Required',
        '408': 'Call Timeout',
        '409': 'Conflict',
        '410': 'Gone',
        '411': 'Length Required',
        '412': 'Conditional Request Failed',
        '413': 'Request Entity Too Large',
        '414': 'Request-URI Too Long',
        '415': 'Unsupported Media Type',
        '416': 'Unsupported URI Scheme',
        '417': 'Unknown Resource-Priority',
        '420': 'Bad Extension',
        '421': 'Extension Required',
        '422': 'Session Interval Too Small',
        '423': 'Interval Too Brief',
        '424': 'Bad Location Information',
        '428': 'Use Identity Header',
        '429': 'Provide Referrer Identity',
        '430': 'Flow Failed',
        '433': 'Anonymity Disallowed',
        '436': 'Bad Identity-Info',
        '437': 'Unsupported Certificate',
        '438': 'Invalid Identity Header',
        '439': 'First Hop Lacks Outbound Support',
        '470': 'Consent Needed',
        '480': 'Number Unavailable',
        '481': 'Call/Transaction Does Not Exist',
        '482': 'Loop Detected.',
        '483': 'Too Many Hops',
        '484': 'Address Incomplete',
        '485': 'Ambiguous response from server',
        '486': 'Number',
        '487': 'Request Terminated',
        '488': 'Not Acceptable Here',
        '489': 'Bad Event',
        '491': 'Request Pending',
        '493': 'Undecipherable',
        '494': 'Security Agreement Required'
      },
      {
        '500': 'Server Internal Error',
        '501': 'Not Implemented',
        '502': 'Bad Gateway',
        '503': 'Service Unavailable',
        '504': 'Server Time-out',
        '505': 'Version Not Supported',
        '513': 'Message Too Large',
        '580': 'Precondition Failure'
      },
      {
        '600': 'Busy Everywhere',
        '603': 'Decline',
        '604': 'Does Not Exist Anywhere',
        '606': 'Not Acceptable'
      }
    ]
    var interval;

    $scope.name                   = restService.cred;
    $scope.isUserProfileVisible   = false;
    $scope.template               = templates[0];

    $scope.ui             = uiService.ui.root;
    $scope.selectedItem   = uiService.ui.root.template;
    $scope.search         = uiService.search;
    $scope.callNo         = '';
    $scope.displayNo      = '';
    $scope.clicked        = false;
    $scope.showMainMenu   = false;
    $scope.showDial       = false;
    $scope.startNo        = false;
    $scope.showSearch     = false;
    $scope.sipEnabled     = false;
    $scope.searchResult   = [];

    uiService.secondary.conference.init();
    uiService.secondary.callhistory.init();

    $scope.callhistory.clickToCall = function(clicked) {
      $scope.showDialFn(true);
      $scope.displayNo = clicked;
    }

    $scope.showMainMenuFn = function () {
      $scope.showMainMenu   = !$scope.showMainMenu;
      $scope.showDial       = false;
      $scope.showSearch     = false;
      $scope.callNo         = '';
      $scope.phone.msg      = null;

      return
    }
    $scope.broadcastViewChange = function (item) {
      $scope.search.t           = '';
      $scope.showMainMenu       = false;
      $scope.showSearch         = false;
      $scope.selectedItem       = item;
      $scope.phone.msg          = null;

      uiService.util.changeView(item);

      return
    }

    $scope.logout = function () {
      //if(confirm("Are you sure you want to leave?")){
        window.location.assign('/sipxconfig/rest/my/logoutunite');
      /*}
      else{
        return;
      }*/
    }

    $scope.phone          = {
      call: null,
      active: null,
      msg: null,
      disabled: null
    }
    $scope.keyboard       = [
      {no: '1', text: ''},
      {no: '2', text: 'abc'},
      {no: '3', text: 'def'},
      {no: '4', text: 'ghi'},
      {no: '5', text: 'jkl'},
      {no: '6', text: 'mno'},
      {no: '7', text: 'pqrs'},
      {no: '8', text: 'tuv'},
      {no: '9', text: 'wxyz'},
      {no: '*', text: ''},
      {no: '0', text: ''},
      {no: '#', text: ''}
    ]
    $scope.showDialFn = function (force) {
      if (force) {
        $scope.showDial = true;
      } else {
        $scope.showDial = !$scope.showDial;
      }
      $scope.showMainMenu   = false;
      $scope.showSearch     = false;
      $scope.phone.msg      = null;

      if ($scope.showDial === false) {
        $interval.cancel(interval);
        $scope.phone.disabled = null;
      }

      return;
    }
    $scope.clearNo = function () {
      $scope.callNo = '';
      $scope.displayNo = '';

      return
    }
    $scope.addNo = function (no) {
      $scope.callNo = $scope.callNo + no;
      $scope.displayNo = $scope.displayNo + no;

      return;
    }
    $scope.phone.call = function () {
      var callNo              = angular.copy($scope.callNo)
        .replace('(', '')
        .replace(')', '')
        .replace('.', '')
        .replace(' ', '')
        .replace('-', '');
      $scope.phone.msg        = 'Requesting call...';
      $scope.phone.disabled   = true;

      if (callNo !== '') {
        restService.postBasicCall(callNo)
          .then(function () {
            $scope.phone.disabled   = true;
            $scope.phone.msg        = 'Requesting call info...';
            startInterval(callNo);
          })
          .catch(function (status) {
            $scope.phone.msg = 'Error ' + status;
          });
      }

      function startInterval(no) {
        var match;
        var search;
        interval = $interval(function () {
          restService.getBasicCall(no).then(function (data) {
            if (data.length === 0) {
              $interval.cancel(interval);
              return;
            }

            // SIP status messages according to wikipedia
            //
            for (var i = 1; i < 7; i++) {
              search = data[data.length-1].status.search(i+'[0-9][0-9]');
              if (search !== -1) {
                match = data[data.length-1].status.match(i+'[0-9][0-9]')[0];

                $scope.phone.msg = sipMessages[i][match];

                if (match === '180') {
                  timeout = $timeout(function () {
                    if (interval) {
                      $interval.cancel(interval);
                    }
                    $scope.phone.msg = null;
                  }, 10000);
                }

                if (match === '183' || match === '180') {
                  $scope.phone.disabled = null;
                  break;
                }

                if (match === '200' && interval) {
                  $interval.cancel(interval);
                  $timeout.cancel(timeout);
                  break;
                }

                if (match === '404' || match === '408' || match === '480' || match === '486') {
                  $scope.phone.disabled = null;
                  $timeout.cancel(timeout);
                  if (interval) {
                    $interval.cancel(interval);
                  }
                  break;
                }
                break;
              }
              continue;
            }

          }).catch(function (status) {
            // SIP status messages according to wikipedia
            //
            for (var i = 1; i < 7; i++) {
              search = data[data.length-1].status.search(i+'[0-9][0-9]');
              if (search !== -1) {
                match = data[data.length-1].status.match(i+'[0-9][0-9]')[0];

                $scope.phone.msg = 'Error: ' + sipMessages[i][match];

                $timeout.cancel(timeout);
                if (interval) {
                  $interval.cancel(interval);
                }
                break;
              }
              continue;
            }

          })
        }, 1000);
      }

      return
    }

    $scope.showSearchFn = function () {
      $scope.showSearch = !$scope.showSearch;
      $scope.showMainMenu   = false;
      $scope.showDial       = false;
      $scope.callNo         = '';
      $scope.phone.msg      = null;

      if ($scope.showSearch === false && uiService.ui.root.template === uiService.ui.root.templates[8]) {
        uiService.util.changeView(uiService.ui.root.oldTemplate);
        $scope.search.t = '';
      }
    }

    $scope.searchResultClick = function (item) {
      $scope.clicked    = true;
      $scope.displayNo = item.name;
      $scope.callNo = item.number;
    }

    $scope.$watchCollection('search', function (val) {
      if (val.t === '') {
        uiService.util.changeView(uiService.ui.root.oldTemplate);
      } else if (uiService.ui.root.template !== uiService.ui.root.templates[8]) {
        uiService.util.changeView(uiService.ui.root.templates[8]);
      };

      return
    })

    $scope.$watch('displayNo', function (val) {
      if (val === '') {
        $scope.startNo = false;
        $scope.callNo = val;
      } else {
        if ($scope.clicked) {
          $scope.searchResult   = [];
          $scope.clicked        = false;
          return;
        }
        $scope.callNo = val;
        var arr = [];
        val = val.toString().toLowerCase();
        $scope.startNo = true;
        arr = _.filter(uiService.rosterList.main, function (item) {
          if (item.name && item.number) {
            return (item.name.toLowerCase().indexOf(val) !== -1) || (item.number.toLowerCase().indexOf(val) !== -1)
          } else {
            return false;
          }
        });
        if (arr.length === 1 && arr[0].name === $scope.displayNo) {
          $scope.searchResult = [];
        } else {
          $scope.searchResult = arr;
        }
      }

      return
    })

    $rootScope.$on('controller.mainview.callDialpad', function (e, obj) {
      $scope.showDialFn(true);
      $scope.callNo = obj.number;
      $scope.displayNo = obj.number;
    })

  }
]);
})();

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
    $scope.callhistory        = uiService.secondary.callhistory;

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

(function(){
'use strict';

uw.controller('settingsController', [
  '$rootScope',
  '$scope',
  '$cookieStore',
  'restService',
  'uiService',
  'CONFIG',
  '_',
  function ($rootScope, $scope, $cookieStore, restService, uiService, CONFIG, _) {
    var authCookie    = $cookieStore.get('JSESSIONID');

    $scope.debug                  = (CONFIG.debug) ? 'off' : 'on';
    $scope.autoLogin              = (!_.isUndefined(authCookie)) ? 'off' : 'on';
    $scope.showResetButtonConn    = false;
    $scope.showResetButtonDebug   = false;
    $scope.toggleResetButton = function (str) {
      switch (str) {
        case 'conn':
          $scope.showResetButtonConn = !$scope.showResetButtonConn;
          break;
      }
    }
    $scope.settings   = [
      {
        icon: 'chat_to_call',
        name: 'Personal Attendant'
      },
      {
        icon: 'follow_me',
        name: 'Call Forwarding'
      },
      {
        icon: 'dialpad',
        name: 'Speed Dials'
      },
      {
        icon: 'settings_cogs',
        name: 'User Settings'
      }
    ];
    $scope.tooltips = {
      personalAttendant: {
        add: {
          'title': 'Add dialpad entry',
          'checked': false
        },
        rem: {
          'title': 'Remove entry',
          'checked': false
        }
      },
      fwd: {
        setup: {
          add: {
            'title': 'Add ring',
            'checked': false
          },
          rem: {
            'title': 'Remove ring',
            'checked': false
          }
        },
        sched: {
          add: {
            'title': 'Add time period',
            'checked': false
          },
          rem: {
            'title': 'Remove time period',
            'checked': false
          },
          remSelected: {
            'title': 'Discard schedule',
            'checked': false
          }
        }
      },
      speed: {
        add: {
          'title': 'Add speed dial',
          'checked': false
        },
        rem: {
          'title': 'Remove speed dial',
          'checked': false
        }
      }
    }
    $scope.selectOption = function () {
      _.each($scope.settings, function (o) {
        o.isSelected = false;
      });
      this.option.isSelected = true;
      $scope.selected = this.option.name;

      switch ($scope.selected) {
        case 'User Settings':
          $scope.userSettings.user.init();
          break;

        case 'Speed Dials':
          $scope.userSettings.speed.init();
          break;

        case 'Call Forwarding':
          $scope.userSettings.fwd.setup.init();
          break;

        case 'Personal Attendant':
          $scope.userSettings.personalAttendant.init();
          break;
      }
    };
    $scope.reloadApp      = uiService.secondary.logout.init;
    $scope.userSettings   = uiService.secondary.settings;
  }
]);
})();

(function(){
'use strict';

uw.controller('UploadFile', [
  '$rootScope',
  '$scope',
  'restService',
  'uiService',
  'CONFIG',
  'FileUploader',
  function ($rootScope, $scope, restService, uiService, CONFIG, FileUploader) {
    $scope.myprofile          = uiService.secondary.profile;

    var uploader = $scope.uploader = new FileUploader({
        url: CONFIG.baseRest + '/avatar/' + restService.cred.user
    });

    // FILTERS

    uploader.filters.push({
        name: 'customFilter',
        fn: function(item /*{File|FileLikeObject}*/, options) {
          var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
          return '|jpg|png|jpeg|bmp|gif|'.indexOf(type) !== -1;
            //return this.queue.length < 10;
        }
    });

    // CALLBACKS

    uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
        console.info('onWhenAddingFileFailed', item, filter, options);
    };
    uploader.onAfterAddingFile = function(fileItem) {
        console.info('onAfterAddingFile', fileItem);
        $scope.myprofile.imageData = fileItem;
        $scope.myprofile.uploadAvatar = true;
        //fileItem.upload();
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
    };
    uploader.onCompleteAll = function() {
        console.info('onCompleteAll');
        $scope.myprofile.info.profile.avatar = $scope.myprofile.info.profile.avatar+ '?' + new Date().getTime();
        $scope.myprofile.uploadAvatar = false;
    };
  }]);
})();

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

(function() {

  'use strict';

  uw.directive('resize', [
    '$window',
    function ($window) {
      return {
        scope: false,
        link: function (scope, element, attrs) {
          var heightDifference  = (attrs.resize === 'full') ? 42 : 75;
          var $                 = angular.element;
          var w                 = $(window);
          var height;

          scope.getWindowDimensions = function () {
            return { h: $window.innerHeight, w: $window.innerWidth };
          };

          scope.$watch(scope.getWindowDimensions, function (newValue, oldValue) {

            setTimeout(resize, 0);

            scope.$on('$viewContentLoaded', function(){
              setTimeout(resize, 0);
            });

            function resize() {
              height = newValue.h - heightDifference;
              $(element).css('max-height', height + 'px');
              return
            }
          }, true);

          w.bind('resize',function(){
            scope.$apply();
          });

          return true;
        }
      }
    }
  ]);

})();

(function() {

  'use strict';

  uw.directive('tree', [
    '$compile',
    '_',
    function ($compile, _) {
      return {
        restrict: 'A',
        scope: {
          treeKey : '=',
          treeVal : '=',
          treeParent : '='
        },
        compile: function (el, attrs) {

          var template        = '<label for="{{ treeKey }}" class="col-sm-4 control-label">{{ treeKey | translate }}</label>';
          var spanParent      = template +
          '<div class="col-sm-8">' +
            '<span data-ng-bind-html="treeVal | linky"></span>' +
          '</div><hr>';
          var spanChildren    = template +
          '<div class="form-group-child col-sm-12 clearfix"' +
            'data-ng-repeat="(k, v) in treeVal track by $index">' +
            '<div data-tree data-tree-key="k" data-tree-val="v"></div>' +
          '</div>';
          var inputParent     = template +
          '<div class="col-sm-8">' +
            '<input type="text" class="form-control" data-ng-model="treeParent[treeKey]">' +
          '</div><hr>';
          var selectParent    = template +
          '<div class="col-sm-8">' +
            '<select class="form-control" data-ng-model="treeParent[treeKey]" data-ng-change="profileForm.$setDirty();">' +
              '<option data-ng-selected="treeParent[treeKey] === true">True</option>' +
              '<option data-ng-selected="treeParent[treeKey] === false">False</option>' +
            '</select>' +
          '</div><hr>';
          var inputChildren   = template + '<div class="form-group-child col-sm-12 clearfix"' +
            'data-ng-repeat="(k, v) in treeVal track by $index">' +
            '<div data-tree data-tree-key="k" data-tree-val="v" data-tree-input data-tree-parent="treeParent[treeKey]"></div>' +
          '</div>';

          var children;
          var parent;

          if (attrs.treeInput === '') {
            parent    = inputParent;
            children  = inputChildren;
          } else {
            parent    = spanParent;
            children  = spanChildren;
          }

          return function postLink (scope, element, attrs) {

            switch (scope.treeKey) {
              case 'avatar':
              case 'timestamp':
              case 'imDisplayName':
              case 'enabled':
              case 'ldapManaged':
              case 'branchName':
              case 'branchAddress':
              case 'officeAddress':
              case 'useBranchAddress':
              case 'salutation':
              case 'employeeId':
              case 'emailAddressAliasesSet':
                return;
              default:
                break;
            }

            if (angular.isObject(scope.treeVal)) {
              if (!_.isEmpty(scope.treeVal)) {
                element.append(children);
                $compile(element.contents())(scope);
                return;
              } else {
                return;
              }
            } else {
              if (scope.treeVal === false || scope.treeVal === true) {
                element.append(selectParent);
              } else {
                element.append(parent);
              }
              $compile(element.contents())(scope);
              return;
            }

          };
        }
      }
    }
  ]);

})();

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

(function(){
'use strict';

uw.directive('maxHeight', [
  'uiService',
  function (uiService) {
    return {
      compile: function () {

        return function link (scope, elem) {
          scope.$watch(
            function() {
              return uiService.groupChat.modal;
            },
            function(newValue, oldValue) {
              if (newValue) {
                elem[0].scrollTop = 0;
                angular.element(elem).css({'height': '100%', 'overflow': 'hidden'})
              } else {
                angular.element(elem).css({'height': 'auto', 'overflow': 'auto'})
              }
            }
          );
        }

      }
    }
  }
]);
})();

(function() {

  'use strict';

  uw.filter('translate', function () {
    return function (input) {
      var translated = input;
      var dict = [
        {vcard: 'ADR', human: 'Address'},
        {vcard: 'CTRY', human: 'Country'},
        {vcard: 'EXTADD', human: 'Ext-Address'},
        {vcard: 'HOME', human: 'Home'},
        {vcard: 'LOCALITY', human: 'Locality'},
        {vcard: 'PCODE', human: 'Postal-Code'},
        {vcard: 'REGION', human: 'Region'},
        {vcard: 'STREET', human: 'Street'},
        {vcard: 'BDAY', human: 'Birthday'},
        {vcard: 'DESC', human: 'Description'},
        {vcard: 'EMAIL', human: 'Email'},
        {vcard: 'INTERNET', human: 'Internet'},
        {vcard: 'PREF', human: 'Pref'},
        {vcard: 'USERID', human: 'User'},
        {vcard: 'FN', human: 'Full-Name'},
        {vcard: 'JABBERID', human: 'IM-ID'},
        {vcard: 'N', human: 'Names'},
        {vcard: 'FAMILY', human: 'Last-Name'},
        {vcard: 'GIVEN', human: 'First-Name'},
        {vcard: 'MIDDLE', human: 'Middle-Name'},
        {vcard: 'NICKNAME', human: 'Nickname'},
        {vcard: 'ORG', human: 'Organization'},
        {vcard: 'ORGNAME', human: 'Org-Name'},
        {vcard: 'ORGUNIT', human: 'Org-Unit'},
        {vcard: 'PHOTO', human: 'Photo'},
        {vcard: 'BINVAL', human: 'Binary-Value'},
        {vcard: 'TYPE', human: 'Type'},
        {vcard: 'ROLE', human: 'Role'},
        {vcard: 'TEL', human: 'Telephone'},
        {vcard: 'HOME', human: 'Home-Phone'},
        {vcard: 'MSG', human: 'Messaging'},
        {vcard: 'NUMBER', human:'Number'},
        {vcard: 'TITLE', human: 'Title'},
        {vcard: 'URL', human: 'Url'},
        {vcard: 'X-ALT-EMAIL', human: 'Alt-Email-Addr'},
        {vcard: 'X-ALT-JABBERID', human: 'Alt-IM-ID'},
        {vcard: 'X-ASSISTANT', human: 'Assistant-Name'},
        {vcard: 'X-ASSISTANT-PHONE', human: 'Assistant-Phone-No'},
        {vcard: 'X-DID', human: 'DID'},
        {vcard: 'X-FACEBOOK', human: 'Facebook-Name'},
        {vcard: 'X-INTERN', human: 'Int-Number'},
        {vcard: 'X-LINKEDIN', human: 'LinkedIn-Name'},
        {vcard: 'X-LOCATION', human: 'Location'},
        {vcard: 'X-MANAGER', human: 'Manager'},
        {vcard: 'X-SALUTATION', human: 'Salutation'},
        {vcard: 'X-TWITTER', human: 'Twitter-Name'},
        {vcard: 'X-XING', human: 'Xing-Name'},

        {vcard: 'assistantPhoneNumber', human: 'Assistant Phone No.'},
        {vcard: 'faxNumber', human: 'Fax Number'},
        {vcard: 'avatar', human: 'Avatar'},
        {vcard: 'emailAddress', human: 'Email Address'},
        {vcard: 'homeAddress', human: 'Home Address'},
        {vcard: 'imDisplayName', human: 'IM Display Name'},
        {vcard: 'imId', human: 'IM ID'},
        {vcard: 'officeAddress', human: 'Office Address'},
        {vcard: 'city', human: 'City'},
        {vcard: 'country', human: 'Country'},
        {vcard: 'officeDesignation', human: 'Mail stop'},
        {vcard: 'state', human: 'State'},
        {vcard: 'street', human: 'Street'},
        {vcard: 'zip', human: 'ZIP'},
        {vcard: 'alternateEmailAddress', human: 'Alt. Email Addr.'},
        {vcard: 'alternateImId', human: 'Alt. IM ID'},
        {vcard: 'jobDept', human: 'Dept.'},
        {vcard: 'jobTitle', human: 'Title'},
        {vcard: 'location', human: 'Location'},
        {vcard: 'homePhoneNumber', human: 'Home Phone'},
        {vcard: 'cellPhoneNumber', human: 'Cell Phone'},
        {vcard: 'companyName', human: 'Company'},

        {vcard: 'assistantName', human: 'Assistant Name'},
        {vcard: 'lastName', human: 'Last Name'},
        {vcard: 'useBranchAddress', human: 'Use Branch Address'},
        {vcard: 'contact-information', human: 'Contact Information'},
        {vcard: 'branchName', human: 'Branch Name'},
        {vcard: 'enabled', human: 'Enabled'},
        {vcard: 'firstName', human: 'First Name'},
        {vcard: 'ldapManaged', human: 'LDAP Managed'},
        {vcard: 'salutation', human: 'Salutation'},
        {vcard: 'timestamp', human: 'Timestamp'},
        {vcard: 'didNumber', human: 'DID'},
        {vcard: 'facebookName', human: 'FB Name'},
        {vcard: 'linkedinName', human: 'LinkedIn Name'},
        {vcard: 'twiterName', human: 'Twitter Name'},
        {vcard: 'xingName', human: 'XING Name'},
        {vcard: 'branchAddress', human: 'Branch Address'},
        {vcard: 'manager', human: 'Manager'},

        {vcard: 'confEnter', human: 'Conference enter'},
        {vcard: 'confExit', human: 'Conference exit'},
        {vcard: 'vmBegin', human: 'Voicemail begin'},
        {vcard: 'vmEnd', human: 'Voicemail end'}
      ];

      _.find(dict, function (obj) {
        if ((translated === obj.vcard) || (translated === obj.human)) {
          translated = (translated === obj.vcard) ? obj.human : obj.vcard;
          return true
        }
      })

      return translated;
    }
  })

})();

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

(function() {

  'use strict';

  uw.filter('csearch', [
    function () {

      /*
        searches for either name or phone number
       */
      return function (searchArr, keyword) {
        return _.filter(searchArr, function (el) {
          return el.name.toString().toLowerCase().indexOf(keyword.toString().toLowerCase()) > -1 ||
                      (el.number && el.number.toString().toLowerCase().indexOf(keyword.toString().toLowerCase()) > -1);
        })

      }
    }
  ])

})();

(function () {

  'use strict';

  /**
   * scroll document element into view
   * useful when body has overflow:hidden
   */
  uw.directive('clickView', function () {
    return {
      restrict: 'A',
      link: function (scope, elem, attrs) {
        elem.on('click', function () {
          if (scope.item) {
            if (scope.item.type === 'right') {
              document.querySelector('.right-side-view').scrollIntoView();
            }
            return;
          } else if (attrs.clickView === 'true') {
            document.querySelector('.right-side-view').scrollIntoView();
            return;
          } else {
            document.querySelector('.left-side-view').scrollIntoView();
            return;
          }
        })

      }
    }
  })

})();

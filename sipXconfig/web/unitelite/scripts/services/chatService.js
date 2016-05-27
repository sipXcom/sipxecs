(function(){
'use strict';

/**
 * chat service abstracting strophe
 * @param  {Object} $rootScope                  angular root scope
 * @param  {Object} ProfileFactory              creates user profile based on vCard
 * @param  {Object} Strophe                     angular DI for strophe.js
 * @param  {Object} _                           angular DI for underscore.js
 * @param  {Object} CONFIG                      app-wide configuration
 */
uw.service('chatService', [
  '$q',
  '$rootScope',
  'rosterService',
  'restService',
  'profileFactory',
  'sharedFactory',
  'strophe',
  'notification',
  '_',
  'CONFIG',
  function ($q, $rootScope, rosterService, restService, profileFactory, sharedFactory, strophe, notification, _, CONFIG) {
    var connectionRetryLimit    = CONFIG.connectionRetryLimit;
    var resourcePriority        = CONFIG.resourcePriority;
    var domain                  = CONFIG.domain;
    var resource                = CONFIG.resource;
    var $                       = angular.element;
    var self                    = this;

    $rootScope.myJid            = null;
    this.connection             = null;
    this.retries                = 0;
    this.noChatstateJids        = [];
    this.vCardRequests          = [];
    this.vCardRequestInterval   = null;
    this.groupChats             = null;
    this.activeList             = '';
    this.constants              = {
      CHATSTATE_PAUSED_TIMEOUT: CONFIG.chatstatePausedTimeout,
      CHATSTATE_INACTIVE_TIMEOUT: CONFIG.chatstateInactiveTimeout,
      CHATSTATE_GONE_TIMEOUT: CONFIG.chatstateGoneTimeout
    };
    this.muc = {};

    /**
     * initialize strophe connection
     * extends current namespace in strophe
     * calls .addConnectionHandlers function
     * @param  {String} url     HTTP Bind URL to connect to
     * @return {}
     */
    this.init = function (url) {
      strophe.addNamespace('VCARD', 'vcard-temp');
      strophe.addNamespace('XHTMLIM', 'http://jabber.org/protocol/xhtml-im');
      strophe.addNamespace('W3XHTML', 'http://www.w3.org/1999/xhtml');
      strophe.addNamespace('CHATSTATES', 'http://jabber.org/protocol/chatstates');
      strophe.addNamespace('PRIVACY', 'jabber:iq:privacy');
      this.connection = new strophe.Connection(url);

      _.bindAll(this, 'handleRoster', 'handleVCard', 'handleConnection', 'handlePresence', 'handleMessage', 'handleChatstate');
      this.addConnectionHandlers();
    };

    /**
     * adds stanza handlers for strophe connection
     */
    this.addConnectionHandlers = function () {
      this.connection.addHandler(this.handleRoster, strophe.NS.ROSTER, 'iq', 'result');
      this.connection.addHandler(this.handleVCard, strophe.NS.VCARD, 'iq', 'result');
      this.connection.addHandler(this.handlePresence, null, 'presence');
      this.connection.addHandler(this.handleMessage, null, 'message');
      this.connection.addHandler(this.handleChatstate, strophe.NS.CHATSTATES, 'message');
    }

    /**
     * attaches strophe to an existing connection
     * @param  {String} jid       XMPP JID
     * @param  {String} sid       XMPP SID
     * @param  {String} rid       XMPP RID
     * @return {}
     */
    this.attach = function (jid, sid, rid) {
      if (!jid || !sid || !rid) {
        $rootScope.$broadcast('services.chat.authfail', {});
        return;
      }

      this.connection.attach(jid, sid, rid, this.handleConnection);
    };

    /**
     * connects strophe using the provided parameters
     * @param  {String} jid           XMPP JID
     * @param  {String} password      Password
     * @return {}
     */
    this.connect = function (jid, password) {
      if (!jid || !password) {
        $rootScope.$broadcast('services.chat.authfail', {});
        return;
      }

      jid = this.getValidJid(jid);
      if (!jid) {
        $rootScope.$broadcast('services.chat.authfail', {});
        return;
      }

      restService.updateCredentials(jid, password);
      this.connection.connect(jid, password, this.handleConnection);
    };

    /**
     * creates new strophe connection
     * @return {Object}       contains XMPP JID and Password
     */
    this.createNewConnection = function () {
      var jid           = this.connection.jid;
      var password      = this.connection.pass;
      var url           = this.connection.service;
      this.connection   = new strophe.Connection(url);
      this.addConnectionHandlers();

      return {
        jid: jid,
        password: password
      };
    };

    /**
     * reconnects strophe
     * based on number of retries
     * @return {}
     */
    this.reconnect = function () {
      if (this.retries >= connectionRetryLimit) {
        $rootScope.$broadcast('services.chat.disconnected', this.connection);
        this.createNewConnection();
        return;
      }

      var obj = this.createNewConnection();
      this.connection.connect(obj.jid, obj.password, this.handleConnection);
      this.retries++;
    };

    /**
     * disconnects current strophe connection
     */
    this.disconnect = function () {
      this.connection.disconnect();
    };

    /**
     * muc
     */
    this.muc.rooms = [];
    /**
     * muc discover rooms
     * @param  {Function} cb      callback
     */
    this.muc.discoverRooms = function(server) {
      var deferred = $q.defer();

      self.connection.muc.discoverRooms(server, function (response) {
        var children  = $(response).children().children();
        var rooms     = [];
        var obj       = {};

        _.each(children, function (item) {
          obj.name  = $(item).attr('name');
          obj.jid   = $(item).attr('jid');
          rooms.push(angular.copy(obj));
        });

        deferred.resolve(rooms);
      });

      return deferred.promise;
    }
    /**
     * muc join room
     * @param  {String}   room      room name
     * @param  {Function} cb        callback
     */
    this.muc.joinRoom = function(url, room, nick) {
      var deferred  = $q.defer();
      var obj       = {};
      var msg       = '';
      var result;

      self.connection.muc.joinRoom(url, room, nick, function (response) {
        result = response;

        _.each($(response).children(), function (el) {
          if ($(el)[0].nodeName === 'error') {
            result = $($(el).children()[0])[0].nodeName;
          }
        });

        if (!_.isObject(result)) {
          switch (result) {
            case 'conflict':
              msg = 'Nickname conflict. Please choose another nickname.'
              break;
            case 'forbidden':
              msg = 'Banned. Please contact room administrator.'
              break;
            case 'registration-required':
              msg = 'Room is members-only.'
              break;
            case 'not-authorized':
              msg = 'Password required.'
              break;
            case 'service-unavailable':
              msg = 'Maximum users reached. Please try again later.'
            default: break;
          }
        }

        if (!_.isEmpty(msg)) {
          deferred.reject({failed: result, msg: msg});
        } else {
          obj = {
            room: room,
            jid: self.muc.getJid(room, url),
            timestamp: (new Date()).getTime(),
            server: url,
            nick: nick
          };
          self.muc.rooms.push(obj);
          deferred.resolve(obj);
        }

      }, {'seconds': '10000000'});

      return deferred.promise;
    }
    /**
     * muc leave room
     * @param  {String}   room      room name
     * @param  {Function} cb        callback
     */
    this.muc.leaveRoom = function(url, room, nick) {
      var deferred = $q.defer();

      self.connection.muc.leaveRoom(url, room, nick, null, function (response) {
      });

      self.muc.rooms = _.filter(self.muc.rooms, function (item) {
        return item.room !== room
      });

      deferred.resolve('true');

      return deferred.promise
    }
    this.muc.queryOccupants = function(url, success, error) {
        var attrs, info;
        attrs = {
            xmlns: Strophe.NS.DISCO_ITEMS
        };
        info = $iq({
            from: self.connection.jid,
            to: url,
            type: 'get'
        }).c('query', attrs);

        self.connection.sendIQ(info, success, error);
    },
    /**
     * muc register handlers
     * @param  {String} room room name
     */
    this.muc.registerHandlers = function (room) {
      // var mucUrl = CONFIG.mucServiceName;
      // this.connection.muc.registerHandlers(mucUrl, room, function cb() {
      //   this.presence = function (presence) {
      //     console.log('HANDLER: presence   '+presence);
      //   }
      //   this.nickchanged = function (oldnick, newnick) {
      //     console.log('HANDLER: oldnick   '+oldnick);
      //     console.log('HANDLER: newnick   '+newnick);
      //   }
      //   this.joined = function (presence) {
      //     console.log('HANDLER: joined   '+presence);
      //   }
      //   this.left = function (presence) {
      //     console.log('HANDLER: left   '+presence);
      //   }
      //   this.kicked = function (presence) {
      //     console.log('HANDLER: kicked   '+presence);
      //   }
      //   this.destroyed = function (presence) {
      //     console.log('HANDLER: destroyed   '+presence);
      //   }
      //   this.roommessage = function (msg) {
      //     console.log('HANDLER: roommessage   '+msg);
      //   }
      //   this.privatemessage = function (msg) {
      //     console.log('HANDLER: privatemessage   '+msg);
      //   }
      //   this.invited = function (msg) {
      //     console.log('HANDLER: invited   '+msg);
      //   }
      // });
    }
    /**
     * muc get JID of room
     * @param  {String} room      room name
     * @return {String}           room JID
     */
    this.muc.getJid = function (room, url) {
      return room + '@' + url
    }

    /**
     * muc get conference server
     * @return {String}     server name
     */
    this.muc.getServer = function () {
      var deferred = $q.defer();
      var result;

      self.connection.disco.discoverItems(CONFIG.domain, null, function (response) {
        var items = $(response).children().children();

        _.find(items, function (el) {
          if ($(el.attributes[0])[0].value.indexOf('conference') !== -1) {
            result = $(el.attributes[0])[0].value;
            deferred.resolve(result);
            return true;
          }

          if ($(el.attributes[1])[0].value.indexOf('Public Chatrooms') !== -1) {
            result = $(el.attributes[0])[0].value;
            deferred.resolve(result);
            return true;
          }
        })
      })

      return deferred.promise;
    }

    /**
     * muc get handle/nick
     * @return {String}     handle/nick
     */
    this.muc.getHandle = function () {
      return self.connection.jid.split('@')[0]
    }

    /**
     * handles strophe connection
     * @param  {String} status      strophe connection status
     */
    this.handleConnection = function (status) {
      switch (status) {
        case strophe.Status.CONNECTED:
        case strophe.Status.ATTACHED:
          this.retries = 0;
          // $rootScope.myJid = strophe.getBareJidFromJid(this.connection.jid);
          this.profile = profileFactory.init(strophe.getBareJidFromJid(this.connection.jid));
          // this.profile.jid = $rootScope.myJid;
          this.initRoster();
          this.initVCard();
          sharedFactory.init(this.connection.jid);
          sharedFactory.getArchiveObj();
          $rootScope.$broadcast('services.chat.connected');
          console.log('strophe is connected.');
          break;
        case strophe.Status.CONNFAIL:
          this.createNewConnection();
          $rootScope.$broadcast('services.chat.connfail');
          console.log('strophe failed to connect.');
          break;
        case strophe.Status.AUTHFAIL:
          this.createNewConnection();
          $rootScope.$broadcast('services.chat.authfail');
          console.log('strophe failed to authenticate.');
          break;
        case strophe.Status.CONNECTING:
          $rootScope.$broadcast('services.chat.connecting');
          console.log('strophe is connecting.');
          break;
        case strophe.Status.AUTHENTICATING:
          $rootScope.$broadcast('services.chat.authenticating');
          console.log('strophe is authenticating.');
          break;
        case strophe.Status.DISCONNECTING:
          // this.reconnect();
          $rootScope.$broadcast('services.chat.disconnecting');
          console.log('strophe is disconnecting.');
          break;
        case strophe.Status.DISCONNECTED:
          this.reconnect();
          console.log('strophe is disconnected.');
          break;
        case strophe.Status.ERROR:
        default:
          this.createNewConnection();
          $rootScope.$broadcast('services.chat.error', this.connection);
          console.log('strophe error.');
          break;
      }
    };

    /**
     * handles the roster
     * sets visible and invisible list
     * sets available presence
     * @param  {Object} data      the roster
     * @return {Boolean}          true
     */
    this.handleRoster = function (data) {
      // set visible/invisble list
      this.setVisibleList();
      this.setInvisibleList();

      // set presence after receiving roster to avoid race conditions
      this.setAvailablePresence();

      rosterService.set(data);

      _.each(rosterService.roster, function (contact, jid) {
        this.fetchVCard(jid);
      }, this);

      $rootScope.$broadcast('services.chat.receivedRoster', {});

      return true;
    };

    /**
     * handles setting vCards
     * @param  {Object} data      vCard response
     * @return {Boolean}          true
     */
    this.handleVCard = function (data) {
      var from = $(data).attr('from');

      if (!from) {
        this.profile = _.extend(this.profile, profileFactory.set(data));
        // this.profile.set(data);
        restService.updateCredentials(this.profile.vCard['X-INTERN']);
        $rootScope.$broadcast('services.chat.receivedMyVCard', this.profile);
      } else {
        var result = rosterService.setProfile(from, data);
        if (result)
          $rootScope.$broadcast('services.chat.receivedContactVCard', result);

      }

      return true;
    };

    /**
     * presence handler
     * @param  {Object} data      presence response
     * @return {Boolean}          true
     */
    this.handlePresence = function (data) {
      var type = $(data).attr('type');
      var from = $(data).attr('from');

      if (type !== 'error') {
        if (type === 'unavailable') {
          if (!this.isMe(from)) {
            rosterService.removeResource(from);
            $rootScope.$broadcast('services.chat.receivedPresence', {
              jid: from,
              type: 'unavailable'
            });
          }
        } else if (type === 'unsubscribed') {
          console.log(from, 'unsubscribed');
        } else {
          if (!this.isMe(from)) {
            rosterService.setResource(from, data);
            $rootScope.$broadcast('services.chat.receivedPresence', {
              jid: from
            });
          }
        }
      }

      return true;
    };

    /**
     * handles receiving XMPP messages
     * @param  {Object} data      received message
     * @return {Boolean}          true
     */
    this.handleMessage = function (data) {
      var type        = $(data).attr('type');
      var from        = $(data).attr('from');
      var to          = $(data).attr('to');
      var delay       = $(data).find('delay').attr('from');
      var delayTime   = $(data).find('delay').attr('stamp');
      var body        = [];
      var jqBody;
      var index = _.find(self.muc.rooms, function (obj) {
        return from.split('/')[0] === obj.jid;
      });

      // if the msg is actually user's own msg broadcast from the room
      if ( (!_.isUndefined(index)) && (!delay) && (to === this.connection.jid) && (from.split('/')[1] === index.nick) ) return true;

      if (type !== 'error') {
        jqBody = $(data).find('body');
        if (jqBody.length > 0) {
          body = $(jqBody[0]);
          var chatstate = $(data).find('[xmlns="' + strophe.NS.CHATSTATES + '"]');
          if (chatstate.length === 0) {
            this.setNoChatstate(from);
          }
          var text = body.text();

          var obj = {
            text: text,
            type: 'received'
          };

          if (!_.isUndefined(index)) {
            obj.from      = from.split('/')[0];
            obj.msgType   = 'groupchat';
            obj.fromNormal = from.split('/')[1];
          } else {
            obj.from        = this.getBareJidFromJid(from);
            obj.fromNormal  = (rosterService.getContact(from)).name;
            obj.msgType     = 'chat';
          };

          if (delayTime) {
            obj.timestamp = new Date(delayTime).getTime();
          }

          // var htmlBody = $(data).find('html[xmlns="' + strophe.NS.XHTMLIM + '"] body[xmlns="' + strophe.NS.W3XHTML + '"]');
          var htmlBody = (jqBody[1]) ? ($(jqBody[1])) : []
          var html;

          if (htmlBody.length > 0) {
            html = htmlBody.html();
            obj.html = html;
          }

          // sharedFactory.archiveMessage(obj.from, this.connection.jid, obj.text, html);
          $rootScope.$broadcast('services.chat.receivedMessage', obj);

          notification({
            title: obj.fromNormal,
            body: obj.text
          })
        }
      }

      return true;
    };

    /**
     * handles chat states
     * @param  {Object} data      chatstate obj
     * @return {Boolean}          true
     */
    this.handleChatstate = function (data) {
      var composing   = $(data).find('composing');
      var paused      = $(data).find('paused');
      var active      = $(data).find('active');
      var inactive    = $(data).find('inactive');
      var gone        = $(data).find('gone');
      var from        = $(data).attr('from');
      var obj         = {
        from: from
      };

      if (composing.length > 0) {
        obj.chatstate = 'composing';
      }

      if (paused.length > 0) {
        obj.chatstate = 'paused';
      }

      if (active.length > 0) {
        obj.chatstate = 'active';
      }

      if (inactive.length > 0) {
        obj.chatstate = 'inactive';
      }

      if (gone.length > 0) {
        obj.chatstate = 'gone';
      }

      if (obj.chatstate) {
        $rootScope.$broadcast('services.chat.receivedChatstate', obj);
        this.unsetNoChatstate(from);
      }

      return true;
    };

    /**
     * creates stanza to fetch roster and sends it
     */
    this.fetchRoster = function () {
      var getRosterIq = $iq({
        from: this.connection.jid,
        type: 'get',
        id: this.connection.getUniqueId()
      }).c('query', {
        xmlns: strophe.NS.ROSTER
      });
      this.connection.send(getRosterIq);
    };

    /**
     * creates stanza to fetch vcard
     * @param  {String} toJid       XMPP JID
     */
    this.fetchVCard = function (toJid) {
      var self    = null;
      var attrs   = {
        from: this.connection.jid,
        type: 'get',
        id: this.connection.getUniqueId()
      };
      var getVCardIq = $iq().c('vCard', {
        xmlns: strophe.NS.VCARD
      }).up();

      if (toJid) {
        attrs.to = toJid;
      }
      getVCardIq.attrs(attrs);
      this.vCardRequests.push(getVCardIq);

      self = this;
      clearInterval(this.vCardRequestInterval);
      this.vCardRequestInterval = setInterval(function () {
        self.sendVCardRequest();
      }, 500);
    };

    /**
     * sends vcard requests
     * @return {}
     */
    this.sendVCardRequest = function () {
      if (this.vCardRequests.length > 0) {
        var getVCardIq = this.vCardRequests.shift();
        this.connection.send(getVCardIq);
      } else
        clearInterval(this.vCardRequestInterval);
    };

    /**
     * sends XMPP message
     * @param  {String} recipientJid      recipient XMPP JID
     * @param  {String} text              the text to send
     * @param  {String} html              the html to send
     */
    this.sendMessage = function (recipientJid, text, html, type) {
      var message = $msg({
        from: this.connection.jid,
        to: recipientJid,
        type: type || 'chat'
      }).c('body').t(text).up();

      var obj = {
        to: recipientJid,
        text: text,
        type: type || 'chat'
      };

      if (this.isChatstateSupported(recipientJid)) {
        message.c('active', {
          xmlns: strophe.NS.CHATSTATES
        }).up();
      }

      if (html) {
        if (html !== text) {
          obj.html = html;

          message.c('html', {
            'xmlns': strophe.NS.XHTMLIM
          }).c('body', {
            'xmlns': strophe.NS.W3XHTML
          }).cnode($('<div>' + html + '</div>')[0]);
        }
      }

      this.connection.send(message);
      // if (obj.type !== 'groupchat')
      // sharedFactory.archiveMessage(this.connection.jid, recipientJid, text, html);
      $rootScope.$broadcast('services.chat.sentMessage', obj);
    };

    /**
     * sends chat state
     * @param  {String} recipientJid      recipient XMPP JID
     * @param  {String} chatstate         chat state
     * @param  {String} type              chat type
     * @return {}
     */
    this.sendChatstate = function (recipientJid, chatstate, type) {
      if (!this.isChatstateSupported(recipientJid)) return;
      if (!chatstate) chatstate = 'active';
      if (!type) type = 'chat';

      var message = $msg({
        to: recipientJid,
        type: type
      }).c(chatstate, {
        xmlns: strophe.NS.CHATSTATES
      });

      this.connection.send(message);
    };

    /**
     * sets available presence
     */
    this.setAvailablePresence = function () {
      this.setPresence('Available');
    };

    /**
     * sets unavailable presence
     */
    this.setUnavailablePresence = function () {
      var presence = $pres({
        from: this.connection.jid,
        type: 'unavailable'
      });

      this.connection.send(presence);
    };

    /**
     * sets the presence
     * @param {String} status         current status
     * @param {String} show           current presence
     * @param {String} priority       presence priority
     */
    this.setPresence = function (status, show, priority) {
      if (this.activeList === 'invisible' && show !== 'invisible') {
        this.activateVisibleList();
      }

      if (!_.isFinite(priority)) {
        priority = resourcePriority;
      }

      if (!_.contains(['away', 'dnd', 'chat', 'xa'], show)) {
        // default online/available
        show = null;
      }

      var presence = $pres({
        from: this.connection.jid
      });
      if (show !== null) {
        presence.c('show', {}, show);
      }
      if (status !== null && status !== undefined && status !== '') {
        presence.c('status', {}, status.toString());
      }
      if (priority !== null && priority !== undefined && priority !== '') {
        presence.c('priority', {}, priority.toString());
      }
      this.connection.send(presence);
    };

    /**
     * sets invisible presence
     */
    this.setInvisiblePresence = function () {
      var iq = $iq({
        from: this.connection.jid,
        type: 'set',
        id: this.connection.getUniqueId()
      }).c('query', {
        xmlns: strophe.NS.PRIVACY
      }).c('active', {
        name: 'invisible'
      });

      this.setUnavailablePresence();
      this.connection.send(iq);
      this.activeList = 'invisible';

      // normal set presence to receive messages
      this.setPresence('Invisible', 'invisible');
    };

    /**
     * activates visible list
     * @return {}
     */
    this.activateVisibleList = function () {
      var iq = $iq({
        from: this.connection.jid,
        type: 'set',
        id: this.connection.getUniqueId()
      }).c('query', {
        xmlns: strophe.NS.PRIVACY
      }).c('active', {
        name: 'visible'
      });

      this.connection.send(iq);
      this.activeList = 'visible';
    };

    /**
     * sets visible list
     */
    this.setVisibleList = function () {
      var iq = $iq({
        from: this.connection.jid,
        type: 'set',
        id: this.connection.getUniqueId()
      }).c('query', {
        xmlns: strophe.NS.PRIVACY
      }).c('list', {
        name: 'visible'
      }).c('item', {
        action: 'allow',
        order: '1'
      }).c('presence-out');

      this.connection.send(iq);
    };

    /**
     * sets invisible list
     */
    this.setInvisibleList = function () {
      var iq = $iq({
        from: this.connection.jid,
        type: 'set',
        id: this.connection.getUniqueId()
      }).c('query', {
        xmlns: strophe.NS.PRIVACY
      }).c('list', {
        name: 'invisible'
      }).c('item', {
        action: 'deny',
        order: '1'
      }).c('presence-out');

      this.connection.send(iq);
    };

    /**
     * verifies if XMPP JID is user
     * @param  {String}  jid      XMPP JID
     * @return {Boolean}          result of comparison
     */
    this.isMe = function (jid) {
      var bareJid = strophe.getBareJidFromJid(jid);
      return bareJid === $rootScope.myJid;
    };

    /**
     * gets a valid XMPP JID from input param
     * @param  {String} input       the string to be parsed
     * @return {String}             valid JID
     */
    this.getValidJid = function (input) {
      // node@domain/resource
      input = input.split('/')[0]; // remove resource

      var split = input.split('@');
      if (split.length > 1) {
        // node@something
        if (split[1] === domain) {
          // node@domain
          return input  + '/' + resource;
        } else {
          // node@otherdomain
          return input.replace('@', '+') + '@' + domain + '/' + resource;
        }
      } else {
        // node
        return input + '@' + domain + '/' + resource;
      }
    };

    this.getBareJidFromJid = function (jid) {
      return strophe.getBareJidFromJid(jid);
    }

    /**
     * pushes XMPP JID param into .noChatstateJids list
     * @param {String} jid      XMPP JID
     */
    this.setNoChatstate = function (jid) {
      if (this.isChatstateSupported(jid)) {
        this.noChatstateJids.push(jid);
      }
    };

    /**
     * removes from .noChatstateJids list the XMPP JID param
     * @param  {String} jid       XMPP JID
     * @return {}
     */
    this.unsetNoChatstate = function (jid) {
      if (!this.isChatstateSupported(jid)) {
        this.noChatstateJids = _.reject(this.noChatstateJids, function (noChatstateJid) {
          return noChatstateJid === jid;
        });
      }
    };

    /**
     * returns bool if .noChatstateJids contains param XMPP JID
     * @param  {String}  jid      XMPP JID
     * @return {Boolean}          .noChatstateJids contains @jid
     */
    this.isChatstateSupported = function (jid) {
      return !_.contains(this.noChatstateJids, jid);
    };

    /**
     * adds a contact, equivalent of moving contact between groups
     * also contains a callback which can have an error parameter,
     * in case the groups are defined on the server side and cannot be changed
     * @param {String}   jid            XMPP JID
     * @param {String}   name           contact name
     * @param {String}   groups         groups to add contact to
     * @param {Function} callback       callback after adding
     */
    this.addContact = function (jid, name, groups, callback) {
      var iq = $iq({
        from: this.connection.jid,
        type: 'set',
        id: this.connection.getUniqueId()
      }).c('query', { xmlns: Strophe.NS.ROSTER })
        .c('item', { jid: jid, name: name || '' });

      if (groups) {
        if (_.isString(groups))
          iq.c('group').t(groups).up();
        if (_.isArray(groups))
          _.each(groups, function (group) {
            iq.c('group').t(group).up();
          });
      };

      this.connection.sendIQ(iq, success, error);

      function success() {
        callback();
      };
      function error() {
        callback(new Error());
      }
    }

    /**
     * removes contact
     * @param  {String} jid XMPP JID
     */
    this.deleteContact = function (jid) {
      var iq = $iq({
        from: this.connection.jid,
        type: 'set',
        id: this.connection.getUniqueId()
      }).c('query', { xmlns: Strophe.NS.ROSTER })
        .c('item', { jid: jid, subscription: 'remove' });

      this.connection.sendIQ(iq);
    }

    /**
     * modifies contact
     * @param  {String}   jid           XMPP JID
     * @param  {String}   name          contact name
     * @param  {String}   groups        groups to add the contact to
     * @param  {Function} callback      callback after modification
     */
    this.modifyContact = function (jid, name, groups, callback) {
      this.addContact(jid, name, groups, callback);
    }

    /**
     * renames a group
     * @param  {String}   oldName       old group name
     * @param  {String}   newName       new group name
     * @param  {Function} callback      callback after group name change
     */
    this.renameGroup = function(oldName, newName, callback) {
      var flag = false;
      var self = this;
      var done = _.after(Object.keys(rosterService.groups[oldName]).length, function() {
        if (!_.isUndefined(rosterService.groups[newName]))
          _.extend(rosterService.groups[newName], rosterService.groups[oldName]);
        else
          rosterService.groups[newName] = angular.copy(rosterService.groups[oldName]);
        delete rosterService.groups[oldName];
        $rootScope.$broadcast('services.chat.receivedRoster', {});
      });

      _.find(rosterService.groups[oldName], function (contact) {
        flag = false;
        if (_.indexOf(contact.groups, newName) !== -1)
          (flag = true, delete contact.groups[_.indexOf(contact.groups, oldName)]);
        else
          contact.groups[_.indexOf(contact.groups, oldName)] = newName;

        self.modifyContact(contact.jid, contact.name, contact.groups, function (err) {
          if (err) {
            if (flag)
              contact.groups.push(oldName);
            else
              contact.groups[_.indexOf(contact.groups, newName)] = oldName;
            callback(new Error());
            return;
          } else
            done();
        });
      });
    };

    this.initRoster = function () {

      this.fetchRoster();

    }

    this.initVCard = function () {

      this.fetchVCard();

    }

    /**
     * inverse of strophe.xmlescape
     * @param  {String} text      the text to be unescaped
     * @return {String}           unescaped text
     */
    this.xmlunescape = function (text) {
      return text
        .replace(/\&amp;/g, '&')
        .replace(/\&lt;/g, '<')
        .replace(/\&gt;/g, '>')
        .replace(/\&apos;/g, '\'')
        .replace(/\&quot;/g, '"');
    };
  }
]);
})();

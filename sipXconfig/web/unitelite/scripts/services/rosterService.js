(function(){
'use strict';

/**
 * roster service dealing with the roster
 * @param  {Object} $rootScope                  angular root scope
 * @param  {Object} ProfileFactory              creates user profile based on vCard
 * @param  {Object} Strophe                     angular DI for strophe.js
 * @param  {Object} _                           angular DI for underscore.js
 * @param  {Object} CONFIG                      app-wide configuration
 * @return {}
 */
uw.service('rosterService', [
  '$sce',
  '$rootScope',
  'sharedFactory',
  'profileFactory',
  'strophe',
  '_',
  'CONFIG',
  function ($sce, $rootScope, sharedFactory, profileFactory, strophe, _, CONFIG) {
    this.received       = false;
    this.roster         = {};
    this.groups         = {};
    this.activityList   = {};
    this.photos = {};
    var groups          = {};
    var noGroup         = CONFIG.groupless;
    var self            = this;
    var $               = angular.element;

    /**
     * sets the roster based on the response from the server
     * fills .groups property
     * @param {Object} roster       the roster received from the server
     */
    this.set = function (roster) {
      // var sharedRoster = sharedFactory.getRoster();
      var convList      = sharedFactory.getConversationList();
      var sharedPhotos  = sharedFactory.getPhotos();
      // if (typeof sharedRoster !== 'string') {
      //   this.photos = sharedPhotos;
      //   this.roster = sharedRoster;
      //   this.groups = formatGroups(this.roster);
      //   sharedRoster = null;
      //   sharedPhotos = null;
      // } else
      if (typeof sharedPhotos !== 'string')
        this.photos = sharedPhotos;
      else
        this.photos = {};

      if (typeof convList !== 'string')
        this.activityList = convList;
      else
        this.activityList = [];

      this.roster = formatRoster(roster);

      // sets groupchat connected property to false on init
      // helps with flushing out the local storage for groupchats
      _.each(this.activityList, function (obj) {
        if (obj.show) obj.show = 'offline';
        if (obj.status) obj.status = 'offline';
        if (obj.connected) obj.connected = false;
      });

      this.received = true;
      // sharedFactory.saveRoster(angular.copy(this.roster));

      function formatRoster (r) {
        return _.reduce($(r).find('item'), function (acc, item) {
          var jid         = $(item).attr('jid');
          var name        = $(item).attr('name');
          var itemGroups  = $(item).find('group');
          var contact     = {
            jid: jid,
            name: name ? name : strophe.getNodeFromJid(jid),
            cResource: {
              show: 'offline',
              status: 'offline'
            },
            resources: {},
            profile: {}
          };

          if (itemGroups[0] === undefined) {
            contact.groups = [noGroup];
            if (!self.groups[noGroup]) self.groups[noGroup] = [];
            self.groups[noGroup].push(contact);
            // self.groups[noGroup][jid] = contact;
          } else {
            contact.groups = _.chain(itemGroups)
              .map(function (item) {
                var group = $(item).text();
                if (!self.groups[group]) self.groups[group] = [];
                self.groups[group].push(contact);
                // self.groups[group][jid] = contact;
                return group;
              })
              .toArray()
              .value();
          };
          acc[jid] = contact;

          for (var i = 0; i < self.activityList; i++) {
            if (self.activityList[i].jid === jid) {
              self.activityList[i].jid      = contact.jid;
              self.activityList[i].show     = contact.cResource.show;
              self.activityList[i].status   = contact.cResource.status;
            }
          }

          return acc;
        }, {});
      }

      function formatGroups (r) {
        var grouped = _.groupBy(r, 'groups');
        var oddOne;

        _.chain(grouped)
          .keys(grouped)
          .each(function (num) {
            if (num.indexOf(',') !== -1) {
              oddOne = grouped[num];
              delete grouped[num];
              _.each(grouped, function (arr, k) {
                grouped[k] = _.union(oddOne, arr);
              })
            }
          })

        return grouped;
      }

    };

    /**
     * sets the profile of a contact
     * calls .set method from profileFactory
     * @param {String} fromJid      XMPP JID
     * @param {Object} vCard        vCard
     */
    this.setProfile = function (fromJid, vCard) {
      var bareFrom  = strophe.getBareJidFromJid(fromJid);
      var contact   = this.roster[bareFrom];
      if (!contact) return false;
      contact.profile = profileFactory.set(vCard);
      if (contact.profile.vCard) {
        if (contact.profile.vCard.PHOTO) {
          this.photos[bareFrom]         = contact.profile.vCard.PHOTO;
          delete contact.profile.vCard.PHOTO;
        }
      }

      this.roster[bareFrom] = contact;
      sharedFactory.savePhotos(angular.copy(this.photos));
      // sharedFactory.saveRoster(angular.copy(this.roster));

      return contact
    };

    /**
     * sets XMPP resources
     * @param {String} fromJid        XMPP JID
     * @param {Object} presence       presence stanza
     */
    this.setResource = function (fromJid, presence) {
      var contact = this.getContact(fromJid);
      if (!contact) return;

      var resource      = strophe.getResourceFromJid(fromJid);
      var show          = $(presence).find('show');
      var status        = $(presence).find('status');
      var priority      = $(presence).find('priority');
      var showText      = (show.length > 0) ? show.text() : 'available';
      var statusText    = (status.length > 0) ? status.text() : 'available';
      var priorityInt   = (priority.length > 0) ? parseInt(priority.text()) : 0;

      contact.resources = {};
      contact.resources[resource] = {
        show: showText,
        status: statusText,
        priority: priorityInt,
        timestamp: (new Date()).getTime()
      };
      contact.cResource         = {}
      contact.cResource.show    = showText;
      contact.cResource.status  = statusText;
    };

    /**
     * removes XMPP resource
     * @param  {String}       fromJid XMPP JID
     */
    this.removeResource = function (fromJid) {
      var contact = this.getContact(fromJid);
      var resource = strophe.getResourceFromJid(fromJid);
      delete contact.resources[resource];
      if (_.isEmpty(contact.resources))
        contact.cResource.show = contact.cResource.status = 'offline';
      else {
        _.each(contact.resources[resource], function (res) {
          if (res.priority <= 1) {
            contact.cResource.status = res.status;
            contact.cResource.show = res.show;
          }
        })
      }
    };

    /**
     * gets contact from roster
     * @param  {String} jid       XMPP JID
     * @return {Object}           roster contact OR made-up roster contact for groupchats
     */
    this.getContact = function (jid) {
      var bareJid = strophe.getBareJidFromJid(jid);

      if (!this.roster[bareJid])
        return {
          name: jid.split('@')[0],
          jid: jid.split('@')[1],
          resources:{
            groupchat: {
              show: 'available',
              status: 'available'
            }
          },
          profile: {},
          type: 'groupchat'
        }
      else
        return this.roster[bareJid];
    };

    /**
     * gets presence from resources
     * @param  {Object} contact       contact to get presence from
     * @return {Object}               presence
     */
    this.getPresence = function (contact) {
      return contact ? _.reduce(contact.resources, function (acc, r) {
        if (acc === 'offline') return r;

        if (acc.priority > r.priority) {
          return acc;
        } else if (acc.priority === r.priority) {
          if (acc.timestamp > r.timestamp) {
            return acc;
          } else {
            return r;
          }
        } else {
          return r;
        }
      }, 'offline') : 'offline';
    };

    /**
     * searches the roster
     * @param  {String} filter      filter the roster
     * @return {String}             contact name
     */
    this.search = function (filter) {
      if (!filter) return [];
      filter = filter.toLowerCase();
      return _.filter(this.roster, function (contact, jid) {
        var nodeMatch = strophe.getNodeFromJid(jid).indexOf(filter) >= 0;
        var nameMatch = contact['Full-Name'].toLowerCase().indexOf(filter) >= 0;
        ///
        var fullNameMatch = contact['Full-Name'].toLowerCase().indexOf(filter) >= 0;
        // var nameMatch = contact.name.toLowerCase().indexOf(filter) >= 0;
        // var fullNameMatch = contact.profile.fullName().toLowerCase().indexOf(filter) >= 0;
        return nodeMatch || nameMatch || fullNameMatch;
      });
    };
  }
]);
})();

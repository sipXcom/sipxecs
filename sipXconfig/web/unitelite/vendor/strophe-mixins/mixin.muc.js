/*
  Copyright 2010, Drakontas LLC
  Ilya Braude ilya@drakontas.com
*/

/*globals Strophe, $pres, $msg*/

(function($sp){

Strophe.addConnectionPlugin('muc', {
    /*
     Extend connection object to have plugin name 'muc'.
    */
    _conn: null,

    //The plugin must have the init function.
    init: function(conn) {

        this._conn = conn;

        /*
          Function used to setup plugin.
        */

        /* extend name space
         *  NS.MUC - XMPP MUC NS
         *  NS.MUC_USER - XMPP MUC User NS
         *
         */
        Strophe.addNamespace('MUC',"http://jabber.org/protocol/muc");
        Strophe.addNamespace('MUC_USER', Strophe.NS.MUC + "#user");
    },


    /** Function: joinRoom
     *
     * Join a MUC room on a service.
     *
     * Parameters:
     *   (String) service - The name of the muc service.
     *   (String) room -  The name of the muc room.
     *   (String) nick -  The nick to use.
     *   (Dictionary) history -  (Optional) A history request dict. e.g.: {seconds: 10}.
     *   (Function) callback - (Optional) Called when the response arrives.
     *          No special processing on the response is performed.
     *
     * Returns:
     *   Iq id used to send the join request.
     */
    joinRoom : function(service, room, nick, callback, history){
        var presId = this._conn.getUniqueId("joinRoom");

        var pres = $pres({to: room + '@' + service + '/' + nick, id:presId});

        pres.c('x', {xmlns: Strophe.NS.MUC});

        if( history ){
            pres.c('history', history);
        }

        if( callback ){
            var muc = this;
            var callback_wrapper = function(presence){
                presence = Strophe.Mixin.apply(presence, muc.mixins.Presence);
                callback(presence);
            };
            this._conn.addHandler(callback_wrapper, null, 'presence', null,
                                  presId, null);
        }
        this._conn.send(pres.tree());
        return presId;
    },

    /** Function: leaveRoom
     *
     * Leave a MUC room on a service.
     *
     * Parameters:
     *   (String) service - The name of the muc service.
     *   (String) room -  The name of the muc room.
     *   (String) nick -  The nick that was used.
     *   (String) status -  (Optional) A status to set when leaving.
     *   (Function) callback - (Optional) Called when the response arrives.
     *          No special processing on the response is performed.
     *
     * Returns:
     *   Iq id used to send the leave request.
     */
    leaveRoom : function(service, room, nick, status, callback){
        var presId = this._conn.getUniqueId("leaveRoom");

        var pres = $pres({to: room + '@' + service + '/' + nick, id:presId,
                          type: 'unavailable'});
        if(status){
            pres.c('status', status);
        }

        if( callback ){
            this._conn.addHandler(callback, null, 'presence', null,
                                  presId, null);
        }

        this._conn.send(pres.tree());
        return presId;
    },

    /** Function: discoverRooms
     *
     * Discovers available rooms on the service.  Requires the disco plugin.
     *
     * Parameters:
     *   (String) service - The name of the muc service.
     *   (Function) callback - (Optional) Called when the disco response arrives.
     *          See disco plugin docs.
     *
     * Returns:
     *   Iq id used to send the leave request.
     */
    discoverRooms : function(service, callback){
        return this._conn.disco.discoverItems(service, null, callback);
    },


    /** Function: cancelHandler
     *
     * Cancels a previously registered handler
     *
     * Parameters:
     *   (Handler) handler - the handler to cancel
     */
    cancelHandler: function(handler){
        this._conn.deleteHandler(handler);
    },


    /** Function: registerHandlers
     *
     * Utility method to register multiple handlers for MUC events.
     *
     * Callbacks are:
     *  presence(pres):
     *     Registeres a notifier for MUC presences for the specified room/service.
     *     The callback gets the presence message.
     *       .hasStatus(status_code) - returns true if the status number is in the presence
     *       .getAffiliation() - returns affiliation
     *       .getJid() - returns jid: presence/x/item[jid]
     *       .getRole() - returns role: presence/x/item[role]
     *       .getNick() - returns nick: presence/x/item[nick]
     *       .getDestroyAlternate() - returns an alternate jid, if available, or null
     *       .getDestroyReason() - returns reason that room was destroyed, or ""
     *  roommessage(message):
     *     Registeres a message notifier for the room/service
     *     param to callback has the message as well as:
     *       .getDelayTime() - gets delay timestamp
     *       .getFromNick() - gets the nick of the sender
     *  privatemessage(message):
     *     Registeres a message notifier for the PMs for the room/service
     *     param to callback has the message as well as:
     *       .getFromNick() - gets the nick of the sender
     *  nickchanged(oldnick, newnick):
     *     Fired when nick is changing
     *  joined(pres):
     *     Fired when a user joined
     *  left(pres):
     *     Fired when a user left
     *  invited(message):
     *     Fired when a (non-direct) MUC invitation arrives (see InviteMessage mixin)
     *  kicked(pres):
     *     This user gets kicked from a room (see Prsence mixin)
     *  destroyed(pres):
     *     The room has been destroyed (see Prsence mixin)
     *
     * TODO: (as soon as I figure out a good API for this functionality):
     *  In addition to the callbacks, mixins can be specified with similar names
     *  to the callbacks.  I.e.: callback: 'presence', mixins: 'presence_mixins'.
     */
    registerHandlers: function(service, room_name, callbacks){
        callbacks = callbacks || {};
        var ret = {};

        var muc = this;

        if(callbacks.presence){
            var callback_wrapper_pres = function(pres){
                pres = Strophe.Mixin.apply(pres, muc.mixins.Presence);
                callbacks.presence(pres);

                return true; // keep handler registered
            };

            var from = null;
            if(room_name && service){
                from = room_name + "@" + service;
            }
            ret.presence = this._conn.addHandler(callback_wrapper_pres,
                                                 Strophe.NS.MUC_USER,
                                                 "presence", null, null,
                                                 from, {
                                                     matchBare: true
                                                 });
        }

        if(callbacks.nickchanged){
            ret.nickchanged = this.registerHandlers(service, room_name, {
                presence: function(pres){
                    if(pres.getType() == "unavailable"){
                        var is303 = pres.hasStatus(303);
                        if( is303 ){
                            var newnick = pres.getNick();
                            var oldnick = Strophe.getResourceFromJid(pres.getFrom());
                            callbacks.nickchanged(oldnick, newnick);
                        }
                    }
                }
            }).presence;
        }

        if(callbacks.joined){
            ret.joined = this.registerHandlers(service, room_name, {
                presence: function(pres){
                    if(pres.getType() != "unavailable"){
                        var isMine = pres.hasStatus(110) ||
                            $sp(pres).find("x > item").attr("jid").get(0) == muc._conn.jid;
                        if( !isMine ){
                            callbacks.joined(pres);
                        }
                    }
                }
            }).presence;
        }

        if(callbacks.left){
            ret.left = this.registerHandlers(service, room_name, {
                presence: function(pres){
                    if(pres.getType() == "unavailable"){
                        var isMine = pres.hasStatus(110) ||
                            pres.getJid() == muc._conn.jid;
                        if( !isMine ){
                            callbacks.left(pres);
                        }
                    }
                }
            }).presence;
        }

        if(callbacks.kicked){
            ret.kicked = this.registerHandlers(service, null, {
                presence: function(pres){
                    if(pres.getType() == "unavailable" &&
                       (pres.hasStatus(307) || //pres.hasStatus(321) ||
                        pres.getJid() == muc._conn.jid) &&
                       pres.getRole() == "none"){
                        callbacks.kicked(pres);
                    }
                }
            }).presence;
        }

        if(callbacks.destroyed){
            ret.destroyed = this.registerHandlers(service, null, {
                presence: function(pres){
                    if(// pres.getType() == "unavailable" &&
                       // pres.getRole() == "none" &&
                       $sp(pres).find("x > destroy").length > 0){
                        callbacks.destroyed(pres);
                    }
                }
            }).presence;
        }


        if(callbacks.roommessage){
            var callback_wrapper_rm = function(message){
                message = Strophe.Mixin.apply(message, muc.mixins.GroupMessage);

                callbacks.roommessage(message);

                return true; // keep handler alive
            };

            ret.roommessage = this._conn.addHandler(callback_wrapper_rm, null,
                                                    "message", "groupchat",
                                                    null,
                                                    room_name + "@" + service,
                                                    {matchBare: true});
        }

        if(callbacks.privatemessage){
            var callback_wrapper_pm = function(message){
                message = Strophe.Mixin.apply(message, muc.mixins.PrivateMessage);
                callbacks.privatemessage(message);

                return true; // keep handler alive
            };

            ret.privatemessage = this._conn.addHandler(callback_wrapper_pm, null,
                                                       "message", "chat",
                                                       null,
                                                       room_name + "@" + service,
                                                       {matchBare: true});
        }

        if(callbacks.invited){
            var callback_wrapper_i = function(message){
                if($sp(message).find(Strophe.NS.MUC_USER + "|x > invite").get(0)){
                    // tell the invite message what its connection is so that it
                    // can decline()
                    message = Strophe.Mixin.apply(message, muc.mixins.InviteMessage,
                                                  {connection: muc._conn});
                    callbacks.invited(message);
                }

                return true; // keep handler alive
            };

            ret.invited = this._conn.addHandler(callback_wrapper_i,
                                                Strophe.NS.MUC_USER,
                                                "message");
        }

        return ret;
    },

    mixins: {
        Presence: Strophe.Mixin.apply({
            hasStatus: function(status_code){
                status_code = status_code + "";
                var x = this.getExtensionsByNS(Strophe.NS.MUC_USER)[0];
                if(x){
                    var statuses = x.getElementsByTagName("status");
                    for(var i = 0; i < statuses.length; i++){
                        if(statuses[i].getAttribute("code") == status_code){
                            return true;
                        }
                    }
                }

                return false;
            },
            getRoomJid: function(){
                return Strophe.getBareJidFromJid(this.getFrom());
            },
            getRoomName: function(){
                return Strophe.getNodeFromJid(this.getFrom());
            },
            getFromNick: function(){
                return Strophe.getResourceFromJid(this.getFrom());
            },
            getAffiliation: function(){
                return this.getExtensionsByNS(Strophe.NS.MUC_USER)
                    .find("item").attr("affiliation").get(0);
            },
            getJid: function(){
                return this.getExtensionsByNS(Strophe.NS.MUC_USER)
                    .find("item").attr("jid").get(0);
            },
            getRole: function(){
                return this.getExtensionsByNS(Strophe.NS.MUC_USER)
                    .find("item").attr("role").get(0);
            },
            getNick: function(){
                return this.getExtensionsByNS(Strophe.NS.MUC_USER)
                    .find("item").attr("nick").get(0);
            },
            // used for kicks
            getActor: function(){
                // new style parsing!
                return $sp(this).find(Strophe.NS.MUC_USER + "|x > item > actor").text();
            },
            // used in destroy notifications
            getDestroyAlternate: function(){
                return $sp(this).find(Strophe.NS.MUC_USER + "|x > destroy").attr("jid").get(0);
            },
            getDestroyReason: function(){
                return $sp(this).find(Strophe.NS.MUC_USER + "|x > destroy > reason").text();
            }
        }, Strophe.Mixin.Presence),


        GroupMessage: Strophe.Mixin.apply({
            getDelayTime: function(){
                $sp(this).find("urn:xmpp:delay|delay").attr("stamp").get(0);
            },
            getDelayFrom: function(){
                $sp(this).find("urn:xmpp:delay|delay").attr("from").get(0);
            },
            getFromNick: function(){
                return Strophe.getResourceFromJid(this.getFrom());
            }
        }, Strophe.Mixin.Message),


        PrivateMessage: Strophe.Mixin.apply({
            getFromNick: function(){
                return Strophe.getResourceFromJid(this.getFrom());
            }
        }, Strophe.Mixin.Message),


        InviteMessage: Strophe.Mixin.apply({
            getInvitedBy: function(){
                return $sp(this).find(Strophe.NS.MUC_USER + "|x > invite").attr("from");
            },
            getReason: function(){
                return $sp(this).find(Strophe.NS.MUC_USER + "|x > invite > reason").text();
            },
            getPassword: function(){
                return $sp(this).find(Strophe.NS.MUC_USER + "|x > invite > password").text();
            },

            // helper function to decline invitation
            decline: function(reason){
                if(this.connection){
                    var reply = $msg({to: this.getFrom()}).c("x", {xmlns:Strophe.NS.MUC_USER})
                        .c("decline", {to: Strophe.getBareJidFromJid(this.getInviteFrom())});

                    if(reason){
                        reply.c("reason").t(reason);
                    }

                    this.connection.send(reply);
                    return true;
                }
                return false;
            }

        }, Strophe.Mixin.Message)
    }
});

})(Strophe.Parser);

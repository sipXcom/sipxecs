/*
 * Copyright (c) eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */

#ifndef V8ENGINE_H_INCLUDED
#define	V8ENGINE_H_INCLUDED


#include <sipxproxy/AuthPlugin.h>
#include <boost/noncopyable.hpp>


class SipRouter;

class V8Engine : public AuthPlugin, private boost::noncopyable
{
public:
  V8Engine(const UtlString& instanceName);
  
  virtual ~V8Engine();
  
  AuthResult authorizeAndModify(const UtlString& id,    /**< The authenticated identity of the
                                                             *   request originator, if any (the null
                                                             *   string if not).
                                                             *   This is in the form of a SIP uri
                                                             *   identity value as used in the
                                                             *   credentials database (user@domain)
                                                             *   without the scheme or any parameters.
                                                             */
                                    const Url&  requestUri, ///< parsed target Uri
                                    RouteState& routeState, ///< the state for this request.  
                                    const UtlString& method,///< the request method
                                    AuthResult  priorResult,///< results from earlier plugins.
                                    SipMessage& request,    ///< see AuthPlugin regarding modifying
                                    bool bSpiralingRequest, ///< request spiraling indication 
                                    UtlString&  reason      ///< rejection reason
                                    );

   /// Read (or re-read) the authorization rules.
   virtual void readConfig( OsConfigDb& configDb /**< a subhash of the individual configuration
                                                  * parameters for this instance of this plugin. */
                           );
   /**<
    * @note
    * The parent service may call the readConfig method at any time to
    * indicate that the configuration may have changed.  The plugin
    * should reinitialize itself based on the configuration that exists when
    * this is called.  The fact that it is a subhash means that whatever prefix
    * is used to identify the plugin (see PluginHooks) has been removed (see the
    * examples in PluginHooks::readConfig).
    */

   virtual void announceAssociatedSipRouter( SipRouter* sipRouter );
   
   /// Boolean indicator that returns true if the plugin wants to process requests
   /// that requires no authentication
   virtual bool willModifyTrustedRequest() const;
   
   /// This method is called by the proxy if willModifyRequest() flag is set to true
   /// giving this plugin the opportunity to modify the request even if it requires
   /// no authentication
   virtual void modifyTrustedRequest(
                                    const Url&  requestUri,  ///< parsed target Uri
                                    SipMessage& request,     ///< see below regarding modifying this
                                    bool bSpiralingRequest  ///< true if request is still spiraling through pr
                                    );
   
   /// This method is called by the proxy if willModifyResponse is set to true
   /// giving the plugin to modify responses before they get relayed
   virtual void modifyFinalResponse(
     SipTransaction* pTransaction, 
     const SipMessage& request, 
     SipMessage& finalResponse);
   
   /// Boolean indicator that returns true if the plugin wants to process final responses
   virtual bool willModifyFinalResponse() const;
  
private:
  SipRouter* _pSipRouter;
};

//
// Inlines
//

inline bool V8Engine::willModifyTrustedRequest() const
{
  return true;
}

inline bool V8Engine::willModifyFinalResponse() const
{
  return true;
}

inline void V8Engine::announceAssociatedSipRouter( SipRouter* pSipRouter )
{
  _pSipRouter = pSipRouter;
}

#endif	// V8ENGINE_H_INCLUDED


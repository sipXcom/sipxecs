//
//
// Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
// $$
//////////////////////////////////////////////////////////////////////////////

#ifndef SIPREDIRECTORAUTHROUTER_H
#define SIPREDIRECTORAUTHROUTER_H

// SYSTEM INCLUDES

// APPLICATION INCLUDES
#include "registry/RedirectPlugin.h"

// DEFINES
// MACROS
// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STRUCTS
// TYPEDEFS
// FORWARD DECLARATIONS

/** The SipRedirectorAuthRouter is there to ensure that every
 *  Contact header present in a 3xx redirection response contains
 *  a Route header parameter pointing to the sipXproxy.  Such
 *  a header parameter is required to guarantee that the sipXproxy
 *  component receives each and every INVITE that gets generated by
 *  the sipXtack as a result of processing the 3xx redirection
 *  response.  The sipXproxy will perform forwarding and
 *  authorization on each INVITE before sending them along.
 *  If it wasn't for the SipRedirectorAuthRouter, INVITEs
 *  resulting from the processing of 3xx response would
 *  get directly sent by the sipXtack to the request
 *  target therefore bypassing the sipXproxy forwarding
 *  and authorization functions.  This would result in
 *  erratic behavior and priviledged calls being completed
 *  by non-priviledged users.
 *
 *  To avoid these problems, the SipRedirectorAuthRouter
 *  adds a Route header pointing to sipXproxy to every
 *  Contact header contained in the 3xx response with the
 *  exception of Contacts that already contain a properly
 *  signed Route header pointing to a sipXproxy.  This
 *  ensures that INVITEs are routed to the auth proxy function
 *  of the sipXproxy component by sipXtack instead of being sent
 *  directly to the request target.
 */
class SipRedirectorAuthRouter : public RedirectPlugin
{
public:

   explicit SipRedirectorAuthRouter(const UtlString& instanceName);

   ~SipRedirectorAuthRouter();

   virtual void readConfig(OsConfigDb& configDb);

   virtual OsStatus initialize(OsConfigDb& configDb,
                               int redirectorNo,
                               const UtlString& localDomainHost);

   virtual void finalize();

   virtual RedirectPlugin::LookUpStatus lookUp(
      const SipMessage& message,
      const UtlString& requestString,
      const Url& requestUri,
      const UtlString& method,
      ContactList& contactList,
      RequestSeqNo requestSeqNo,
      int redirectorNo,
      SipRedirectorPrivateStorage*& privateStorage,
      ErrorDescriptor& errorDescriptor);

   virtual const UtlString& name( void ) const;

protected:

   UtlString mAuthUrl;

   // String to use in place of class name in log messages:
   // "[instance] class".
   UtlString mLogName;

private:

};

#endif // SIPREDIRECTORAUTHROUTER_H

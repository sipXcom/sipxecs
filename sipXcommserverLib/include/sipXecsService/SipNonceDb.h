//
// Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
//
////////////////////////////////////////////////////////////////////////
#ifndef _SipNonceDb_h_
#define _SipNonceDb_h_

// SYSTEM INCLUDES

// APPLICATION INCLUDES
#include "utl/UtlString.h"
#include "os/OsBSem.h"
#include "sipXecsService/SharedSecret.h"

// DEFINES
// MACROS
// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STRUCTS
// TYPEDEFS
// FORWARD DECLARATIONS


/// Create a nonce recognizable as having been generated by this cluster.
class SipNonceDb
{
/* //////////////////////////// PUBLIC //////////////////////////////////// */
public:

/* ============================ CREATORS ================================== */

   SipNonceDb();

   virtual
   ~SipNonceDb();

/* ============================ MANIPULATORS ============================== */

   /// Generate a nonce value based on the current call.
   void createNewNonce(const UtlString& callId, //input
                       const UtlString& fromTag, // input
                       const UtlString& realm, // input
                       UtlString& nonce); // output

/* ============================ ACCESSORS ================================= */

/* ============================ INQUIRY =================================== */

   /// Validate that a presented nonce was generated by this system.
   UtlBoolean isNonceValid(const UtlString& nonce,
                          const UtlString& callId,
                          const UtlString& fromTag,
                          const UtlString& realm,
                          const long expiredTime);

/* //////////////////////////// PROTECTED ///////////////////////////////// */
protected:

/* //////////////////////////// PRIVATE /////////////////////////////////// */
private:
   /// Generate a signature for a given set of inputs
   UtlString nonceSignature(const UtlString& callId,
                            const UtlString& fromTag,
                            const UtlString& realm,
                            const char*      timestamp
                            );

   SharedSecret* mpNonceSignatureSecret;

   // @cond INCLUDENOCOPY
   SipNonceDb(const SipNonceDb& rSipNonceDb);
   SipNonceDb& operator=(const SipNonceDb& rhs);
   // @endcond
};

/// A shared singleton instance of SipNonceDb.
class SharedNonceDb : public SipNonceDb
{
  public:

   /// Get the singleton shared nonce database.
   static SipNonceDb* get();

  private:

   static OsBSem*     spLock;
   static SipNonceDb* spSipNonceDb;

   /// Hidden constructor for singleton
   SharedNonceDb();

   /// Hidden destructor
   ~SharedNonceDb();
};

#endif  // _SipNonceDb_h_

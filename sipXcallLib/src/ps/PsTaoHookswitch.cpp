//
// Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
//
// $$
////////////////////////////////////////////////////////////////////////
//////


// SYSTEM INCLUDES
#include <assert.h>

// APPLICATION INCLUDES
#include "ps/PsTaoHookswitch.h"
#include <os/OsLock.h>

// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STATIC VARIABLE INITIALIZATIONS

/* //////////////////////////// PUBLIC //////////////////////////////////// */

/* ============================ CREATORS ================================== */

// Constructor
PsTaoHookswitch::PsTaoHookswitch()
{
}

PsTaoHookswitch::PsTaoHookswitch(const UtlString& rComponentName, int componentType) :
PsTaoComponent(rComponentName, componentType)
{
}

// Copy constructor
PsTaoHookswitch::PsTaoHookswitch(const PsTaoHookswitch& rPsTaoHookswitch)
{
}

// Destructor
PsTaoHookswitch::~PsTaoHookswitch()
{
}

/* ============================ MANIPULATORS ============================== */

// Assignment operator
PsTaoHookswitch&
PsTaoHookswitch::operator=(const PsTaoHookswitch& rhs)
{
   if (this == &rhs)            // handle the assignment to self case
      return *this;

   return *this;
}

/* ============================ ACCESSORS ================================= */
/* ============================ INQUIRY =================================== */

/* //////////////////////////// PROTECTED ///////////////////////////////// */
/* //////////////////////////// PRIVATE /////////////////////////////////// */

/* ============================ FUNCTIONS ================================= */

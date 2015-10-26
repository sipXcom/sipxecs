//
//
// Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
// $$
//////////////////////////////////////////////////////////////////////////////
#include "registry/RedirectPlugin.h"

// SYSTEM INCLUDES

// APPLICATION INCLUDES
#include <utl/UtlRegex.h>
#include <net/SipMessage.h>
#include "os/OsDateTime.h"
#include "os/OsFS.h"
#include "sipdb/ResultSet.h"
#include "registry/SipRedirectServer.h"
#include <boost/algorithm/string.hpp>

// DEFINES
#define UNINITIALIZED_WARNING_CODE  (-1)

// MACROS
// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS

const char* RedirectPlugin::Prefix  = "SIP_REDIRECT";
const char* RedirectPlugin::Factory = "getRedirectPlugin";

// STRUCTS
// TYPEDEFS
// FORWARD DECLARATIONS

// Null default destructor.
RedirectPlugin::~RedirectPlugin()
{
}

void RedirectPlugin::observe(
   const SipMessage& message,
   const UtlString& requestString,
   const Url& requestUri,
   const UtlString& method,
   const ContactList& contactList,
   RequestSeqNo requestSeqNo,
   int redirectorNo )
{
   Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "RedirectPlugin::observe() [%s] called for %s",
                             name().data(),
                             requestString.data() );
}

// Null default cancel() implementation.
void RedirectPlugin::cancel(RequestSeqNo request)
{
}

// Null default readConfig() implementation
void
RedirectPlugin::readConfig(OsConfigDb& configDb)
{
}

void RedirectPlugin::resumeRedirection(RequestSeqNo request,
                                       int redirector)
{
   SipRedirectServer::getInstance()->resumeRequest(request, redirector);
}

SipRedirectorPrivateStorage::~SipRedirectorPrivateStorage()
{
}

ErrorDescriptor::ErrorDescriptor() :
   mStatusCode             ( SIP_FORBIDDEN_CODE ),
   mReasonPhrase           ( SIP_FORBIDDEN_TEXT ),
   mWarningCode            ( UNINITIALIZED_WARNING_CODE ),
   mAppendRequestToResponse( false )
{
}

ErrorDescriptor::~ErrorDescriptor()
{
   mOptionalFieldsValues.destroyAll();
}

bool ErrorDescriptor::setStatusLineData( const int statusCode, const UtlString& reasonPhrase )
{
   bool result = false;
   if( statusCode >= SIP_4XX_CLASS_CODE && statusCode < SIP_7XX_CLASS_CODE )
   {
      mStatusCode   = statusCode;
      mReasonPhrase = reasonPhrase;
      result = true;
   }
   else
   {
      Os::Logger::instance().log(FAC_SIP, PRI_CRIT, "ErrorDescriptor::setStatusLineData(): redirector supplied invalid status code: %d", statusCode );
   }
   return result;
}

bool ErrorDescriptor::setWarningData( const int warningCode, const UtlString& warningText )
{
   bool result = false;
   if( warningCode >= SIP_WARN_INCOMPAT_PROTO_CODE && warningCode <= SIP_WARN_MISC_CODE )
   {
      mWarningCode = warningCode;
      mWarningText = warningText;
      result = true;
   }
   else
   {
      Os::Logger::instance().log(FAC_SIP, PRI_CRIT, "ErrorDescriptor::setWarningData(): redirector supplied invalid warning code: %d", warningCode );
   }
   return result;
}

void ErrorDescriptor::setRetryAfterFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_RETRY_AFTER_FIELD, fieldValue );
}

void ErrorDescriptor::setRequireFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_REQUIRE_FIELD, fieldValue );
}

void ErrorDescriptor::setUnsupportedFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_UNSUPPORTED_FIELD, fieldValue );
}

void ErrorDescriptor::setAllowFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_ALLOW_FIELD, fieldValue );
}

void ErrorDescriptor::setAcceptFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_ACCEPT_FIELD, fieldValue );
}

void ErrorDescriptor::setAcceptEncodingFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_ACCEPT_ENCODING_FIELD, fieldValue );
}

void ErrorDescriptor::setAcceptLanguageFieldValue( const UtlString& fieldValue )
{
   setOptionalFieldValue( SIP_ACCEPT_LANGUAGE_FIELD, fieldValue );
}

void ErrorDescriptor::setOptionalFieldValue( const UtlString& fieldName, const UtlString& fieldValue )
{
   mOptionalFieldsValues.destroy( &fieldName );
   mOptionalFieldsValues.insertKeyAndValue( new UtlString( fieldName ), new UtlString( fieldValue ) );
}

void ErrorDescriptor::appendRequestToResponse( void )
{
   mAppendRequestToResponse = true;
}

void ErrorDescriptor::dontAppendRequestToResponse( void )
{
   mAppendRequestToResponse = false;
}

void ErrorDescriptor::getStatusLineData( int& statusCode, UtlString& reasonPhrase ) const
{
   statusCode   = mStatusCode;
   reasonPhrase = mReasonPhrase;
}

bool ErrorDescriptor::getWarningData( int& warningCode, UtlString& warningText ) const
{
   bool result = isWarningDataSet();
   if( result )
   {
      warningCode = mWarningCode;
      warningText = mWarningText;
   }
   return result;
}

bool ErrorDescriptor::getRetryAfterFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_RETRY_AFTER_FIELD, fieldValue );
}

bool ErrorDescriptor::getRequireFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_REQUIRE_FIELD, fieldValue );
}

bool ErrorDescriptor::getUnsupportedFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_UNSUPPORTED_FIELD, fieldValue );
}

bool ErrorDescriptor::getAllowFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_ALLOW_FIELD, fieldValue );
}

bool ErrorDescriptor::getAcceptFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_ACCEPT_FIELD, fieldValue );
}

bool ErrorDescriptor::getAcceptEncodingFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_ACCEPT_ENCODING_FIELD, fieldValue );
}

bool ErrorDescriptor::getAcceptLanguageFieldValue( UtlString& fieldValue ) const
{
   return getOptinalFieldValue( SIP_ACCEPT_LANGUAGE_FIELD, fieldValue );
}

bool ErrorDescriptor::getOptinalFieldValue( const UtlString& fieldName, UtlString& fieldValue ) const
{
   bool result = false;
   UtlString* pReturnedValue;

   pReturnedValue = dynamic_cast<UtlString*>(mOptionalFieldsValues.findValue( &fieldName ) );
   if( pReturnedValue )
   {
      fieldValue = *pReturnedValue;
      result = true;
   }
   return result;
}

bool ErrorDescriptor::isWarningDataSet( void ) const
{
   return mWarningCode != UNINITIALIZED_WARNING_CODE;
}

bool ErrorDescriptor::shouldRequestBeAppendedToResponse( void ) const
{
   return mAppendRequestToResponse;
}

ContactList::ContactList( 
  const UtlString& requestString, 
  EntityDB* pEntityDb,
  const std::string& callerLocation,
  const std::string& fallbackLocation) :
   mRequestString( requestString ),
   mbListWasModified( false ),
   _pEntityDb(pEntityDb),
   _callerLocation(callerLocation),
   _fallbackLocation(fallbackLocation),
   _isTrustedLocation(false),
   _hasProcessedLocation(false)
{
  if (!_callerLocation.empty())
  {
    boost::to_lower(_callerLocation);
    _callerLocation.erase(std::remove_if(_callerLocation.begin(), _callerLocation.end(), isspace), _callerLocation.end());
    boost::split(_callerLocationTokens, _callerLocation, boost::is_any_of(","), boost::token_compress_on);
  }
}

bool ContactList::isAllowedLocation(const UtlString& contact, const RedirectPlugin& plugin)
{
  if (_isTrustedLocation || _callerLocationTokens.empty())
  {
    OS_LOG_INFO(FAC_SIP, "ContactList::isAllowedLocation() No location restriction defined for contact " << contact.data() << " added by plugin " << plugin.name().data());
    return true;
  }
  
  //
  // Check if we have already verified the location for this contact list
  //  Simply allow or disallow further additions based on previous results
  //
  if (_hasProcessedLocation)
  {
    if (!_isTrustedLocation)
    {
      OS_LOG_WARNING(FAC_SIP, "ContactList::isAllowedLocation() REJECTED insertion of contact " << contact.data() << " added by plugin " << plugin.name().data() << " because of location restrictions");
    }
    return _isTrustedLocation;
  }
  _hasProcessedLocation = true;
    
  Url requestUri(mRequestString, TRUE); // request-uri is an addr-spec
  Url contactUri(contact, FALSE); // contact-uri is an name-addr
  
  UtlString host;
  UtlString user;
  UtlString contactUser;
  UtlString contactHost;
  std::ostringstream identity;
  std::string domain = SipRedirectServer::getInstance()->sipUserAgent()->getDomain();
   
  requestUri.getHostAddress(host);
  requestUri.getUserId(user);
  contactUri.getUserId(contactUser);
  contactUri.getHostAddress(contactHost);
  
  //
  //  Make sure we use the actual domain for domain aliases or we wont match identities correctly
  //
  if ( host.compareTo(domain.c_str()) != 0 && SipRedirectServer::getInstance()->sipUserAgent()->isMyHostAlias(requestUri))
  {
    host = domain.c_str();
  }
  
  //
  // Note:  Fallback and Mapping rules may return contacts that will not be routed
  // back to the registrar.  We therefore won't have any chance to evaluate the
  // location of those contacts.  So far, we have identified gateways and * codes
  // behave this way.  We therefore add some custom hack to evaluate based on the contact
  // returned.
  // Uncomment the block below to bring back new locations handling for gateways
#if 0
  if (mRequestString.first("sipxecs-lineid") != UtlString::UTLSTRING_NOT_FOUND)
  {
    //
    // Gateway identity does not have a user
    //
    UtlString lineId;
    requestUri.getUrlParameter("sipxecs-lineid", lineId, 0);
    identity << host.data() << ";" << "sipxecs-lineid=" << lineId.data();
  }
  else if (contact.first("sipxecs-lineid") != UtlString::UTLSTRING_NOT_FOUND)
  {
    //
    // This is a gateway call returned by the fallback redirector.  Gateway identity does not have a user
    //
    UtlString lineId;
    contactUri.getUrlParameter("sipxecs-lineid", lineId, 0);
    identity << contactHost.data() << ";" << "sipxecs-lineid=" << lineId.data();
  }

#endif
  if (user.first('*') == 0)
  {
    //
    // This is a star code.  It won't match any identity so let us use the user of the contact returned by mapping rules
    //
    identity << contactUser.data() << "@" << host.data();
  }
  else
  {
    identity << user.data() << "@" << host.data();
  }
  
  EntityRecord entity;
  
  
  //
  // We check both actual identity and aliases to match the location.
  //
  if (!_pEntityDb->findByIdentity(identity.str(), entity))
  {
    OS_LOG_INFO(FAC_SIP, "ContactList::isAllowedLocation() - did not match any location restriction for user identity " << identity.str());
    if (!_pEntityDb->findByAliasIdentity(identity.str(), entity))
    {
      OS_LOG_INFO(FAC_SIP, "ContactList::isAllowedLocation() - did not match any location restriction for alias identity " << identity.str());
      _isTrustedLocation = true;
      return true;
    }
  }
    
  
  if (entity.allowedLocations().empty())
  {
    //
    // no location specified
    //
    OS_LOG_INFO(FAC_SIP, "ContactList::isAllowedLocation() No location restriction defined for contact " << contact.data() << " added by plugin " << plugin.name().data());
    _isTrustedLocation = true;
    return true;
  }
  
  bool foundMatch = false;
  for (std::set<std::string>::iterator iter = entity.allowedLocations().begin(); iter != entity.allowedLocations().end(); iter++)
  {
    std::string loc = *iter;
    boost::trim(loc);
    boost::to_lower(loc);
    
    if (_callerLocationTokens.find(loc) != _callerLocationTokens.end())
    {
      OS_LOG_INFO(FAC_SIP, "ContactList::isAllowedLocation() found matching location " << loc << " for contact " << contact.data() << " added by plugin " << plugin.name().data());
      foundMatch = true;
      break;
    }
    else
    {
      OS_LOG_DEBUG(FAC_SIP, "ContactList::isAllowedLocation() - location " << loc << " for contact " << contact.data() << " does not match any item in " << _callerLocation);
    }
  }
  
  _isTrustedLocation = foundMatch;
  
  if (!_isTrustedLocation)
  {
    OS_LOG_WARNING(FAC_SIP, "ContactList::isAllowedLocation() REJECTED insertion of contact " << contact.data() << " added by plugin " << plugin.name().data() << " because of location restrictions");
  }
    
  return foundMatch;
}

bool ContactList::add( const Url& contactUrl, const RedirectPlugin& plugin )
{
   return add( contactUrl.toString(), plugin );
}

bool ContactList::add( const UtlString& contact, const RedirectPlugin& plugin )
{
   if (!isAllowedLocation(contact, plugin))
   {
    OS_LOG_WARNING(FAC_SIP, "ContactList::add() revoked insertion of contact " << contact << " from " << plugin.name().data() 
      << " Invalid Caller Location: " << _callerLocation << " for destination " << contact.data());
     return false;
   }
   
   mbListWasModified = true;
   mContactList.push_back( contact );
   Os::Logger::instance().log(FAC_SIP, PRI_NOTICE, "ContactList::add(): %s added contact for '%s':\n"
                             "   '%s' (contact index %zu)",
                             plugin.name().data(),
                             mRequestString.data(),
                             contact.data(),
                             mContactList.size() - 1 );
   return true;
}

bool ContactList::set( size_t index, const Url& contactUrl, const RedirectPlugin& plugin )
{
   return set( index, contactUrl.toString(), plugin );
}

bool ContactList::set( size_t index, const UtlString& contact, const RedirectPlugin& plugin )
{
   bool success = false;
   if( index < mContactList.size() )
   {
      mbListWasModified = true;
      success = true;

      Os::Logger::instance().log(FAC_SIP, PRI_NOTICE, "ContactList::set(): %s modified contact index %zu for '%s':\n"
                                "   was:    '%s'\n"
                                "   now is: '%s'",
                                plugin.name().data(),
                                index,
                                mRequestString.data(),
                                mContactList[ index ].data(),
                                contact.data() );
      mContactList[ index ] = contact;
   }
   else
   {
      Os::Logger::instance().log(FAC_SIP, PRI_ERR, "ContactList::set(): %s failed to set contact index %zu - list only has %zu elements",
                                plugin.name().data(),
                                index,
                                mContactList.size() );
   }
   return success;
}

bool ContactList::get( size_t index, UtlString& contact ) const
{
   bool success = false;
   if( index < mContactList.size() )
   {
      contact = mContactList[ index ];
      success = true;
   }
   else
   {
      Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "ContactList::get(): plugin failed to get contact index %zu - list only has %zu elements",
                                index,
                                mContactList.size() );
   }
   return success;
}

bool ContactList::get( size_t index, Url& contactUrl ) const
{
   UtlString contactAsString;
   bool success = get( index, contactAsString );
   if( success )
   {
      contactUrl.fromString( contactAsString );
   }
   return success;
}

bool ContactList::remove( size_t index, const RedirectPlugin& plugin )
{
   bool success = false;
   if( index < mContactList.size() )
   {
      success = true;
      mbListWasModified = true;

      Os::Logger::instance().log(FAC_SIP, PRI_NOTICE, "ContactList::remove(): %s removed contact index %zu  for '%s':\n"
                                        "   was:    '%s'",
                                plugin.name().data(),
                                index,
                                mRequestString.data(),
                                mContactList[ index ].data() );

      mContactList.erase( mContactList.begin() + index );
   }
   else
   {
      Os::Logger::instance().log(FAC_SIP, PRI_ERR, "ContactList::remove(): %s failed to remove contact index %zu - list only has %zu elements",
                                plugin.name().data(),
                                index,
                                mContactList.size() );
   }
   return success;
}

bool ContactList::removeAll( const RedirectPlugin& plugin )
{
   Os::Logger::instance().log(FAC_SIP, PRI_NOTICE, "ContactList::removeAll(): %s removed %zu contacts for '%s'",
                             plugin.name().data(),
                             mContactList.size(),
                             mRequestString.data() );

   mbListWasModified = true;
   mContactList.clear();
   return true;
}

void ContactList::touch( const RedirectPlugin& plugin )
{
   Os::Logger::instance().log(FAC_SIP, PRI_NOTICE, "ContactList::touch(): list touched by %s",
                             plugin.name().data() );
   mbListWasModified = true;
}

size_t ContactList::entries( void ) const
{
   return mContactList.size();
}

void ContactList::resetWasModifiedFlag( void )
{
   mbListWasModified = false;
}

bool ContactList::wasListModified( void ) const
{
   return mbListWasModified;
}

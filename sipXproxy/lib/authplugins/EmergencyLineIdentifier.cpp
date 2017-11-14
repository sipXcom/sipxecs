// Copyright (c) eZuce, Inc. All rights reserved.
// Contributed to SIPfoundry under a Contributor Agreement
//
// This software is free software; you can redistribute it and/or modify it under
// the terms of the Affero General Public License (AGPL) as published by the
// Free Software Foundation; either version 3 of the License, or (at your option)
// any later version.
//
// This software is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
// details.




#include <cassert>
#include <string>
#include <boost/thread.hpp>

#include "EmergencyDB.h"
#include "EmergencyLineIdentifier.h"
#include "sipXecsService/SipXecsService.h"
#include "digitmaps/EmergencyRulesUrlMapping.h"
#include "os/OsLogger.h"
#include <net/NetAttributeTokenizer.h>

const char DEFAULT_EMERG_RULES_FILENAME[] = "authrules.xml";
const char EMERGRULES_FILENAME_CONFIG_PARAM[] = "EMERGRULES";
const char E911LINE[] = "X-Sipx-Emergency-Identifier";

typedef boost::mutex mutex;
typedef boost::lock_guard<boost::mutex> mutex_lock;


static AuthPlugin* gpInstance = 0;
static mutex gInstanceMutex;
static EmergencyRulesUrlMapping* gpEmergencyRules = 0;

static EmergencyDB* gpEmergency = 0;

extern "C" AuthPlugin* getAuthPlugin(const UtlString& pluginName)
{
  mutex_lock lock(gInstanceMutex);
  if (!gpInstance)
  {
    assert(!gpEmergency);
    assert(!gpEmergencyRules);
    MongoDB::ConnectionInfo info = MongoDB::ConnectionInfo::globalInfo();
    gpEmergency = new EmergencyDB(info);
    return new EmergencyLineIdentifier(pluginName);
  }
  else
  {
    return gpInstance;
  }
}

EmergencyLineIdentifier::EmergencyLineIdentifier(const UtlString& pluginName)
  : AuthPlugin(pluginName)
{
}

EmergencyLineIdentifier::~EmergencyLineIdentifier()
{
  delete gpEmergency;
}

void EmergencyLineIdentifier::readConfig(OsConfigDb& configDb)
{
  if (gpEmergencyRules)
    delete gpEmergencyRules;
  gpEmergencyRules = new EmergencyRulesUrlMapping();

  UtlString fileName;
  if (!configDb.get(EMERGRULES_FILENAME_CONFIG_PARAM, fileName))
  {
    OsPath defaultPath =
    SipXecsService::Path(SipXecsService::ConfigurationDirType, DEFAULT_EMERG_RULES_FILENAME);
    fileName = SipXecsService::Path(SipXecsService::ConfigurationDirType, DEFAULT_EMERG_RULES_FILENAME);
  }

  if (OS_SUCCESS != gpEmergencyRules->loadMappings(fileName))
  {
    OS_LOG_ERROR(FAC_SIP, "E911: Unable to load " << fileName.data() << ".  Emergency Line Identifier feature will be DISABLED!");
    delete gpEmergencyRules;
    gpEmergencyRules = 0;
    return;
  }

  OS_LOG_NOTICE(FAC_SIP, "E911: Emergency Line Identifier successfully loaded " << fileName.data());
}

AuthPlugin::AuthResult EmergencyLineIdentifier::authorizeAndModify(const UtlString& id,
  const Url&  requestUri,
  RouteState& routeState,
  const UtlString& method,
  AuthResult  priorResult,
  SipMessage& request,
  bool bSpiralingRequest,
  UtlString&  reason
)
{
  if (!gpEmergencyRules || !gpEmergency)
    return AuthPlugin::CONTINUE;

  UtlString nameStr;
  UtlString descriptionStr;
  std::string e911;
  std::string address;
  std::string location;
  if (gpEmergencyRules->getMatchedRule(requestUri, nameStr, descriptionStr))
  {
    std::string instrument;
    if (!request.getHeaderValue( 0, E911LINE))
    {
      //
      // Check if the instrument identifier is in the database
      //

      if (getInstrument(request, instrument) && gpEmergency->findE911InstrumentIdentifier(instrument, e911, address, location))
      {

        request.setHeaderValue( E911LINE, e911.c_str(), 0 );
        OS_LOG_NOTICE(FAC_SIP, "E911: Setting Line Identifier for user " << id.data() << "/" << instrument << " to " << e911);
      }
      else if (gpEmergency->findE911LineIdentifier(id.data(), e911, address, location))
      {
        request.setHeaderValue( E911LINE, e911.c_str(), 0 );
        OS_LOG_NOTICE(FAC_SIP, "E911: Setting Line Identifier for user " << id.data() << " to " << e911);
      }
      else
      {
        OS_LOG_NOTICE(FAC_SIP, "E911: No Line Identifier configured for " << id.data());
      }

    }

    //
    // Send out an alarm
    //
    UtlString fromField;
    UtlString fromLabel;
    UtlString contactField;
    request.getFromUri(&fromField);
    request.getFromLabel(&fromLabel);
    request.getContactUri(0, &contactField);

    if (e911.empty())
    {
      OS_LOG_EMERGENCY(FAC_ALARM, "ALARM_PROXY_EMERG_NUMBER_DIALED Emergency dial rule "
        << nameStr.data()
        << " (" <<   descriptionStr.data() << ") "
        << "was invoked by " << fromLabel.data()
        << "<" << fromField.data() << ">"
        << " Contact: " << contactField.data()
        << " MAC: " << instrument);
    }
    else
    {
      OS_LOG_EMERGENCY(FAC_ALARM, "ALARM_PROXY_EMERG_NUMBER_DIALED Emergency dial rule "
        << nameStr.data()
        << " (" <<   descriptionStr.data() << ") "
        << "was invoked by " << fromLabel.data()
        << "<" << fromField.data() << ">"
        << " Contact: " << contactField.data()
        << " ELIN: " << e911
        << " Addr-Info: " << address << ""
        << " Location: " << location << ""
        << " MAC: " << instrument);
    }
  }


  if (!bSpiralingRequest)
  {
    const char* identifier = request.getHeaderValue( 0, E911LINE);
    if (identifier)
    {
      OS_LOG_NOTICE(FAC_SIP, "E911: Rewriting From User-ID for " << id.data() << " to " << identifier);
      //
      // Rewrite the From-URI and set the line identifier to be sent to the gateway
      //
      UtlString from;
      request.getFromField(&from);
      Url fromUrl(from);

      /// Get the URL user identity if present
      UtlString originalFromUserId;
      fromUrl.getUserId(originalFromUserId);
      // set this as a uri parameter in the new From
      fromUrl.setUrlParameter(SIP_SIPX_FROM, originalFromUserId.data());

      fromUrl.setUserId(identifier);
      UtlString newFromFieldValue;
      fromUrl.toString(newFromFieldValue);
      request.setRawFromField(newFromFieldValue.data());
      //
      // Finally remove the E911LINE sip header
      //
      request.removeHeader(E911LINE, 0);
    }
  }

  return AuthPlugin::CONTINUE;
}

bool EmergencyLineIdentifier::getInstrument(const SipMessage& message, std::string& instrument)
{
  const char* value = NULL;
  value = message.getHeaderValue(0, HTTP_PROXY_AUTHORIZATION_FIELD);

  if (!value)
    return false;

  NetAttributeTokenizer tokenizer(value);

  UtlString n;
  UtlString v;
  UtlString inst;

  while(tokenizer.getNextAttribute(n, v))
  {
    if (n.compareTo(HTTP_AUTHENTICATION_USERNAME_TOKEN, UtlString::ignoreCase) == 0)
    {
      ssize_t index = v.last('/');
      if (index != UTL_NOT_FOUND)
      {
        inst.append(v, index+1, v.length() - index-1);
        instrument = inst.data();
        return true;
      }
    }
  }

  return false;
}

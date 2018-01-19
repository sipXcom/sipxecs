// APPLICATION INCLUDES
#include "SipStatsObserver.h"
#include "StatisticsManagerLib/StatisticsManager.hpp"
#include <net/SipUserAgent.h>
#include <os/OsDateTime.h>
#include "os/OsEventMsg.h"
#include <os/OsLogger.h>

SipStatsObserver::SipStatsObserver(SipUserAgent &sipUserAgent) :
   OsServerTask("SipStatsObserver-%d", NULL, 100),
   SipOutputProcessor(120)
{
   // Register to get incoming requests
   sipUserAgent.addMessageObserver(*getMessageQueue(),
                                   "", // all methods
                                   TRUE, // Requests,
                                   TRUE, //Responses,
                                   TRUE, //Incoming,
                                   FALSE, //OutGoing,
                                   "", //eventName,
                                   NULL, // any session
                                   NULL // no observerData
                                   );

    sipUserAgent.addSipOutputProcessor(this);
}

// Destructor
SipStatsObserver::~SipStatsObserver()
{
}

void SipStatsObserver::handleOutputMessage(SipMessage& message, const char* address, int port)
{
    reportSipStatistics(message, false);
}

UtlBoolean SipStatsObserver::handleMessage(OsMsg& eventMessage)
{
   int msgType = eventMessage.getMsgType();

   if (msgType == OsMsg::PHONE_APP)
   {
      SipMessage* message;

      if((message = (SipMessage *)((SipMessageEvent &)eventMessage).getMessage()))
      {
         reportSipStatistics(*message);
      }
   }

   return(TRUE);
}

void SipStatsObserver::reportSipStatistics(const SipMessage& msg, bool received)
{
  UtlString m;

  bool isRequest = !msg.isResponse();

  if (isRequest)
  {
    msg.getRequestMethod(&m);
  }
  else
  {
     int seq;
     msg.getCSeqField(&seq, &m);
  }

  int code = (isRequest ? 0 : msg.getResponseStatusCode());

  statistics::MethodType method = statistics::SipStatistics::getMethodType(m.data());

  if (received)
  {
     OS_LOG_DEBUG(FAC_SIP, "SipRouter::reportSipStatistics received " << (isRequest ? "request" : "response") << " " << m.data() << " with code " << code);
     statistics::StatisticsManager::Instance().received(method, isRequest, code);
  }
  else
  {
     OS_LOG_DEBUG(FAC_SIP, "SipRouter::reportSipStatistics sent " << (isRequest ? "request" : "response") << " " << m.data() << " with code " << code);
     statistics::StatisticsManager::Instance().sent(method, isRequest, code);
  }
}


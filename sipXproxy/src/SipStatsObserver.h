#ifndef _SipStatsObserver_h_
#define _SipStatsObserver_h_

#include "net/SipOutputProcessor.h"
#include "net/SipMessage.h"
#include <os/OsServerTask.h>

class SipUserAgent;

/// Observe all SIP messages and report statistics
class SipStatsObserver : public OsServerTask, SipOutputProcessor
{
public:

   SipStatsObserver(SipUserAgent &sipUserAgent);
   virtual ~SipStatsObserver();

   virtual UtlBoolean handleMessage(OsMsg& rMsg);

   /// Called when SIP messages are about to be sent by proxy
   virtual void handleOutputMessage( SipMessage& message, const char* address, int port);

private:
   /// prepare and report SIP statistics
   void reportSipStatistics(const SipMessage& msg, bool received = true);

   /// no copy constructor or assignment operator
   SipStatsObserver(const SipStatsObserver& rSipStatsObserver);
   SipStatsObserver operator=(const SipStatsObserver& rSipStatsObserver);

};

#endif  // _SipStatsObserver_h_

#include "SipStatistics.hpp"

#include <string>
#include <string.h>

namespace statistics
{

unsigned int
SipStatistics::sum2xxIn(MethodType method) const
{
   unsigned int ret = 0;
   for (int code = 200; code < 300; ++code)
   {
      ret += responsesReceivedByMethodByCode[method][code];
   }

   return ret;
}

unsigned int
SipStatistics::sum2xxOut(MethodType method) const
{
   unsigned int ret = 0;
   for (int code = 200; code < 300; ++code)
   {
      ret += responsesSentByMethodByCode[method][code];
   }

   return ret;
}

unsigned int
SipStatistics::sumErrIn(MethodType method) const
{
   unsigned int ret = 0;
   for (int code = 300; code < MaxCode; ++code)
   {
      ret += responsesReceivedByMethodByCode[method][code];
   }

   return ret;
}

unsigned int
SipStatistics::sumErrOut(MethodType method) const
{
   unsigned int ret = 0;
   for (int code = 300; code < MaxCode; ++code)
   {
      ret += responsesSentByMethodByCode[method][code];
   }

   return ret;
}

void
SipStatistics::zeroOut()
{
   tuFifoSize = 0;
   transportFifoSizeSum = 0;
   transactionFifoSize = 0;
   activeTimers = 0;
   openTcpConnections = 0;
   activeClientTransactions = 0;
   activeServerTransactions = 0;
   pendingDnsQueries = 0;
   requestsSent = 0;
   responsesSent = 0;
   requestsRetransmitted = 0;
   responsesRetransmitted = 0;
   requestsReceived = 0;
   responsesReceived = 0;
   memset(responsesByCode, 0, sizeof(responsesByCode));
   memset(requestsSentByMethod, 0, sizeof(requestsSentByMethod));
   memset(requestsRetransmittedByMethod, 0, sizeof(requestsRetransmittedByMethod));
   memset(requestsReceivedByMethod, 0, sizeof(requestsReceivedByMethod));
   memset(responsesSentByMethod, 0, sizeof(responsesSentByMethod));
   memset(responsesRetransmittedByMethod, 0, sizeof(responsesRetransmittedByMethod));
   memset(responsesReceivedByMethod, 0, sizeof(responsesReceivedByMethod));
   memset(responsesSentByMethodByCode, 0, sizeof(responsesSentByMethodByCode));
   memset(responsesRetransmittedByMethodByCode, 0, sizeof(responsesRetransmittedByMethodByCode));
   memset(responsesReceivedByMethodByCode, 0, sizeof(responsesReceivedByMethodByCode));
}

std::ostream &
operator<<(std::ostream& strm, const SipStatistics& stats)
{
   unsigned int retriesFinal = 0;

   for (int c = 200; c < 300; ++c)
   {
      retriesFinal += stats.responsesRetransmittedByMethodByCode[INVITE][c];
   }

   unsigned int retriesNonFinal = 0;
   for (int c = 100; c < 200; ++c)
   {
      retriesNonFinal += stats.responsesRetransmittedByMethodByCode[INVITE][c];
   }

   strm << "TU summary: " << stats.tuFifoSize
        << " TRANSPORT " << stats.transportFifoSizeSum
        << " TRANSACTION " << stats.transactionFifoSize
        << " CLIENTTX " << stats.activeClientTransactions
        << " SERVERTX " << stats.activeServerTransactions
        << " TIMERS " << stats.activeTimers
        << std::endl
        << "Transaction summary: reqi " << stats.requestsReceived
        << " reqo " << stats.requestsSent
        << " rspi " << stats.responsesReceived
        << " rspo " << stats.responsesSent
        << std::endl
        << "Details: INVi " << stats.requestsReceivedByMethod[INVITE] << "/S" << stats.sum2xxOut(INVITE) << "/F" << stats.sumErrOut(INVITE)
        << " INVo " << stats.requestsSentByMethod[INVITE]-stats.requestsRetransmittedByMethod[INVITE] << "/S" << stats.sum2xxIn(INVITE) << "/F" << stats.sumErrIn(INVITE)
        << " ACKi " << stats.requestsReceivedByMethod[ACK]
        << " ACKo " << stats.requestsSentByMethod[ACK]-stats.requestsRetransmittedByMethod[ACK]
        << " BYEi " << stats.requestsReceivedByMethod[BYE] << "/S" << stats.sum2xxOut(BYE) << "/F" << stats.sumErrOut(BYE)
        << " BYEo " << stats.requestsSentByMethod[BYE]-stats.requestsRetransmittedByMethod[BYE] << "/S" << stats.sum2xxIn(BYE) << "/F" << stats.sumErrIn(BYE)
        << " CANi " << stats.requestsReceivedByMethod[CANCEL] << "/S" << stats.sum2xxOut(BYE) << "/F" << stats.sumErrOut(BYE)
        << " CANo " << stats.requestsSentByMethod[CANCEL]-stats.requestsRetransmittedByMethod[CANCEL] << "/S" << stats.sum2xxIn(CANCEL) << "/F" << stats.sumErrIn(CANCEL)
        << " MSGi " << stats.requestsReceivedByMethod[MESSAGE] << "/S" << stats.sum2xxOut(MESSAGE) << "/F" << stats.sumErrOut(MESSAGE)
        << " MSGo " << stats.requestsSentByMethod[MESSAGE]-stats.requestsRetransmittedByMethod[MESSAGE] << "/S" << stats.sum2xxIn(MESSAGE) << "/F" << stats.sumErrIn(MESSAGE)
        << " OPTi " << stats.requestsReceivedByMethod[OPTIONS] << "/S" << stats.sum2xxOut(OPTIONS) << "/F" << stats.sumErrOut(OPTIONS)
        << " OPTo " << stats.requestsSentByMethod[OPTIONS]-stats.requestsRetransmittedByMethod[OPTIONS] << "/S" << stats.sum2xxIn(OPTIONS) << "/F" << stats.sumErrIn(OPTIONS)
        << " REGi " << stats.requestsReceivedByMethod[REGISTER] << "/S" << stats.sum2xxOut(REGISTER) << "/F" << stats.sumErrOut(REGISTER)
        << " REGo " << stats.requestsSentByMethod[REGISTER]-stats.requestsRetransmittedByMethod[REGISTER] << "/S" << stats.sum2xxIn(REGISTER) << "/F" << stats.sumErrIn(REGISTER)
        << " PUBi " << stats.requestsReceivedByMethod[PUBLISH] << "/S" << stats.sum2xxOut(PUBLISH) << "/F" << stats.sumErrOut(PUBLISH)
        << " PUBo " << stats.requestsSentByMethod[PUBLISH] << "/S" << stats.sum2xxIn(PUBLISH) << "/F" << stats.sumErrIn(PUBLISH)
        << " SUBi " << stats.requestsReceivedByMethod[SUBSCRIBE] << "/S" << stats.sum2xxOut(SUBSCRIBE) << "/F" << stats.sumErrOut(SUBSCRIBE)
        << " SUBo " << stats.requestsSentByMethod[SUBSCRIBE] << "/S" << stats.sum2xxIn(SUBSCRIBE) << "/F" << stats.sumErrIn(SUBSCRIBE)
        << " NOTi " << stats.requestsReceivedByMethod[NOTIFY] << "/S" << stats.sum2xxOut(NOTIFY) << "/F" << stats.sumErrOut(NOTIFY)
        << " NOTo " << stats.requestsSentByMethod[NOTIFY] << "/S" << stats.sum2xxIn(NOTIFY) << "/F" << stats.sumErrIn(NOTIFY)
        << " REFi " << stats.requestsReceivedByMethod[REFER] << "/S" << stats.sum2xxOut(REFER) << "/F" << stats.sumErrOut(REFER)
        << " REFo " << stats.requestsSentByMethod[REFER] << "/S" << stats.sum2xxIn(REFER) << "/F" << stats.sumErrIn(REFER)
        << " INFi " << stats.requestsReceivedByMethod[INFO] << "/S" << stats.sum2xxOut(INFO) << "/F" << stats.sumErrOut(INFO)
        << " INFo " << stats.requestsSentByMethod[INFO] << "/S" << stats.sum2xxIn(INFO) << "/F" << stats.sumErrIn(INFO)
        << " PRAi " << stats.requestsReceivedByMethod[PRACK] << "/S" << stats.sum2xxOut(PRACK) << "/F" << stats.sumErrOut(PRACK)
        << " PRAo " << stats.requestsSentByMethod[PRACK] << "/S" << stats.sum2xxIn(PRACK) << "/F" << stats.sumErrIn(PRACK)
        << " SERi " << stats.requestsReceivedByMethod[SERVICE] << "/S" << stats.sum2xxOut(SERVICE) << "/F" << stats.sumErrOut(SERVICE)
        << " SERo " << stats.requestsSentByMethod[SERVICE] << "/S" << stats.sum2xxIn(SERVICE) << "/F" << stats.sumErrIn(SERVICE)
        << " UPDi " << stats.requestsReceivedByMethod[UPDATE] << "/S" << stats.sum2xxOut(UPDATE) << "/F" << stats.sumErrOut(UPDATE)
        << " UPDo " << stats.requestsSentByMethod[UPDATE] << "/S" << stats.sum2xxIn(UPDATE) << "/F" << stats.sumErrIn(UPDATE)
        << std::endl
        << "Retransmissions: INVx " << stats.requestsRetransmittedByMethod[INVITE]
        << " finx " << retriesFinal
        << " nonx " << retriesNonFinal
        << " BYE" << stats.requestsRetransmittedByMethod[BYE]
        << " CANx " << stats.requestsRetransmittedByMethod[CANCEL]
        << " MSGx " << stats.requestsRetransmittedByMethod[MESSAGE]
        << " OPTx " << stats.requestsRetransmittedByMethod[OPTIONS]
        << " REGx " << stats.requestsRetransmittedByMethod[REGISTER]
        << " PUBx " << stats.requestsRetransmittedByMethod[PUBLISH]
        << " SUBx " << stats.requestsRetransmittedByMethod[SUBSCRIBE]
        << " NOTx " << stats.requestsRetransmittedByMethod[NOTIFY]
        << " REFx " << stats.requestsRetransmittedByMethod[REFER]
        << " INFx " << stats.requestsRetransmittedByMethod[INFO]
        << " PRAx " << stats.requestsRetransmittedByMethod[PRACK]
        << " SERx " << stats.requestsRetransmittedByMethod[SERVICE]
        << " UPDx " << stats.requestsRetransmittedByMethod[UPDATE];

   return strm;
}

bool
SipStatistics::received(MethodType method, bool request, unsigned int code)
{
   if (request)
   {
      ++requestsReceived;
      ++requestsReceivedByMethod[method];
   }
   else
   {
      ++responsesReceived;
      ++responsesReceivedByMethod[method];
      if (code < 0 || code >= MaxCode)
      {
         code = 0;
      }
      ++responsesReceivedByMethodByCode[method][code];
   }

   return false;
}

bool
SipStatistics::sent(MethodType method, bool request, unsigned int code)
{
   if (request)
   {
      ++requestsSent;
      ++requestsSentByMethod[method];
   }
   else
   {
      if (code < 0 || code >= MaxCode)
      {
         code = 0;
      }

      ++responsesSent;
      ++responsesSentByMethod[method];
      ++responsesSentByMethodByCode[method][code];
   }

   return false;
}

bool
SipStatistics::retransmitted(MethodType method, bool request, unsigned int code)
{
   if (request)
   {
      ++requestsRetransmitted;
      ++requestsRetransmittedByMethod[method];
   }
   else
   {
      ++responsesRetransmitted;
      ++responsesRetransmittedByMethod[method];
      ++responsesRetransmittedByMethodByCode[method][code];
   }
   return false;
}

MethodType SipStatistics::getMethodType(const std::string &method) const
{
      MethodType rc = UNKNOWN;

      for (int i = UNKNOWN; i < MAX_METHODS; i++)
      {
            if (MethodNames[i] == method)
            {
                  rc = static_cast<MethodType>(i);
                  break;
            }
      }

      return rc;
}

} // namespace statistics

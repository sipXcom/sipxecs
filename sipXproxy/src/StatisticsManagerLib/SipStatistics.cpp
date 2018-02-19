#include "SipStatistics.hpp"

#include <string>
#include <string.h>

#define STATISTICS_FREQUENCY 10

namespace statistics
{

static std::string MethodNames[] =
{
      "UNKNOWN",
      "INVITE",
      "REGISTER",
      "SUBSCRIBE",
      "NOTIFY",
      "ACK",
      "BYE",
      "CANCEL",
      "OPTIONS",
      "PUBLISH",
      "REFER",
      "RESPONSE",
      "MESSAGE",
      "INFO",
      "PRACK",
      "SERVICE",
      "UPDATE"
};

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

void
SipStatistics::received(MethodType method, bool isRequest, unsigned int code)
{
      if (isRequest)
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
}

void
SipStatistics::sent(MethodType method, bool isRequest, unsigned int code)
{
      if (isRequest)
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
}

void
SipStatistics::retransmitted(MethodType method, bool isRequest, unsigned int code)
{
      if (isRequest)
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
}

std::string
SipStatistics::getMethodName(MethodType method)
{
      return MethodNames[method];
}

MethodType
SipStatistics::getMethodType(const std::string &method)
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

void
SipStatistics::updateAllStats()
{
      for (int i = UNKNOWN; i < MAX_METHODS; i++)
      {
            const MethodType &method = static_cast<MethodType>(i);
            updateStats(method, Sent);
            updateStats(method, Received);
      }
}

void
SipStatistics::updateStats(MethodType method, StatisticsUpdateSet updateType)
{
      updateStats(method, updateType, true);
      updateStats(method, updateType, false);
}

void
SipStatistics::updateStats(MethodType method, StatisticsUpdateSet updateType, bool isRequest)
{
      std::string methodName = getMethodName(method);

      if (updateType == Sent || updateType == All)
      {
            if (isRequest)
            {
                  mFifo.push(Data(methodName + "o", requestsSentByMethod[method] - requestsRetransmittedByMethod[method]));
                  mFifo.push(Data("reqo", requestsSent));
            }
            else
            {
                  mFifo.push(Data("rspo", responsesSent));
                  mFifo.push(Data(methodName + "iS", sum2xxOut(method)));
                  mFifo.push(Data(methodName + "iF", sumErrOut(method)));
            }

      }

      if (updateType == Received || updateType == All)
      {
            if (isRequest)
            {
                  mFifo.push(Data(methodName + "i", requestsReceivedByMethod[method]));
                  mFifo.push(Data("reqi", requestsReceived));
            }
            else
            {
                  mFifo.push(Data("rspi", responsesReceived));
                  mFifo.push(Data(methodName + "oS", sum2xxIn(method)));
                  mFifo.push(Data(methodName + "oF", sumErrIn(method)));
            }

      }

      if (updateType == Retransmitted || updateType == All)
      {
            mFifo.push(Data(methodName + "x", requestsRetransmittedByMethod[method]));
      }
}

} // namespace statistics

#include "SipStatistics.hpp"

#include <string>
#include <string.h>

namespace statistics
{

static std::string MethodNames[] =
{
      "UNKNOWN",
      "ACK",
      "BYE",
      "CANCEL",
      "INVITE",
      "NOTIFY",
      "OPTIONS",
      "REFER",
      "REGISTER",
      "SUBSCRIBE",
      "RESPONSE",
      "MESSAGE",
      "INFO",
      "PRACK",
      "PUBLISH",
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

      updateStats(method);
}

void
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

      updateStats(method);
}

void
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

      updateStats(method);
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
SipStatistics::updateStats(MethodType m)
{
      mFifo.push(Data(getMethodName(m) + "i", requestsReceivedByMethod[m]));
      mFifo.push(Data(getMethodName(m) + "o", requestsSentByMethod[m] - requestsRetransmittedByMethod[m]));

      mFifo.push(Data(getMethodName(m) + "iS", sum2xxOut(m)));
      mFifo.push(Data(getMethodName(m) + "iF", sumErrOut(m)));

      mFifo.push(Data(getMethodName(m) + "oS", sum2xxIn(m)));
      mFifo.push(Data(getMethodName(m) + "oF", sumErrIn(m)));

      mFifo.push(Data(getMethodName(m) + "x", requestsRetransmittedByMethod[m]));

      mFifo.push(Data("reqi", requestsReceived));
      mFifo.push(Data("reqo", requestsSent));
      mFifo.push(Data("rspi", responsesReceived));
      mFifo.push(Data("rspo", responsesSent));

}

} // namespace statistics

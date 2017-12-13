#pragma once

#include <string>
#include <iostream>

namespace statistics
{

typedef enum
{
   UNKNOWN,
   ACK,
   BYE,
   CANCEL,
   INVITE,
   NOTIFY,
   OPTIONS,
   REFER,
   REGISTER,
   SUBSCRIBE,
   RESPONSE,
   MESSAGE,
   INFO,
   PRACK,
   PUBLISH,
   SERVICE,
   UPDATE,
   MAX_METHODS
} MethodType;

std::string MethodNames[] =
{
   "UNKNOWN"
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

class SipStatistics
{
   public:
      enum {MaxCode = 700};

      unsigned int tuFifoSize;
      unsigned int transportFifoSizeSum;
      unsigned int transactionFifoSize;
      unsigned int activeTimers;
      unsigned int openTcpConnections; // .dlb. not implemented
      unsigned int activeClientTransactions;
      unsigned int activeServerTransactions;
      unsigned int pendingDnsQueries; // .dlb. not implemented

      unsigned int requestsSent; // includes retransmissions
      unsigned int responsesSent; // includes retransmissions
      unsigned int requestsRetransmitted; // counts each retransmission
      unsigned int responsesRetransmitted; // counts each retransmission
      unsigned int requestsReceived;
      unsigned int responsesReceived;

      unsigned int responsesByCode[MaxCode];

      unsigned int requestsSentByMethod[MAX_METHODS];
      unsigned int requestsRetransmittedByMethod[MAX_METHODS];
      unsigned int requestsReceivedByMethod[MAX_METHODS];
      unsigned int responsesSentByMethod[MAX_METHODS];
      unsigned int responsesRetransmittedByMethod[MAX_METHODS];
      unsigned int responsesReceivedByMethod[MAX_METHODS];

      unsigned int responsesSentByMethodByCode[MAX_METHODS][MaxCode];
      unsigned int responsesRetransmittedByMethodByCode[MAX_METHODS][MaxCode];
      unsigned int responsesReceivedByMethodByCode[MAX_METHODS][MaxCode];

      unsigned int sum2xxIn(MethodType method) const;
      unsigned int sumErrIn(MethodType method) const;
      unsigned int sum2xxOut(MethodType method) const;
      unsigned int sumErrOut(MethodType method) const;

      bool received(MethodType metod, bool request, unsigned int code);
      bool sent(MethodType method, bool request, unsigned int code);
      bool retransmitted(MethodType method, bool request, unsigned int code);

      MethodType getMethodType(const std::string &method) const;

      void zeroOut();
};

std::ostream &operator<<(std::ostream& strm, const SipStatistics& stats);

} // namespace statistics

#pragma once

#include "Fifo.hpp"
#include "Data.hpp"
#include <string>
#include <iostream>

namespace statistics
{

typedef enum
{
   UNKNOWN,
   INVITE,
   REGISTER,
   SUBSCRIBE,
   NOTIFY,
   ACK,
   BYE,
   CANCEL,
   OPTIONS,
   PUBLISH,
   REFER,
   RESPONSE,
   MESSAGE,
   INFO,
   PRACK,
   SERVICE,
   UPDATE,
   MAX_METHODS
} MethodType;

class SipStatistics
{
public:

      SipStatistics(Fifo<Data> &fifo) : mFifo(fifo) {};

      enum {MaxCode = 700};

      enum StatisticsUpdateSet
      {
         All = 0,
         Received,
         Sent,
         Retransmitted
      };

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

      void received(MethodType method, bool isRequest, unsigned int code);
      void sent(MethodType method, bool isRequest, unsigned int code);
      void retransmitted(MethodType method, bool isRequest, unsigned int code);

      static std::string getMethodName(MethodType method);
      static MethodType getMethodType(const std::string &method);

      void updateStats(MethodType method, StatisticsUpdateSet updateType, bool isRequest);
      void updateStats(MethodType method, StatisticsUpdateSet updateType);
      void updateAllStats();

      void zeroOut();
private:


      Fifo<Data> &mFifo;
};

} // namespace statistics

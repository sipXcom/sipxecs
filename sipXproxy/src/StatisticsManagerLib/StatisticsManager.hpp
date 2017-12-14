#pragma once
#include "SipStatistics.hpp"
#include "Data.hpp"
#include "Fifo.hpp"

#include <signal.h>
#include <boost/thread/thread.hpp>

namespace statistics
{
    /// \class StatsProcessor
    /// \brief data processing interface
    ///
    /// all processors should extend StatsProcessor interface
    class StatsProcessor
    {
    public:
        /// process new incoming data
        virtual void process(const Data & d) = 0;

        /// processor itself doesn't hold thread for processing
        /// so caller should use touch() in case when no new statistics data available,
        /// but some internal work should be done
        virtual void touch() = 0;
    };

    /// \class Printer simple stats printer to outstream
    class Printer : public StatsProcessor
    {
    public:
        Printer(std::ostream & ss);
        virtual void process(const Data & d);
        virtual void touch();

    private:
        void print(const Data & d) const;

        std::ostream & mStream;
    };

    /// \class TimedMapFileWriter
    /// \brief store in memory and write statistics to file periodically
    /// Processor which will store and statistics data in memory.
    /// On some period it will write stats in file
    class TimedMapFileWriter : public StatsProcessor
    {
        typedef std::map<std::string, Data> DataStorage;

    public:

        /// constructor
        /// \param period of writing data (seconds)
        /// \param filename for writing statistics
        TimedMapFileWriter(time_t period, const std::string & filename);

        virtual void process(const Data & d);
        virtual void touch();

    private:

        /// main file writing routine
        void writeToFile(const std::string &filename);

        time_t mPeriod;
        time_t mLastWriteTime;
        std::string mFileName;
        DataStorage mData;
    };

    /// \typedef processors storage
    typedef std::map<std::string, StatsProcessor *> ProcessorMap;

    /// \class StatisticsManager
    /// \brief main statistics processors holder
    ///
    /// StatisticsManager is global singletone class
    /// StatisticsManager will receive stats from data producers
    /// and give it to registered stats processors
    /// order of calling stats processor is not related to order of registration
    /// StatisticsManager uses lock-free queue to avoid data producers lock and performance lost
    class StatisticsManager
    {
    public:
        static StatisticsManager& Instance()
        {
            static StatisticsManager instance;
            return instance;
        }

    protected:
        StatisticsManager() {}
        ~StatisticsManager() {}

    public:

        /// start internal threads
        void start();

        /// main loop worker
        void worker();

        /// pass data to processors
        void add(const Data &data);

        /// stop processing and shutdown threads
        void stop();

        /// \brief processor registration
        /// \param name processor idendificator
        void registerProcessor(const std::string &name, StatsProcessor *p);

        /// remove all processors
        /// this is not thread safe operation
        void unregisterAllProcessors();

        /// remove single processor
        /// this is not thread safe operation
        void unregisterProcessor(const std::string &name);

        /// log callback registration
        void registerLoggerFunc(void (*f)(const std::string &));

        /// update SIP statistics on message received
        void received(MethodType method, bool request, unsigned int code);

        /// update SIP statistics on message sent
        void sent(MethodType method, bool request, unsigned int code);

        /// update SIP statistics on message retransmitted
        void retransmitted(MethodType method, bool request, unsigned int code);

    private:

        /// logger func
        void log(const std::string &msg);

        sig_atomic_t mFinish;
        ProcessorMap mProcessors;
        Fifo<Data> mFifo;
        boost::thread mWorkerThread;
        void (*mLoggerFunc)(const std::string &);
        SipStatistics *mSipStatistics;
    };
} // namespace statistics

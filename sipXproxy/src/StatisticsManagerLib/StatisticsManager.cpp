
#include "StatisticsManager.hpp"

#include <fstream>
#include <signal.h>
#include <boost/foreach.hpp>
#include <boost/thread/thread.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>

#define SLEEP_TIMEOUT 100

namespace statistics
{
    std::ostream & operator<< (std::ostream &out, Data &d)
    {
        boost::posix_time::ptime t = boost::posix_time::microsec_clock::universal_time();
        out << "{\"timestamp\": \"" << boost::posix_time::to_iso_extended_string(t) << "Z\", \"" << d.name << "\": "<< d.value << "}";
        return out;
    }

    Printer::Printer(std::ostream & ss) : mStream(ss) {}

    void
    Printer::process(const Data & d)
    {
        print(d);
    }

    void
    Printer::touch()
    {
    }

    void
    Printer::print(const Data & d) const
    {
        mStream << d.name << ": " << d.value << std::endl;
    }

    TimedMapFileWriter::TimedMapFileWriter(time_t period, const std::string & filename) : mPeriod(period), mFileName(filename)
    {
        mLastWriteTime = time(NULL);
    }

    void
    TimedMapFileWriter::process(const Data & d)
    {
        mData[d.name] = d;
        touch();
    }

    void
    TimedMapFileWriter::touch()
    {
        time_t now = time(NULL);
        if (now - mPeriod > mLastWriteTime)
        {
            mLastWriteTime = now;
            writeToFile(mFileName);
        }
    }

    void
    TimedMapFileWriter::writeToFile(const std::string &filename)
    {
        std::ofstream file;
        file.open(filename.c_str(), std::ios_base::app);

        BOOST_FOREACH(DataStorage::value_type &v, mData)
        {
            file << v.second << std::endl;
        }

        file.close();
    }

    void
    StatisticsManager::start()
    {
        mFinish = 0;
        mWorkerThread = boost::thread(boost::bind(&StatisticsManager::worker, this));
        log("StatisticsManager started");
    }

    void
    StatisticsManager::worker()
    {
        log("worker started");

        while (!mFinish)
        {
            size_t queueSize = mFifo.size();

            if (queueSize != 0)
            {
                for (size_t i = 0; i < queueSize; i++)
                {
                    Data data = mFifo.pop();

                    BOOST_FOREACH(const ProcessorMap::value_type &p, mProcessors)
                    {
                        p.second->process(data);
                        Data qs("queue_size", queueSize);
                        p.second->process(qs);
                    }
                }
            }
            else
            {
                boost::this_thread::sleep(boost::posix_time::millisec(SLEEP_TIMEOUT));

                BOOST_FOREACH(const ProcessorMap::value_type &p,  mProcessors)
                {
                    p.second->touch();
                }
            }
        }

        log("worker stopped");
    }

    void
    StatisticsManager::add(const Data &data)
    {
        mFifo.push(data);
    }

    void
    StatisticsManager::stop()
    {
        mFinish = 1;
        mWorkerThread.join();
        log("StatisticsManager stopped");
    }

    void
    StatisticsManager::registerProcessor(const std::string &name, StatsProcessor *p)
    {
        log("processor " + name + " registered");
        mProcessors.insert(std::make_pair(name, p));
    }

    void
    StatisticsManager::unregisterAllProcessors()
    {
        BOOST_FOREACH(ProcessorMap::value_type &p,  mProcessors)
        {
            unregisterProcessor(p.first);
        }
    }

    void
    StatisticsManager::unregisterProcessor(const std::string &name)
    {
        mProcessors.erase(name);
    }

    void
    StatisticsManager::registerLoggerFunc(void (*f)(const std::string &))
    {
        mLoggerFunc = f;
    }

    void
    StatisticsManager::log(const std::string &msg)
    {
        if (mLoggerFunc)
        {
            mLoggerFunc(msg);
        }
    }
} // namespace statistics

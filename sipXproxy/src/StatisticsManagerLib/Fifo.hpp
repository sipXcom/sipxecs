#pragma once

#include <queue>
#include <boost/thread/mutex.hpp>

namespace statistics
{

    template<typename T>
    class Fifo
    {
    public:
        void push(const T &t)
        {
            boost::mutex::scoped_lock lock(queueMutex);
            queue.push(t);
        }

        T pop()
        {
            boost::mutex::scoped_lock lock(queueMutex);
            T t = queue.front();
            queue.pop();
            return t;
        }

        size_t size()
        {
            boost::mutex::scoped_lock lock(queueMutex);
            size_t size = queue.size();
            return size;
        }

    private:
        boost::mutex queueMutex;
        std::queue<T> queue;
    };

} // namespace statistics

/* 
 * File:   UtlUtlBlockingQueue.h
 * Author: joegen
 *
 * Created on June 24, 2015, 3:05 PM
 */

#ifndef UTLBLOCKINGQUEUE_H
#define	UTLBLOCKINGQUEUE_H


#include <queue>
#include <cassert>
#include <boost/thread.hpp>
#include <boost/noncopyable.hpp>


template <typename T>
class UtlBlockingQueue : boost::noncopyable
{
private:
  class Semaphore
  {
      //The current semaphore count.
      unsigned int _count;

      //_mutex protects _count.
      //Any code that reads or writes the _count data must hold a lock on
      //the mutex.
      boost::mutex _mutex;

      //Code that increments _count must notify the condition variable.
      boost::condition_variable _condition;

  public:
      explicit Semaphore(unsigned int initial_count = 0)
         : _count(initial_count),
           _mutex(),
           _condition()
      {
      }


      void signal() //called "release" in Java
      {
          boost::unique_lock<boost::mutex> lock(_mutex);

          ++_count;

          //Wake up any waiting threads.
          //Always do this, even if _count wasn't 0 on entry.
          //Otherwise, we might not wake up enough waiting threads if we
          //get a number of signal() calls in a row.
          _condition.notify_one();
      }

      void wait() //called "acquire" in Java
      {
          boost::unique_lock<boost::mutex> lock(_mutex);
          while (_count == 0)
          {
               _condition.wait(lock);
          }
          --_count;
      }

      void clear()
      {
          boost::unique_lock<boost::mutex> lock(_mutex);
          _count = 0;
      }
  };  
  
protected:
  std::queue<T> _queue;
  Semaphore _semaphore;
  std::size_t _maxQueueSize;
  typedef boost::recursive_mutex mutex;
  typedef boost::lock_guard<mutex> mutex_lock;
  mutex _mutex;
  bool _terminating;
  
public:
  UtlBlockingQueue(std::size_t maxQueueSize) :
    _maxQueueSize(maxQueueSize),
    _terminating(false)
  {
  }

  ~UtlBlockingQueue()
  {
    terminate();
  }

  void terminate()
  {
    //
    // Unblock dequeue
    //
    mutex_lock lock(_mutex);
    _terminating = true;
    _semaphore.signal();
  }

  bool dequeue(T& data)
  {
    _semaphore.wait();
    mutex_lock lock(_mutex);
    if (_queue.empty() || _terminating)
      return false;
    data = _queue.front();
    _queue.pop();
    return true;
  }
  
  bool dequeueAll(std::vector<T>& dataVec)
  {
    mutex_lock lock(_mutex);
    if (_queue.empty() || _terminating)
      return false;

    while (!_queue.empty())
    {
      dataVec.push_back(_queue.front());
      _queue.pop();
    }

    return true;
  }

  bool enqueue(const T& data)
  {
    _mutex.lock();
    if (_maxQueueSize && _queue.size() > _maxQueueSize)
    {
      _mutex.unlock();
      return false;
    }
    _queue.push(data);
    _mutex.unlock();
    _semaphore.signal();

    return true;
  }

  void clear()
  {
    std::queue<T> empty;

    mutex_lock lock(_mutex);
    std::swap(_queue, empty);
  }

};

#endif	/* UTLBLOCKINGQUEUE_H */


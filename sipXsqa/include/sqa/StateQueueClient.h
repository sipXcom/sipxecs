/*
 * Copyright (c) eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */

#ifndef StateQueueClient_H
#define StateQueueClient_H

#include <cassert>
#include <zmq.hpp>
#include <boost/noncopyable.hpp>
#include <boost/asio.hpp>
#include <boost/enable_shared_from_this.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <boost/lexical_cast.hpp>

#include <os/OsLogger.h>

#include "StateQueueMessage.h"
#include "BlockingQueue.h"
#include "BlockingTcpClient.h"

#define SQA_LINGER_TIME_MILLIS 5000
#define SQA_TERMINATE_STRING "__TERMINATE__"
#define SQA_KEEP_ALIVE_TICKS 30

// Defines the interval, in seconds, to wait between keep alive loop calls
#define SQA_KEEP_ALIVE_LOOP_INTERVAL_SECS 1

class StateQueueClient : public boost::enable_shared_from_this<StateQueueClient>, private boost::noncopyable
{
public:

  enum Type
  {
    Publisher,
    Worker,
    Watcher
  };

protected:
  typedef BlockingQueue<BlockingTcpClient::Ptr> ClientPool;
  typedef BlockingQueue<std::string> EventQueue;

  Type _type;
  boost::asio::io_service _ioService;
  boost::asio::io_service::work _ioServiceWork;
  boost::thread* _pIoServiceThread;
  boost::scoped_ptr<boost::thread> _pKeepAliveThread;
  int _sleepCount;
  std::size_t _poolSize;
  std::string _serviceAddress;
  std::string _servicePort;
  ClientPool _clientPool;
  bool _terminate;
  zmq::context_t* _zmqContext;
  zmq::socket_t* _zmqSocket;
  boost::thread* _pEventThread;
  std::string _zmqEventId;
  std::string _applicationId;
  EventQueue _eventQueue;
  std::vector<BlockingTcpClient::Ptr> _clientPointers;
  int _expires;
  int _subscriptionExpires;
  int _backoffCount;
  bool _refreshSignin;
  int _currentSigninTick;
  std::string _localAddress;
  int _keepAliveTicks;
  int _currentKeepAliveTicks;
  int _isAlive;

public:
  const std::string& className()
  {
    static const std::string className("StateQueueClient");
    return className;
  }

  StateQueueClient(
        Type type,
        const std::string& applicationId,
        const std::string& serviceAddress,
        const std::string& servicePort,
        const std::string& zmqEventId,
        std::size_t poolSize,
        int readTimeout,
        int writeTimeout,
        int keepAliveTicks = SQA_KEEP_ALIVE_TICKS
        ) :
    _type(type),
    _ioService(),
    _ioServiceWork(_ioService),
    _pIoServiceThread(0),
    _pKeepAliveThread(0),
    _sleepCount(0),
    _poolSize(poolSize),
    _serviceAddress(serviceAddress),
    _servicePort(servicePort),
    _clientPool(_poolSize),
    _terminate(false),
    _zmqContext(new zmq::context_t(1)),
    _zmqSocket(0),
    _pEventThread(0),
    _applicationId(applicationId),
    _eventQueue(1000),
    _expires(10),
    _subscriptionExpires(1800),
    _backoffCount(0),
    _refreshSignin(false),
    _currentSigninTick(-1),
    _keepAliveTicks(keepAliveTicks),
    _currentKeepAliveTicks(keepAliveTicks),
    _isAlive(true)
  {
      if (_type != Publisher)
      {
        createZmqSocket();
      }

      _pIoServiceThread = new boost::thread(boost::bind(&boost::asio::io_service::run, &_ioService));

      for (std::size_t i = 0; i < _poolSize; i++)
      {
        BlockingTcpClient* pClient = new BlockingTcpClient(_ioService, readTimeout, writeTimeout, i == 0 ? SQA_KEY_ALPHA : SQA_KEY_DEFAULT );
        pClient->connect(_serviceAddress, _servicePort);

        if (_localAddress.empty())
        {
          _localAddress = pClient->getLocalAddress();
        }

        BlockingTcpClient::Ptr client(pClient);
        _clientPointers.push_back(client);
        _clientPool.enqueue(client);
      }

      _pKeepAliveThread.reset(new boost::thread( boost::bind(&StateQueueClient::keepAliveThreadRun, this)));

      if (_type == Watcher)
      {
        _zmqEventId = "sqw.";
      }
      else
      {
        _zmqEventId = "sqa.";
      }

      _zmqEventId += zmqEventId;

      OS_LOG_INFO(FAC_NET, "StateQueueClient-" << applicationId <<  " for event " <<  _zmqEventId << " CREATED");

      if (_type != Publisher)
      {
        _pEventThread = new boost::thread(boost::bind(&StateQueueClient::eventLoop, this));
      }
      else
      {
        std::string publisherAddress;
        signin(publisherAddress);
      }
  }

  StateQueueClient(
        Type type,
        const std::string& applicationId,
        const std::string& zmqEventId,
        std::size_t poolSize,
        int readTimeout,
        int writeTimeout,
        int keepAliveTicks = SQA_KEEP_ALIVE_TICKS
        ) :
    _type(type),
    _ioService(),
    _ioServiceWork(_ioService),
    _pIoServiceThread(0),
    _pKeepAliveThread(0),
    _sleepCount(0),
    _poolSize(poolSize),
    _clientPool(_poolSize),
    _terminate(false),
    _zmqContext(new zmq::context_t(1)),
    _zmqSocket(0),
    _pEventThread(0),
    _applicationId(applicationId),
    _eventQueue(1000),
    _expires(10),
    _subscriptionExpires(1800),
    _backoffCount(0),
    _refreshSignin(false),
    _currentSigninTick(-1),
    _keepAliveTicks(keepAliveTicks),
    _currentKeepAliveTicks(keepAliveTicks),
    _isAlive(true)
  {
      if (_type != Publisher)
      {
        createZmqSocket();
      }

      _pIoServiceThread = new boost::thread(boost::bind(&boost::asio::io_service::run, &_ioService));

      for (std::size_t i = 0; i < _poolSize; i++)
      {
        BlockingTcpClient* pClient = new BlockingTcpClient(_ioService, readTimeout, writeTimeout, i == 0 ? SQA_KEY_ALPHA : SQA_KEY_DEFAULT);
        pClient->connect();

        if (_localAddress.empty())
          _localAddress = pClient->getLocalAddress();

        BlockingTcpClient::Ptr client(pClient);
        _clientPointers.push_back(client);
        _clientPool.enqueue(client);
      }

      setServiceAddressAndPort();

      _pKeepAliveThread.reset(new boost::thread(boost::bind(&StateQueueClient::keepAliveThreadRun, this)));

      if (_type == Watcher)
        _zmqEventId = "sqw.";
      else
        _zmqEventId = "sqa.";

      _zmqEventId += zmqEventId;

      OS_LOG_INFO(FAC_NET, "StateQueueClient-" << applicationId <<  " for event " <<  _zmqEventId << " CREATED");

      if (_type != Publisher)
      {
        _pEventThread = new boost::thread(boost::bind(&StateQueueClient::eventLoop, this));
      }
      else
      {
        std::string publisherAddress;
        signin(publisherAddress);
      }
  }

  ~StateQueueClient()
  {
    terminate();
  }

  void setServiceAddressAndPort()
  {
    if (_terminate)
    {
      return;
    }

    BlockingTcpClient::Ptr pClient = *_clientPointers.begin();
    _serviceAddress = pClient->getServiceAddress();
    _servicePort = pClient->getServicePort();
  }

  void createZmqSocket()
  {
    _zmqSocket = new zmq::socket_t(*_zmqContext,ZMQ_SUB);
    int linger = SQA_LINGER_TIME_MILLIS; // milliseconds
    //int recvTimeoutMs = SQA_CONN_READ_TIMEOUT;// milliseconds
    //int sendTimeoutMs = SQA_CONN_WRITE_TIMEOUT;// milliseconds
    _zmqSocket->setsockopt(ZMQ_LINGER, &linger, sizeof(int));
    // WARNING: at one test it worked only with this fix; later it worked also without
    // this fix
    //_zmqSocket->setsockopt(ZMQ_RCVTIMEO, &recvTimeoutMs, sizeof(int));
    //_zmqSocket->setsockopt(ZMQ_SNDTIMEO, &sendTimeoutMs, sizeof(int));
  }

  void destroyZmqSocket()
  {
    if (_zmqSocket)
    {
      // this function verifies if the socket was already closed and close it
      // only if it was not
      _zmqSocket->close();

      delete _zmqSocket;
      _zmqSocket = 0;
    }
  }

  const std::string& getLocalAddress()
  {
    return _localAddress;
  }

  void terminate()
  {
    if (_terminate || !_zmqContext)
      return;

    logout();

     _terminate = true;

    // WARNING: delete _zmqContext will call internally zmq_term
    // Context termination is performed in the following steps:
    //
    // 1. Any blocking operations currently in progress on sockets open
    //    within context shall return immediately with an error code of
    //    ETERM. With the exception of zmq_close(), any further operations on
    //    sockets open within context shall fail with an error code of ETERM.

    // 2. After interrupting all blocking calls, zmq_term() shall block until
    //    the following conditions are satisfied:

    //    o   All sockets open within context have been closed with
    //        zmq_close().

    //    o   For each socket within context, all messages sent by the
    //        application with zmq_send() have either been physically
    //        transferred to a network peer, or the socket's linger period
    //        set with the ZMQ_LINGER socket option has expired.

    // We need to have zmq_socket->close called from another thread otherwise
    // zmq_term will block

    delete _zmqContext;
    _zmqContext = 0;

    OS_LOG_INFO(FAC_NET, CLASS_INFO() "waiting for event thread to exit.");
    if (_pEventThread)
    {
      _pEventThread->join();
      delete _pEventThread;
      _pEventThread = 0;
    }

    destroyZmqSocket();

    _ioService.stop();

    if (_pIoServiceThread)
    {
      _pIoServiceThread->join();
      delete _pIoServiceThread;
      _pIoServiceThread = 0;
    }

    if (_pKeepAliveThread)
    {
      _pKeepAliveThread->join();
    }

    OS_LOG_INFO(FAC_NET, CLASS_INFO() "Ok");
  }

  void setExpires(int expires) { _expires = expires; }

  bool isConnected()
  {
    for (std::vector<BlockingTcpClient::Ptr>::const_iterator iter = _clientPointers.begin();
            iter != _clientPointers.end(); iter++)
    {
      BlockingTcpClient::Ptr pClient = *iter;
      if (pClient->isConnected())
        return true;
    }
    return false;
  }

  void keepAliveLoop()
  {
    if (!_terminate)
    {
      _sleepCount++;

      if (_sleepCount >= _currentKeepAliveTicks)
      {
        _sleepCount = 0;
      }

      if (_refreshSignin && (--_currentSigninTick == 0))
      {
        std::string publisherAddress;
        signin(publisherAddress);
        OS_LOG_INFO(FAC_NET, CLASS_INFO() "refreshed signin @ " << publisherAddress);
      }

      if (!_sleepCount)
      {
        //
        // send keep-alives
        //
        for (std::size_t i = 0; i < _poolSize; i++)
        {
          StateQueueMessage ping;
          StateQueueMessage pong;
          ping.setType(StateQueueMessage::Ping);
          ping.set("message-app-id", _applicationId.c_str());
          if (sendAndReceive(ping, pong))
          {
            if (pong.getType() == StateQueueMessage::Pong)
            {
              // reinitialize service address and service port for StateQueueClient class
              // they were only updated for BlockingTcpClient class
              setServiceAddressAndPort();

              OS_LOG_DEBUG(FAC_NET, CLASS_INFO() "Keep-alive response received from " << _serviceAddress << ":" << _servicePort);
              //
              // Reset it back to the default value
              //
              _currentKeepAliveTicks = _keepAliveTicks;
              _isAlive = true;
            }
          }
          else
          {
            //
            // Reset the keep-alive to 1 so we attempt to reconnect every second
            //
            _currentKeepAliveTicks = 1;
            _isAlive = false;
          }
        }
      }
    }
  }

  void keepAliveThreadRun()
  // Runs the keep alive loop at 1 secs interval
  {
    OS_LOG_INFO(FAC_SIP, CLASS_INFO() << "starting");

    while (!_terminate)
    {
      // sleep the current thread
      boost::this_thread::sleep(boost::posix_time::seconds(SQA_KEEP_ALIVE_LOOP_INTERVAL_SECS));
      keepAliveLoop();

    }

    OS_LOG_INFO(FAC_SIP, CLASS_INFO() << "exiting");
  }

private:
  bool subscribe(const std::string& eventId, const std::string& sqaAddress)
  {
    assert(_type != Publisher);

    OS_LOG_INFO(FAC_NET, CLASS_INFO() "eventId=" << eventId << " address=" << sqaAddress);

    try
    {
      _zmqSocket->connect(sqaAddress.c_str());
      _zmqSocket->setsockopt(ZMQ_SUBSCRIBE, eventId.c_str(), eventId.size());

    }
    catch(std::exception e)
    {
      OS_LOG_INFO(FAC_NET, CLASS_INFO() "eventId=" << eventId << " address=" << sqaAddress << " FAILED!  Error: " << e.what());
      return false;
    }

    return true;
  }

  bool signin(std::string& publisherAddress)
  {
    std::ostringstream ss;
    ss << "sqa.signin."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));

    std::string id = ss.str();

    StateQueueMessage request;
    request.setType(StateQueueMessage::Signin);
    request.set("message-id", id.c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("subscription-expires", _subscriptionExpires);
    request.set("subscription-event", _zmqEventId.c_str());

    std::string clientType;
    if (_type == Publisher)
    {
      request.set("service-type", "publisher");
      clientType = "publisher";
    }
    else if (_type == Worker)
    {
      request.set("service-type", "worker");
      clientType = "worker";
    }
    else if (_type == Watcher)
    {
      request.set("service-type", "watcher");
      clientType = "watcher";
    }

    OS_LOG_NOTICE(FAC_NET, CLASS_INFO() "Type=" << clientType << " SIGNIN");


    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    bool ok = response.get("message-data", publisherAddress);

    if (ok)
    {
      _refreshSignin = true;
      _currentSigninTick = _subscriptionExpires * .75;
      OS_LOG_NOTICE(FAC_NET, CLASS_INFO()  "Type=" << clientType << " SQA=" << publisherAddress << " SUCCEEDED");
    }

    return ok;
  }

  bool logout()
  {
    std::ostringstream ss;
    ss << "sqa.logout."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));

    std::string id = ss.str();

    StateQueueMessage request;
    request.setType(StateQueueMessage::Logout);
    request.set("message-id", id.c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("subscription-event", _zmqEventId.c_str());

    std::string clientType;
    if (_type == Publisher)
    {
      request.set("service-type", "publisher");
      clientType = "publisher";
    }
    else if (_type == Worker)
    {
      request.set("service-type", "worker");
      clientType = "worker";
    }
    else if (_type == Watcher)
    {
      request.set("service-type", "watcher");
      clientType = "watcher";
    }

    StateQueueMessage response;
    return sendAndReceive(request, response);
  }

  void subscribeForEvents()
  {
    std::string publisherAddress;
    const int retryTime = 1000;

    while(!_terminate)
    {
      if (signin(publisherAddress))
      {
        break;
      }
      else
      {
        OS_LOG_WARNING(FAC_NET, CLASS_INFO()
                  << "Network Queue did no respond.  Retrying SIGN IN after " << retryTime << " ms.");
        boost::this_thread::sleep(boost::posix_time::milliseconds(retryTime));
      }
    }

    while (!_terminate)
    {
      if (subscribe(_zmqEventId, publisherAddress))
      {
        break;
      }
      else
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO()
            << "Is unable to SUBSCRIBE to SQA service @ " << publisherAddress);
        boost::this_thread::sleep(boost::posix_time::milliseconds(retryTime));
      }
    }
  }

  void eventLoop()
  {
    bool firstHit = true;

    subscribeForEvents();

    assert(_type != Publisher);

    while (!_terminate)
    {
      bool lastReadFailed = false;
      // Although ZeroMQ assures that a sub socket will reconnect to publisher automatically,
      // this seems to be not a 100% certainty.  There are cases when publisher disappears,
      // it takes 8 minutes for the client to receive events again.
      //
      // There is also the case of socket reads where a cann to recv is assured to only return when
      // it receives the complete packet.  We will not treat this at 100% certainty and treat
      // a read failure as a trigger for resubscribe.

      if (false == _isAlive || lastReadFailed)
      {
        OS_LOG_ERROR(FAC_NET, "StateQueueClient::eventLoop connection is not alive, closing socket, resubscribe");

        // Create a new socket to refresh it's state.
        destroyZmqSocket();
        createZmqSocket();

        //
        // This will block until it succeeds or _terminated flag is set
        // So there is no danger of a runaway loop here
        //
        subscribeForEvents();
      }

      std::string id;
      std::string data;
      int count = 0;

      if (readEvent(id, data, count))
      {
        if (_terminate)
        {
          break;
        }

        OS_LOG_INFO(FAC_NET, CLASS_INFO() "received event: " << id);
        OS_LOG_DEBUG(FAC_NET, CLASS_INFO() "received data: " << std::endl << data);

        if (_type == Worker)
        {
          OS_LOG_DEBUG(FAC_NET, CLASS_INFO() "popping data: " << id);
          do_pop(firstHit, count, id, data);
        }
        else if (_type == Watcher)
        {
          OS_LOG_DEBUG(FAC_NET, CLASS_INFO() "watching data: " << id);
          do_watch(firstHit, count, id, data);
        }
      }
      else
      {
        lastReadFailed = true;

        if (_terminate)
        {
          break;
        }
      }

      firstHit = false;
    }

    if (_type == Watcher)
    {
      do_watch(firstHit, 0, SQA_TERMINATE_STRING, SQA_TERMINATE_STRING);
    }
    else if (_type == Worker)
    {
      do_pop(firstHit, 0, SQA_TERMINATE_STRING, SQA_TERMINATE_STRING);
    }

    // WARNING: This should not be removed. zmq_term will block until all sockets are closed
    _zmqSocket->close();

    OS_LOG_INFO(FAC_NET, CLASS_INFO() "TERMINATED.");
  }

  void do_pop(bool firstHit, int count, const std::string& id, const std::string& data)
  {
    //
    // Check if we are the last succesful popper.
    // If count >= 2 we will skip the next message
    // after we have successfully popped
    //

    if (id.substr(0, 3) == "sqw")
    {
      OS_LOG_WARNING(FAC_NET, CLASS_INFO() "do_pop dropping event " << id);
      return;
    }

    if (id == "__TERMINATE__")
    {
      StateQueueMessage terminate;
      terminate.setType(StateQueueMessage::Pop);
      terminate.set("message-id", "__TERMINATE__");
      terminate.set("message-data", "__TERMINATE__");
      _eventQueue.enqueue(terminate.data());
      return;
    }

    if (!firstHit && count >= 2 && _backoffCount < count - 1 )
    {
      _backoffCount++; //  this will ensure that we participate next time
      boost::this_thread::yield();
    }

    //
    // Check if we are in the exlude list
    //
    if (data.find(_applicationId.c_str()) != std::string::npos)
    {
      //
      // We are still considered the last popper so don't toggle?
      //
      _backoffCount++;
      OS_LOG_DEBUG(FAC_NET, CLASS_INFO() "do_pop is not allowed to pop " << id);
      return;
    }

    //
    // Pop it
    //
    StateQueueMessage pop;
    pop.setType(StateQueueMessage::Pop);
    pop.set("message-id", id.c_str());
    pop.set("message-app-id", _applicationId.c_str());
    pop.set("message-expires", _expires);

    OS_LOG_INFO(FAC_NET, CLASS_INFO() << _applicationId
              << " Popping event " << id);

    StateQueueMessage popResponse;

    if (!sendAndReceive(pop, popResponse))
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "do_pop unable to send pop command for event " << id);
      _backoffCount++;
      return;
    }

    //
    // Check if Pop is successful
    //
    std::string messageResponse;
    popResponse.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      popResponse.get("message-error", messageResponseError);
      OS_LOG_DEBUG(FAC_NET, CLASS_INFO()
              << "Dropping event " << id
              << " Error: " << messageResponseError);
      _backoffCount++;
    }
    else
    {
      std::string messageId;
      popResponse.get("message-id", messageId);
      std::string messageData;
      popResponse.get("message-data", messageData);
      OS_LOG_DEBUG(FAC_NET, CLASS_INFO()
              << "Popped event " << messageId << " -- " << messageData);
      _eventQueue.enqueue(popResponse.data());
      _backoffCount = 0;
    }
  }

  void do_watch(bool firstHit, int count, const std::string& id, const std::string& data)
  {
    OS_LOG_DEBUG(FAC_NET, CLASS_INFO() "Received watcher data " << id);
    StateQueueMessage watcherData;
    watcherData.set("message-id", id);
    watcherData.set("message-data", data);
    _eventQueue.enqueue(watcherData.data());
  }

  bool readEvent(std::string& id, std::string& data, int& count)
  {
    assert(_type != Publisher);

    try
    {
      if (!zmq_receive(_zmqSocket, id))
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO() "0mq failed failed to receive ID segment.");
        return false;
      }

      std::string address;
      if (!zmq_receive(_zmqSocket, address))
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO() "0mq failed failed to receive ADDR segment.");
        return false;
      }

      //
      // Read the data vector
      //
      if (!zmq_receive(_zmqSocket, data))
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO() "0mq failed failed to receive DATA segment.");
        return false;
      }

      //
      // Read number of subscribers active
      //
      std::string strcount;
      if (!zmq_receive(_zmqSocket, strcount))
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO() "0mq failed failed to receive COUNT segment.");
        return false;
      }

        count = boost::lexical_cast<int>(strcount);
    }
    catch(std::exception e)
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "Unknown exception: " << e.what());
      return false;
    }
    return true;
  }

  static void zmq_free (void *data, void *hint)
  {
      free (data);
  }

  //  Convert string to 0MQ string and send to socket
  static bool zmq_send (zmq::socket_t & socket, const std::string & data)
  {
    char * buff = (char*)malloc(data.size());
    memcpy(buff, data.c_str(), data.size());
    zmq::message_t message((void*)buff, data.size(), zmq_free, 0);
    bool rc = socket.send(message);
    return (rc);
  }

  //  Sends string as 0MQ string, as multipart non-terminal
  static bool zmq_sendmore (zmq::socket_t & socket, const std::string & data)
  {
    char * buff = (char*)malloc(data.size());
    memcpy(buff, data.c_str(), data.size());
    zmq::message_t message((void*)buff, data.size(), zmq_free, 0);
    bool rc = socket.send(message, ZMQ_SNDMORE);
    return (rc);
  }

  static bool zmq_receive (zmq::socket_t *socket, std::string& value)
  {
      zmq::message_t message;
      socket->recv(&message);

      if (!message.size())
      {
        return false;
      }

      value = std::string(static_cast<char*>(message.data()), message.size());
      return true;
  }

  bool sendNoResponse(const StateQueueMessage& request)
  {
    if (!_isAlive)
    {
      OS_LOG_WARNING(FAC_NET, CLASS_INFO() "Connection is not alive.");
      return false;
    }

    BlockingTcpClient::Ptr conn;
    if (!_clientPool.dequeue(conn))
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "Unable to retrieve a TCP connection for pool.");
      return false;
    }

    if (!conn->isConnected() && !conn->connect(_serviceAddress, _servicePort))
    {
      //
      // Put it back to the queue.  The server is down.
      //
      _clientPool.enqueue(conn);
      return false;
    }

    bool sent = conn->send(request);
    _clientPool.enqueue(conn);
    return sent;
  }

  bool sendAndReceive(const StateQueueMessage& request, StateQueueMessage& response)
  {

    if (!_isAlive && request.getType() != StateQueueMessage::Ping)
    {
      OS_LOG_WARNING(FAC_NET, CLASS_INFO() "Connection is not alive.");
      //
      // Only allow ping requests to get through when connection is not alive
      //
      return false;
    }

    BlockingTcpClient::Ptr conn;

    if (!_clientPool.dequeue(conn))
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "Unable to retrieve a TCP connection for pool.");
      return false;
    }

    if (!conn->isConnected() && !conn->connect())
    {
      //
      // Put it back to the queue.  The server is down.
      //
      _clientPool.enqueue(conn);
      return false;
    }

    bool sent = conn->sendAndReceive(request, response);
    _clientPool.enqueue(conn);
    return sent;
  }

  bool pop(StateQueueMessage& ev)
  {
    std::string data;
    if (_eventQueue.dequeue(data))
    {
      ev.parseData(data);
      return true;
    }
    return false;
  }

  bool enqueue(const std::string& id, const std::string& data, int expires = 30, bool publish= false)
  {
    //
    // Enqueue it
    //
    StateQueueMessage enqueueRequest;

    if (!publish)
    {
      enqueueRequest.setType(StateQueueMessage::Enqueue);
    }
    else
    {
      enqueueRequest.setType(StateQueueMessage::EnqueueAndPublish);
    }

    enqueueRequest.set("message-id", id.c_str());
    enqueueRequest.set("message-app-id", _applicationId.c_str());
    enqueueRequest.set("message-expires", _expires);
    enqueueRequest.set("message-data", data);

    StateQueueMessage enqueueResponse;
    if (!sendAndReceive(enqueueRequest, enqueueResponse))
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "FAILED");
      return false;
    }

    //
    // Check if Queue is successful
    //
    std::string messageResponse;
    enqueueResponse.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      enqueueResponse.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << id
                  << " Error: " << messageResponseError);
      return false;
    }

    return true;
  }

  bool internal_publish(const std::string& id, const std::string& data, bool noresponse = false)
  {
    //
    // Enqueue it
    //
    StateQueueMessage enqueueRequest;
    enqueueRequest.setType(StateQueueMessage::Publish);
    enqueueRequest.set("message-id", id.c_str());
    enqueueRequest.set("message-app-id", _applicationId.c_str());
    enqueueRequest.set("message-data", data);


    OS_LOG_INFO(FAC_NET, CLASS_INFO() << "publishing data ID=" << id);

    if (noresponse)
    {
      enqueueRequest.set("noresponse", noresponse);
      return sendNoResponse(enqueueRequest);
    }
    else
    {
      StateQueueMessage enqueueResponse;

      if (!sendAndReceive(enqueueRequest, enqueueResponse))
      {
        return false;
      }

      //
      // Check if Queue is successful
      //
      std::string messageResponse;
      enqueueResponse.get("message-response", messageResponse);

      if (messageResponse != "ok")
      {
        std::string messageResponseError;
        enqueueResponse.get("message-error", messageResponseError);
        OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                    << "Failed to publish " << id
                    << " Error: " << messageResponseError);
        return false;
      }

      return true;
    }
  }

  bool internal_publish_and_persist(int workspace, const std::string& id, const std::string& data, int expires)
  {
    StateQueueMessage enqueueRequest;
    enqueueRequest.setType(StateQueueMessage::PublishAndPersist);
    enqueueRequest.set("message-id", id.c_str());
    enqueueRequest.set("message-data-id", id.c_str());
    enqueueRequest.set("message-app-id", _applicationId.c_str());
    enqueueRequest.set("message-data", data);
    enqueueRequest.set("message-expires", expires);
    enqueueRequest.set("workspace", workspace);

    OS_LOG_INFO(FAC_NET, CLASS_INFO() "publishing data ID=" << id);

    StateQueueMessage enqueueResponse;

    if (!sendAndReceive(enqueueRequest, enqueueResponse))
    {
      return false;
    }

    //
    // Check if Queue is successful
    //
    std::string messageResponse;
    enqueueResponse.get("message-response", messageResponse);
    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      enqueueResponse.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to publish " << id
                  << " Error: " << messageResponseError);
      return false;
    }

    return true;
  }


public:

  bool pop(std::string& id, std::string& data)
  {
    StateQueueMessage message;

    if (!pop(message))
    {
      return false;
    }

    return message.get("message-id", id) && message.get("message-data", data);
  }

  bool watch(std::string& id, std::string& data)
  {
    OS_LOG_INFO(FAC_NET, CLASS_INFO() "(" << id << ") INVOKED" );
    StateQueueMessage message;

    if (!pop(message))
    {
      return false;
    }

    return message.get("message-id", id) && message.get("message-data", data);
  }

  bool enqueue(const std::string& data, int expires = 30, bool publish = false)
  {
    if (_type != Publisher)
    {
      return false;
    }

    std::ostringstream ss;
    ss << _zmqEventId << "."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));

    return enqueue(ss.str(), data, expires, publish);
  }


  bool publish(const std::string& eventId, const std::string& data, bool noresponse)
  {
    if (_type != Publisher)
    {
      return false;
    }

    std::ostringstream ss;
    ss << "sqw." << eventId << "."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));

    return internal_publish(ss.str(), data, noresponse);
  }

  bool publish(const std::string& eventId, const char* data, int dataLength, bool noresponse)
  {
    std::string buff = std::string(data, dataLength);
    return publish(eventId, data, dataLength);
  }

  bool publishAndPersist(int workspace, const std::string& eventId, const std::string& data, int expires)
  {
    if (_type != Publisher)
    {
      return false;
    }

    std::ostringstream ss;
    ss << "sqw." << eventId;

    return internal_publish_and_persist(workspace, ss.str(), data, expires);
  }


  bool erase(const std::string& id)
  {
    StateQueueMessage request;
    request.setType(StateQueueMessage::Erase);
    request.set("message-id", id.c_str());
    request.set("message-app-id", _applicationId.c_str());

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to erase " << id
                  << " Error: " << messageResponseError);
      return false;
    }

    OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Successfully erased " << id);
    return true;
  }

  bool persist(int workspace, const std::string& dataId, int expires)
  {
    std::ostringstream ss;
    ss << "sqa.persist."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));
    //
    // Enqueue it
    //
    StateQueueMessage request;
    request.setType(StateQueueMessage::Persist);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("message-expires", _expires);
    request.set("workspace", workspace);
    request.set("message-data-id", dataId.c_str());

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return true;
  }

  bool set(int workspace, const std::string& dataId, const std::string& data, int expires)
  {
    std::ostringstream ss;
    ss << "sqa.set."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));

    StateQueueMessage request;
    request.setType(StateQueueMessage::Set);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("message-expires", _expires);
    request.set("workspace", workspace);
    request.set("message-data-id", dataId.c_str());
    request.set("message-data", data);

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return true;
  }

  bool mset(int workspace, const std::string& mapId, const std::string& dataId, const std::string& data, int expires)
  {
    std::ostringstream ss;
    ss << "sqa.mset."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));

    StateQueueMessage request;
    request.setType(StateQueueMessage::MapSet);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("message-expires", _expires);
    request.set("workspace", workspace);
    request.set("message-map-id", mapId.c_str());
    request.set("message-data-id", dataId.c_str());
    request.set("message-data", data);

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return true;
  }

  bool get(int workspace, const std::string& dataId, std::string& data)
  {
    std::ostringstream ss;
    ss << "sqa.get."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));
    //
    // Enqueue it
    //
    StateQueueMessage request;
    request.setType(StateQueueMessage::Get);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("workspace", workspace);
    request.set("message-data-id", dataId.c_str());

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return response.get("message-data", data);
  }

  bool mget(int workspace, const std::string& mapId, const std::string& dataId, std::string& data)
  {
    std::ostringstream ss;
    ss << "sqa.get."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));
    //
    // Enqueue it
    //
    StateQueueMessage request;
    request.setType(StateQueueMessage::MapGet);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("workspace", workspace);
    request.set("message-map-id", mapId.c_str());
    request.set("message-data-id", dataId.c_str());

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return response.get("message-data", data);
  }

  bool mgetm(int workspace, const std::string& mapId, std::map<std::string, std::string>& smap)
  {
    std::ostringstream ss;
    ss << "sqa.get."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));
    //
    // Enqueue it
    //
    StateQueueMessage request;
    request.setType(StateQueueMessage::MapGetMultiple);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("workspace", workspace);
    request.set("message-map-id", mapId.c_str());

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << mapId
                  << " Error: " << messageResponseError);
      return false;
    }

    std::string data;
    response.get("message-data", data);

    StateQueueMessage message;

    if (!message.parseData(data))
    {
      return false;
    }

    return message.getMap(smap);
  }

  bool mgeti(int workspace, const std::string& mapId, const std::string& dataId, std::string& data)
  {
    std::ostringstream ss;
    ss << "sqa.get."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));
    //
    // Enqueue it
    //
    StateQueueMessage request;
    request.setType(StateQueueMessage::MapGetInc);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("workspace", workspace);
    request.set("message-map-id", mapId.c_str());
    request.set("message-data-id", dataId.c_str());

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);
    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return response.get("message-data", data);
  }

  bool remove(int workspace, const std::string& dataId)
  {
    std::ostringstream ss;
    ss << "sqa.remove."
       << std::hex << std::uppercase
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0)) << "-"
       << std::setw(4) << std::setfill('0') << (int) ((float) (0x10000) * random () / (RAND_MAX + 1.0));
    //
    // Enqueue it
    //
    StateQueueMessage request;
    request.setType(StateQueueMessage::Remove);
    request.set("message-id", ss.str().c_str());
    request.set("message-app-id", _applicationId.c_str());
    request.set("message-data-id", dataId.c_str());
    request.set("workspace", workspace);

    StateQueueMessage response;

    if (!sendAndReceive(request, response))
    {
      return false;
    }

    std::string messageResponse;
    response.get("message-response", messageResponse);

    if (messageResponse != "ok")
    {
      std::string messageResponseError;
      response.get("message-error", messageResponseError);
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
                  << "Failed to enqueue " << dataId
                  << " Error: " << messageResponseError);
      return false;
    }

    return true;
  }
};

#endif /* StateQueueClient_H */


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

#ifndef BlockingTcpClient_H
#define	BlockingTcpClient_H

#include <cassert>
#include <zmq.hpp>
#include <boost/noncopyable.hpp>
#include <boost/asio.hpp>
#include <boost/enable_shared_from_this.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <boost/lexical_cast.hpp>

#include <os/OsLogger.h>
#include <os/OsServiceOptions.h>

#include "StateQueueMessage.h"
#include "BlockingQueue.h"
#include "BlockingTcpClient.h"

#define SQA_LINGER_TIME_MILLIS 5000
#define SQA_TERMINATE_STRING "__TERMINATE__"
#define SQA_CONN_MAX_READ_BUFF_SIZE 65536
#define SQA_CONN_CONNECTION_TIMEOUT_MSEC 5000
#define SQA_CONN_READ_TIMEOUT 1000
#define SQA_CONN_WRITE_TIMEOUT 1000
#define SQA_KEY_MIN 22172
#define SQA_KEY_ALPHA 22180
#define SQA_KEY_DEFAULT SQA_KEY_MIN
#define SQA_KEY_MAX 22200
#define SQA_KEEP_ALIVE_TICKS 30

// Defines the interval, in seconds, to wait between keep alive loop calls
#define SQA_KEEP_ALIVE_LOOP_INTERVAL_SECS 1

  class BlockingTcpClient
  {
public:

    typedef boost::shared_ptr<BlockingTcpClient> Ptr;

    BlockingTcpClient(
      boost::asio::io_service& ioService,
      int readTimeout = SQA_CONN_READ_TIMEOUT,
      int writeTimeout = SQA_CONN_WRITE_TIMEOUT,
      short key = SQA_KEY_DEFAULT);

    ~BlockingTcpClient();

    bool connect();
    bool connect(const std::string& serviceAddress, const std::string& servicePort);
    bool send(const StateQueueMessage& request);
    bool receive(StateQueueMessage& response);
    bool sendAndReceive(const StateQueueMessage& request, StateQueueMessage& response);
    bool isConnected() const;
    std::string getLocalAddress();
    const std::string &getServicePort() const;
    const std::string &getServiceAddress() const;

private:
    void setReadTimeout(boost::asio::ip::tcp::socket& socket, int milliseconds);
    void setWriteTimeout(boost::asio::ip::tcp::socket& socket, int milliseconds);
    void startReadTimer();
    void startWriteTimer();
    void startConnectTimer();
    void cancelReadTimer();
    void cancelWriteTimer();
    void cancelConnectTimer();
    void onReadTimeout(const boost::system::error_code& e);
    void onWriteTimeout(const boost::system::error_code& e);
    void onConnectTimeout(const boost::system::error_code& e);
    void close();
    bool timedWaitUntilDataAvailable(boost::function<void(const boost::system::error_code&)> onTimeoutCb, int timeoutMs, short int requestedEvents);
    bool timedWaitUntilReadDataAvailable();
    bool timedWaitUntilWriteDataAvailable();

    unsigned long getNextReadSize();


private:
    class ConnectTimer
    {
    public:
      ConnectTimer(BlockingTcpClient* pOwner) :
        _pOwner(pOwner)
      {
        _pOwner->startConnectTimer();
      }

      ~ConnectTimer()
      {
        _pOwner->cancelConnectTimer();
      }
      BlockingTcpClient* _pOwner;
    };

    class ReadTimer
    {
    public:
      ReadTimer(BlockingTcpClient* pOwner) :
        _pOwner(pOwner)
      {
        _pOwner->startReadTimer();
      }

      ~ReadTimer()
      {
        _pOwner->cancelReadTimer();
      }
      BlockingTcpClient* _pOwner;
    };

    class WriteTimer
    {
    public:
      WriteTimer(BlockingTcpClient* pOwner) :
        _pOwner(pOwner)
      {
        _pOwner->startWriteTimer();
      }

      ~WriteTimer()
      {
        _pOwner->cancelWriteTimer();
      }
      BlockingTcpClient* _pOwner;
    };

    const std::string& className();

    boost::asio::io_service& _ioService;
    boost::asio::ip::tcp::resolver _resolver;
    boost::asio::ip::tcp::socket *_pSocket;
    std::string _serviceAddress;
    std::string _servicePort;
    bool _isConnected;
    int _readTimeout;
    int _writeTimeout;
    short _key;
    boost::asio::deadline_timer _readTimer;
    boost::asio::deadline_timer _writeTimer;
    boost::asio::deadline_timer _connectTimer;
  };

#endif	/* BlockingTcpClient_H */


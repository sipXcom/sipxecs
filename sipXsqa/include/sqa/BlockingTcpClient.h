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

    BlockingTcpClient(boost::asio::io_service& ioService, int readTimeout, int writeTimeout, short key) :
      _ioService(ioService),
      _resolver(_ioService),
      _pSocket(0),
      _isConnected(false),
      _readTimeout(readTimeout),
      _writeTimeout(writeTimeout),
      _key(key),
      _readTimer(_ioService),
      _writeTimer(_ioService),
      _connectTimer(_ioService)
  {
  }

  ~BlockingTcpClient()
  {
    if (_pSocket)
    {
      delete _pSocket;
      _pSocket = 0;
    }
  }

  bool connect()
  {
    // Initialize State Queue Agent Publisher if an address is provided
    std::string sqaControlAddress;
    std::string sqaControlPort;
    std::ostringstream sqaconfig;
    sqaconfig << SIPX_CONFDIR << "/" << "sipxsqa-client.ini";
    OsServiceOptions configOptions(sqaconfig.str());

    if (configOptions.parseOptions())
    {
      bool enabled = false;
      if (configOptions.getOption("enabled", enabled, enabled) && enabled)
      {
        configOptions.getOption("sqa-control-address", _serviceAddress);
        configOptions.getOption("sqa-control-port", _servicePort);

        if (configOptions.hasOption("tcp-timeout"))
        {
          configOptions.getOption("tcp-timeout", _readTimeout);
          configOptions.getOption("tcp-timeout", _writeTimeout);
        }
      }
      else
      {
        OS_LOG_ERROR(FAC_NET, "BlockingTcpClient::connect() this:" << this << " Unable to read connection information from " << sqaconfig.str());
        return false;
      }
    }

    if(_serviceAddress.empty() || _servicePort.empty())
    {
      OS_LOG_ERROR(FAC_NET, "BlockingTcpClient::connect() this:" << this << " remote address is not set");
      return false;
    }

    return connect(_serviceAddress, _servicePort);
  }

  bool connect(const std::string& serviceAddress, const std::string& servicePort)
  {
    // Close the previous socket;
    close();

    if (_pSocket)
    {
      delete _pSocket;
      _pSocket = 0;
    }

    _pSocket = new boost::asio::ip::tcp::socket(_ioService);

    OS_LOG_INFO(FAC_NET, CLASS_INFO() "creating new connection to " << serviceAddress << ":" << servicePort);

    _serviceAddress = serviceAddress;
    _servicePort = servicePort;

    try
    {
      boost::asio::ip::tcp::resolver::query query(boost::asio::ip::tcp::v4(), serviceAddress.c_str(), servicePort.c_str());
      boost::asio::ip::tcp::resolver::iterator hosts = _resolver.resolve(query);

      ConnectTimer timer(this);

      // this flag may be reset by ConnectTimer's timer during connect() call
      _isConnected = true;

      //////////////////////////////////////////////////////////////////////////
      // Only works in 1.47 version of asio.  1.46 doesnt have this utility func
      // boost::asio::connect(*_pSocket, hosts);
      _pSocket->connect(hosts->endpoint()); // so we use the connect member
      //////////////////////////////////////////////////////////////////////////
      OS_LOG_INFO(FAC_NET, CLASS_INFO() "creating new connection to " << serviceAddress << ":" << servicePort << " SUCESSFUL.");
    }
    catch(std::exception &e)
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "failed with error " << e.what());
      _isConnected = false;
    }

    return _isConnected;
  }

  bool send(const StateQueueMessage& request)
  {
    assert(_pSocket);
    std::string data = request.data();

    if (data.size() > SQA_CONN_MAX_READ_BUFF_SIZE - 1) /// Account for the terminating char "_"
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "data size: " << data.size() << " maximum buffer length of " << SQA_CONN_MAX_READ_BUFF_SIZE - 1);
      return false;
    }

    short version = 1;
    unsigned long len = (unsigned long)data.size() + 1; /// Account for the terminating char "_"
    std::stringstream strm;
    strm.write((char*)(&version), sizeof(version));
    strm.write((char*)(&_key), sizeof(_key));
    strm.write((char*)(&len), sizeof(len));
    strm << data << "_";
    std::string packet = strm.str();
    boost::system::error_code ec;
    bool ok = false;

    {
      if (false == timedWaitUntilWriteDataAvailable())
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO()
            << "timedWaitUntilWriteDataAvailable failed: "
            << "Unable to send request");

        _isConnected = false;
        return false;
      }

      //ok = boost::asio::write(*_pSocket, boost::asio::buffer(packet.c_str(), packet.size()),  boost::asio::transfer_all(), ec) > 0;
      ok = _pSocket->write_some(boost::asio::buffer(packet.c_str(), packet.size()), ec) > 0;
    }

    if (!ok || ec)
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO() "write_some error: " << ec.message());
      _isConnected = false;
      return false;
    }
    return true;
  }

  bool receive(StateQueueMessage& response)
  {
    assert(_pSocket);
    unsigned long len = getNextReadSize();
    if (!len)
    {
      OS_LOG_INFO(FAC_NET, CLASS_INFO() "next read size is empty.");
      return false;
    }

    char responseBuff[len];
    boost::system::error_code ec;
    {
      if (false == timedWaitUntilReadDataAvailable())
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO()
            << "timedWaitUntilReadDataAvailable failed: "
            << "Unable to receive response");

        _isConnected = false;
        return false;
      }

      _pSocket->read_some(boost::asio::buffer((char*)responseBuff, len), ec);
    }

    if (ec)
    {
      if (boost::asio::error::eof == ec)
      {
        OS_LOG_INFO(FAC_NET, CLASS_INFO() "remote closed the connection, read_some error: " << ec.message());
      }
      else
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO() "read_some error: " << ec.message());
      }

      _isConnected = false;
      return false;
    }
    std::string responseData(responseBuff, len);
    return response.parseData(responseData);
  }

  bool sendAndReceive(const StateQueueMessage& request, StateQueueMessage& response)
  {
    if (send(request))
    {
      return receive(response);
    }

    return false;
  }

  bool isConnected() const
  {
    return _isConnected;
  }

  std::string getLocalAddress()
  {
    try
    {
      if (!_pSocket)
      {
        return "";
      }

      return _pSocket->local_endpoint().address().to_string();
    }
    catch(...)
    {
      return "";
    }
  }

  const std::string &getServicePort() const
  {
    return _servicePort;
  }

  const std::string &getServiceAddress() const
  {
    return _serviceAddress;
  }

private:
  void startConnectTimer()
  {
    boost::system::error_code ec;
    _connectTimer.expires_from_now(boost::posix_time::milliseconds(SQA_CONN_CONNECTION_TIMEOUT_MSEC), ec);
    _connectTimer.async_wait(boost::bind(&BlockingTcpClient::onConnectTimeout, this, boost::asio::placeholders::error));
  }

  void cancelConnectTimer()
  {
    boost::system::error_code ec;
    _connectTimer.cancel(ec);
  }

  void onReadTimeout(const boost::system::error_code& e)
  {
    if (e)
    {
      return;
    }

    close();
    OS_LOG_ERROR(FAC_NET, CLASS_INFO() "- " << _readTimeout << " milliseconds.");
  }

  void onWriteTimeout(const boost::system::error_code& e)
  {
    if (e)
    {
      return;
    }

    close();
    OS_LOG_ERROR(FAC_NET, CLASS_INFO() "- " << _writeTimeout << " milliseconds.");
  }

  void onConnectTimeout(const boost::system::error_code& e)
  {
    if (e)
    {
      return;
    }

    close();
    OS_LOG_ERROR(FAC_NET, CLASS_INFO() "- " << SQA_CONN_CONNECTION_TIMEOUT_MSEC << " milliseconds.");
  }

  void close()
  {
    if (_pSocket)
    {
      boost::system::error_code ignored_ec;
      _pSocket->shutdown(boost::asio::ip::tcp::socket::shutdown_both, ignored_ec);
      _pSocket->close(ignored_ec);
      _isConnected = false;
      OS_LOG_INFO(FAC_NET, CLASS_INFO() "- socket deleted.");
    }
  }

  bool timedWaitUntilDataAvailable(boost::function<void(const boost::system::error_code&)> onTimeoutCb, int timeoutMs, short int requestedEvents)
  {
    int error = 0;
    bool ret = false;
    int nativeSocket = _pSocket->native();

    struct pollfd fds[1] = {{nativeSocket, requestedEvents, 0}};


    int pollResult = poll(fds, sizeof(fds) / sizeof(fds[0]), timeoutMs);
    if (1 == pollResult)
    {
      if (fds[0].revents & POLLERR)
      {
        error = errno;
      }
      else if (fds[0].revents & requestedEvents)
      {
        ret = true;
      }
      else
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO()
            << "unexpected return from poll(): pollResult = " << pollResult
            << ", fds[0].revents =" << fds[0].revents);
      }
    }
    else if(0 == pollResult)
    { // timeout
      const boost::system::error_code e;

      onTimeoutCb(e);
      error = ETIMEDOUT;
    }
    else
    {
      error = errno;
    }

    if (0 != error)
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
          << "(" << nativeSocket << ", " << timeoutMs << " ms) error: " <<
          error << "=" <<  strerror(error));
    }

    return ret;
  }

  bool timedWaitUntilReadDataAvailable()
  {
    // check for normal or out-of-band
    return timedWaitUntilDataAvailable(boost::bind(&BlockingTcpClient::onReadTimeout, this, _1),
        _readTimeout,
        POLLIN | POLLPRI);
  }

  bool timedWaitUntilWriteDataAvailable()
  {
    return timedWaitUntilDataAvailable(boost::bind(&BlockingTcpClient::onWriteTimeout, this, _1),
        _writeTimeout,
        POLLOUT);
  }

  unsigned long getNextReadSize()
  {
    short version = 1;
    bool hasVersion = false;
    bool hasKey = false;
    unsigned long remoteLen = 0;

    while (!hasVersion || !hasKey)
    {
      short remoteVersion;
      short remoteKey;

      //TODO: Refactor the code below to do one read for the three fields
      //
      // Read the version (must be 1)
      //
      while (true)
      {

        boost::system::error_code ec;
        if (false == timedWaitUntilReadDataAvailable())
        {
          OS_LOG_ERROR(FAC_NET, CLASS_INFO()
              << "timedWaitUntilReadDataAvailable failed: "
              << "Unable to read version");

          _isConnected = false;
          return 0;
        }

        _pSocket->read_some(boost::asio::buffer((char*)&remoteVersion, sizeof(remoteVersion)), ec);
        if (ec)
        {
          if (boost::asio::error::eof == ec)
          {
            OS_LOG_ERROR(FAC_NET, CLASS_INFO() "remote closed the connection, read_some error: " << ec.message());
          }
          else
          {
            OS_LOG_INFO(FAC_NET, CLASS_INFO()
                << "Unable to read version "
                << "ERROR: " << ec.message());
          }

          _isConnected = false;
          return 0;
        }
        else
        {
          if (remoteVersion == version)
          {
            hasVersion = true;
            break;
          }
        }
      }

      while (true)
      {

        boost::system::error_code ec;
        if (false == timedWaitUntilReadDataAvailable())
        {
          OS_LOG_ERROR(FAC_NET, CLASS_INFO()
              << "timedWaitUntilReadDataAvailable failed: "
              << "Unable to read secret key");

          _isConnected = false;
          return 0;
        }

        _pSocket->read_some(boost::asio::buffer((char*)&remoteKey, sizeof(remoteKey)), ec);
        if (ec)
        {
          if (boost::asio::error::eof == ec)
          {
            OS_LOG_ERROR(FAC_NET, CLASS_INFO() "remote closed the connection, read_some error: " << ec.message());
          }
          else
          {
            OS_LOG_INFO(FAC_NET, CLASS_INFO()
                << "Unable to read secret key "
                << "ERROR: " << ec.message());
          }

          _isConnected = false;
          return 0;
        }
        else
        {
          if (remoteKey >= SQA_KEY_MIN && remoteKey <= SQA_KEY_MAX)
          {
            hasKey = true;
            break;
          }
        }
      }
    }

    boost::system::error_code ec;
    if (false == timedWaitUntilReadDataAvailable())
    {
      OS_LOG_ERROR(FAC_NET, CLASS_INFO()
          << "timedWaitUntilReadDataAvailable failed: "
          << "Unable to read secret packet length");

      _isConnected = false;
      return 0;
    }

    _pSocket->read_some(boost::asio::buffer((char*)&remoteLen, sizeof(remoteLen)), ec);
    if (ec)
    {
      if (boost::asio::error::eof == ec)
      {
        OS_LOG_ERROR(FAC_NET, CLASS_INFO() "remote closed the connection, read_some error: " << ec.message());
      }
      else
      {
        OS_LOG_INFO(FAC_NET, CLASS_INFO()
            << "Unable to read secret packet length "
            << "ERROR: " << ec.message());
      }

      _isConnected = false;
      return 0;
    }

    return remoteLen;
  }

private:
  struct ConnectTimer
  {
    ConnectTimer(BlockingTcpClient* pOwner) : _pOwner(pOwner)
    {
      _pOwner->startConnectTimer();
    }

    ~ConnectTimer()
    {
      _pOwner->cancelConnectTimer();
    }

    BlockingTcpClient* _pOwner;
  };

  const std::string& className()
  {
    static const std::string className("StateQueueClient::BlockingTcpClient");
    return className;
  }

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


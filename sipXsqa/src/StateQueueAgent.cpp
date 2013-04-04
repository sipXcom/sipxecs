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

#include "sqa/SQADefines.h"
#include "sqa/StateQueueAgent.h"
#include "sqa/StateQueueNotification.h"
#include "os/OsLogger.h"

static const char* REDIS_CHANNEL = REDIS_EVENT_CHANNEL;

StateQueueAgent::StateQueueAgent(const std::string& agentId, ServiceOptions& options) :
    _agentId(agentId),
  _options(options),
  _pIoServiceThread(0),
  _ioService(),
  _publisher(new StateQueuePublisher(this)),
  _queueWorkSpaceIndex(REDIS_STATEQUEUE_WORKSPACE),
  _listener(this),
  _inactivityThreshold(60),
  _terminated(false)
{
    //TODO: REFACTOR this in case options do not exist and getOptions returns false
    std::string port;
    std::string address;
    _options.getOption("zmq-subscription-address", address);
    _options.getOption("zmq-subscription-port", port);
    _publisherAddress.append("tcp://").append(address).append(":").append(port);

    OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
        <<" publisher address is:" << _publisherAddress);

    OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
        << " StateQueueAgent CREATED.");
}

StateQueueAgent::~StateQueueAgent()
{
  stop();

  if (_publisher)
  {
    delete _publisher;
    _publisher = NULL;
  }

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
      << " DESTROYED.");
}

void StateQueueAgent::run()
{
  assert(!_pIoServiceThread);
  assert(!_publisherAddress.empty());
  _pIoServiceThread = new boost::thread(boost::bind(&StateQueueAgent::internal_run_io_service, this));
}

void StateQueueAgent::onRedisWatcherEvent(const std::vector<std::string>& event)
{
  if (event.size() == 3)
  {
    OS_LOG_INFO(FAC_NET, "StateQueueAgent::onRedisWatcherEvent: "
            << event[0] << " | "
            << event[1] << " | "
            << event[2]);
    
    if (event[0] == "message" && event[1] == REDIS_CHANNEL)
    {
      StateQueueRecord rec;
      rec.id = REDIS_CHANNEL;
      rec.data = event[2];
      publish(rec);
    }
  }
}

void StateQueueAgent::onRedisWatcherConnect(int status)
{
  OS_LOG_NOTICE(FAC_NET, LOG_TAG_WID(_agentId)
      << " status=" << status);
}

void StateQueueAgent::onRedisWatcherDisconnect(int status)
{
  OS_LOG_NOTICE(FAC_NET, LOG_TAG_WID(_agentId)
      << " status=" << status);
}

void StateQueueAgent::internal_run_io_service()
{
  if (!_publisherAddress.empty())
  {
    _publisher->setBindAddress(_publisherAddress);
    _publisher->run();
  }

  //
  // Connect the redis client
  //
//  _redisWatcher.connect(
//    boost::bind(&StateQueueAgent::onRedisWatcherConnect, this, _1),
//    boost::bind(&StateQueueAgent::onRedisWatcherDisconnect, this, _1),
//    boost::bind(&StateQueueAgent::onRedisWatcherEvent, this, _1)
//  );
//
//  std::vector<std::string> watch;
//  watch.push_back("SUBSCRIBE");
//  watch.push_back(REDIS_CHANNEL);
//  _redisWatcher.asyncCommand(watch);

  //_redisWatcher.run();
  
  _listener.run();
  _ioService.run();
}


void StateQueueAgent::stop()
{
  if (_terminated)
    return;

  _terminated = true;

  //
  // Unsubscribe from redis channel
  //
  std::vector<std::string> unsubscribe;
  unsubscribe.push_back("UNSUBSCRIBE");
  unsubscribe.push_back(REDIS_CHANNEL);
  //_redisWatcher.stop();


  _dataStore.stop();
  _ioService.stop();
  if (_pIoServiceThread && _pIoServiceThread->joinable())
    _pIoServiceThread->join();
  delete _pIoServiceThread;
  _pIoServiceThread = 0;

  _publisher->stop();
}

void StateQueueAgent::onIncomingConnection(StateQueueConnection::Ptr conn)
{
}

void StateQueueAgent::onDestroyConnection(StateQueueConnection::Ptr conn)
{
  //
  // Publish connection destruction to who ever wants to know
  //
  if (conn->isAlphaConnection() && !conn->getApplicationId().empty())
  {
    StateQueueRecord record;

    fillEventRecord(record, *conn, ConnectionEventTerminate);

    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
            << " Publish record: "
            << " record.id: " << record.id
            << " record.data: " << record.data);
    publish(record);
  }
}

void StateQueueAgent::onIncomingRequest(StateQueueConnection& conn, const char* bytes, std::size_t bytes_transferred)
{
  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
      << " processing " << bytes_transferred << " bytes.");
  std::string packet(bytes, bytes_transferred);
  StateQueueMessage message(packet);
  StateQueueMessage::Type type;
  type = message.getType();

  std::string id;
  std::string appId;

  if (!message.get("message-app-id", appId) || appId.empty())
  {
    sendErrorResponse(type, conn, id, "Missing required argument message-app-id.");
    return;
  }

  if (type != StateQueueMessage::Ping)
  {
      if (!message.get("message-id", id) || id.empty())
      {
        OS_LOG_INFO(FAC_NET, packet);
        sendErrorResponse(message.getType(), conn, "unknown-id", "Missing required argument message-id.");
        return;
      }

    conn.setApplicationId(appId);

    if (conn.isAlphaConnection() && !conn.isCreationPublished())
    {
      StateQueueRecord record;

      fillEventRecord(record, conn, ConnectionEventEstablished);

      // Publish connection up from client with appId
      OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
              << " Publish record: "
              << " record.id: " << record.id
              << " record.data: " << record.data);
      publish(record);

      //
      // Mark it as published
      //
      conn.setCreationPublished();
    }
  }

  switch (type)
  {
    case StateQueueMessage::Signin:
      handleSignin(conn, message, id, appId);
      break;
    case StateQueueMessage::Logout:
      handleLogout(conn, message, id, appId);
      break;
    case StateQueueMessage::Enqueue:
      handleEnqueue(conn, message, id, appId);
      break;
    case StateQueueMessage::EnqueueAndPublish:
      handleEnqueueAndPublish(conn, message, id, appId);
      break;
    case StateQueueMessage::Publish:
      handlePublish(conn, message, id, appId);
      break;
    case StateQueueMessage::PublishAndSet:
      handlePublishAndSet(conn, message, id, appId);
      break;
    case StateQueueMessage::Pop:
      handlePop(conn, message, id, appId);
      break;
    case StateQueueMessage::Erase:
      handleErase(conn, message, id, appId);
      break;
    case StateQueueMessage::Persist:
      handlePersist(conn, message, id, appId);
      break;
    case StateQueueMessage::Set:
      handleSet(conn, message, id, appId);
      break;
    case StateQueueMessage::Get:
      handleGet(conn, message, id, appId);
      break;
    case StateQueueMessage::MapSet:
      handleMapSet(conn, message, id, appId);
      break;
    case StateQueueMessage::MapGet:
      handleMapGet(conn, message, id, appId);
      break;
    case StateQueueMessage::MapGetMultiple:
      handleMapGetMultiple(conn, message, id, appId);
      break;
    case StateQueueMessage::MapGetInc:
      handleMapGetInc(conn, message, id, appId);
      break;
    case StateQueueMessage::Remove:
      handleRemove(conn, message, id, appId);
      break;
    case StateQueueMessage::Ping:
      handlePing(conn, message, appId);
      break;
    default:
      sendErrorResponse(type, conn, id, "Invalid Command!");
  }
}

void StateQueueAgent::sendErrorResponse(
  StateQueueMessage::Type type,
  StateQueueConnection& conn,
  const std::string& messageId,
  const std::string& error)
{
  OS_LOG_WARNING(FAC_NET, LOG_TAG_WID(_agentId)
      << " Message-id: " << messageId << " Error: " << error);
  StateQueueMessage response;
  response.setType(type);
  response.set("message-id", messageId);
  response.set("message-response", "error");
  response.set("message-error", error);
  conn.write(response.data());
}

void StateQueueAgent::sendOkResponse(StateQueueMessage::Type type, StateQueueConnection& conn, const std::string& messageId, const std::string& messageData)
{
  StateQueueMessage response;
  response.setType(type);
  response.set("message-response", "ok");
  response.set("message-id", messageId);
  if (!messageData.empty())
    response.set("message-data", messageData);
  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
      << " Message-id: " << messageId << " Ok: " << messageData);
  conn.write(response.data());
}

void StateQueueAgent::handlePing(StateQueueConnection& conn, StateQueueMessage& message, const std::string& appId)
{
    //
    // This is a PING request
    //
    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
        << " Keep-alive request received from " << conn.getRemoteAddress() << ":" << conn.getRemotePort());

    StateQueueRecord record;
    fillEventRecord(record, conn, ConnectionEventKeepAlive);

    // All ping requests are published
    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " Publish record: "
          << " record.id: " << record.id
          << " record.data: " << record.data);
    publish(record);

    StateQueueMessage response;
    response.setType(StateQueueMessage::Pong);
    conn.write(response.data());
}

void StateQueueAgent::handleEnqueueAndPublish(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
    std::string data;
    int expires = -1;

    if (!message.get("message-data",  data) || data.empty())
    {
        sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data.");
        return;
    }

    if (!message.get("message-expires",  expires) || expires <= 0)
    {
        sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
        return;
    }

    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
            << " Received new command ENQUEUE.AND.PUBLISH "
            << " message-id: " << id
            << " message-app-id: " << appId
            << " message-data: " << data
            << " message-expires: " << expires);

    StateQueueRecord record;
    fillEventRecord(record, id, data, expires, false);
    enqueue(record);


    fillEventRecord(record, id, data, expires, true);
    publish(record);

  StateQueueMessage response;
  response.setType(message.getType());
  response.set("message-response", "ok");
  conn.write(response.data());
}

void StateQueueAgent::handleEnqueue(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
    std::string data;
    int expires = 0;
  if (!message.get("message-data",  data) || data.empty())
  {
      sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data.");
      return;
  }

  if (!message.get("message-expires",  expires) || expires <= 0)
  {
      sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
      return;
  }


  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command ENQUEUE "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data: " << data
          << " message-expires: " << expires);

  StateQueueRecord record;
  fillEventRecord(record, id, data, expires, false);
  enqueue(record);

  StateQueueMessage response;
  response.setType(message.getType());
  response.set("message-response", "ok");
  conn.write(response.data());
}

void StateQueueAgent::enqueue(StateQueueRecord& record)
{
  //
  // persist the new record and tell everyone about it.
  //
  if (!record.expires)
    record.expires = 30;

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
        << " record.id: " << record.id
        << " record.data: " << record.data
        << " record.expires: " << record.expires);

  _dataStore.set(this->_queueWorkSpaceIndex, record, record.expires);
  _publisher->publish(record);
}


void StateQueueAgent::handlePublish(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
    std::string data;
    if (!message.get("message-data",  data) || data.empty())
    {
      sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data.");
      return;
    }

    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
            << " Received new command PUBLISH: "
            << " message-id: " << id
            << " message-app-id: " << appId
            << " message-data: " << data);

  StateQueueRecord record;
  fillEventRecord(record, id, data, true);

  publish(record);

  bool noresponse = false;
  if (message.get("noresponse", noresponse) && noresponse)
  {
    noresponse = true;
  }

  if (!noresponse)
  {
    StateQueueMessage response;
    response.setType(message.getType());
    response.set("message-response", "ok");
    conn.write(response.data());
  }
}

void StateQueueAgent::handlePublishAndSet(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  int expires = 0;
  if (!message.get("message-expires",  expires) || expires <= 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
    return;
  }

  std::string dataId;
  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  std::string data;
  if (!message.get("message-data",  data) || data.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  if (((unsigned)workspace) == _queueWorkSpaceIndex)
  {
    sendErrorResponse(message.getType(), conn, id, "Persisting to default workspace is now allowed.");
    return;
  }

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command PUBLISH AND PERSIST: "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data-id: " << dataId
          << " message-data: " << data
          << " workspace: " << workspace
          << " message-expires: " << expires);

  StateQueueRecord record(dataId, data, expires);

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " Will PUBLISH AND PERSIST: "
          << " record.id: " << id
          << " record.data: " << data
          << " record.expires: " << expires);

  set(dataId, workspace, record, expires);

  publish(record);

  StateQueueMessage response;
  response.setType(message.getType());
  response.set("message-response", "ok");
  conn.write(response.data());
}


void StateQueueAgent::publish(StateQueueRecord& record)
{
  //
  // persist the new record and tell everyne about it.
  //
  if (!record.expires)
    record.expires = 30;

  record.watcherData = true;

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
      << " [" << _agentId << "]"
          << " record.id: " << record.id
          << " record.data: " << record.data
          << " record.expires: " << record.expires);

  _publisher->publish(record);
}

void StateQueueAgent::handlePop(StateQueueConnection& conn, StateQueueMessage& message, const std::string& id, const std::string& appId)
{ 
  int expires;
  if (!message.get("message-expires",  expires) || expires <= 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
    return;
  }

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command POP. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-expires: " << expires);

  StateQueueRecord record;
  if (!pop(appId, id, record, expires))
  {
    sendErrorResponse(message.getType(), conn, id, "Message non existent or has expired.");
    return;
  }

  sendOkResponse(message.getType(), conn, id, record.data);
}

bool StateQueueAgent::pop(const std::string& appId, const std::string& id, StateQueueRecord& record, int expires)
{
  if (!_dataStore.get(_queueWorkSpaceIndex, id, record))
    return false;
  _dataStore.erase(_queueWorkSpaceIndex, id);
  record.retry++;
  record.exclude.push_back(appId);
  record.expires = expires;
  _cache.enqueue(id, record, boost::bind(&StateQueueAgent::onQueueTimeout, this, _1, _2), expires);
  return true;
}

void StateQueueAgent::handlePersist(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  int expires = -1;
  if (!message.get("message-expires",  expires) || expires <= 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
    return;
  }

  int workspace = -1;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  std::string dataId;
  if (!message.get("message-data-id", dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  if (((unsigned)workspace) == _queueWorkSpaceIndex)
  {
    sendErrorResponse(message.getType(), conn, id, "Persisting to default workspace is now allowed.");
    return;
  }
  
  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command PERSIST. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data-id: " << dataId
          << " workspace: " << workspace
          << " message-expires: " << expires);

  persist(dataId, workspace, expires);
  
  sendOkResponse(message.getType(), conn, id, "");
}

void StateQueueAgent::persist(const std::string& id, int workspaceId, int expires)
{
  StateQueueRecord record;

  boost::any cacheData;
  if (!_cache.dequeue(id, cacheData))
    return;

  record = boost::any_cast<StateQueueRecord>(cacheData);

  _dataStore.erase(_queueWorkSpaceIndex, id);
  set(id, workspaceId, record, expires);
}

void StateQueueAgent::handleSet(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  int expires;
  if (!message.get("message-expires",  expires) || expires <= 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
    return;
  }

  std::string dataId;
  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  std::string data;
  if (!message.get("message-data",  data) || data.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  if (((unsigned)workspace) == _queueWorkSpaceIndex)
  {
    sendErrorResponse(message.getType(), conn, id, "Persisting to default workspace is now allowed.");
    return;
  }
  
  StateQueueRecord record(dataId, data, expires);

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command SET: "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data-id: " << dataId
          << " workspace: " << workspace
          << " message-expires: " << expires);

  set(dataId, workspace, record, expires);

  sendOkResponse(message.getType(), conn, id, "");
}

void StateQueueAgent::set(const std::string& dataId, int workspaceId, StateQueueRecord& record, int expires)
{
  _dataStore.set(workspaceId, record, expires);
}

void StateQueueAgent::handleGet(StateQueueConnection& conn, StateQueueMessage& message, const std::string& id, const std::string& appId)
{
  std::string dataId;
  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  OS_LOG_DEBUG(FAC_NET, "StateQueueAgent::handleGet "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data-id: " << dataId
          << " workspace: " << workspace);


  StateQueueRecord record;
  if (!get(appId, dataId, workspace, record))
  {
    sendErrorResponse(message.getType(), conn, id, "Message non existent or has expired.");
    return;
  }

  sendOkResponse(message.getType(), conn, id, record.data);
}

bool StateQueueAgent::get(const std::string& appId, const std::string& dataId, int workspaceId, StateQueueRecord& record)
{
    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
            << " workspaceId: " << workspaceId
            << " dataId: " << dataId);

  return _dataStore.get(workspaceId, dataId, record);
}

void StateQueueAgent::handleMapGet(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  std::string mapId;
  std::string dataId;

  if (!message.get("message-map-id",  mapId) || mapId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-map-id.");
    return;
  }

  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " message-id: " << id
          << " message-app-id: " << appId
          << " workspace: "    << workspace
          << " message-map-id: "    << mapId
          << " message-data-id: " << dataId);


  StateQueueRecord record;
  if (!mget(appId, mapId, dataId, workspace, record))
  {
    sendErrorResponse(message.getType(), conn, id, "Message non existent or has expired.");
    return;
  }

  sendOkResponse(message.getType(), conn, id, record.data);
}

bool StateQueueAgent::mget(const std::string& appId, const std::string& mapId,
        const std::string& dataId, int workspaceId, StateQueueRecord& record)
{
    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
            << " mapId: " << mapId
            << " workspaceId: " << workspaceId
            << " dataId: " << dataId
          << " record.id: " << record.id
          << " record.data: " << record.data
          << " record.expires: " << record.expires);


  return _dataStore.mapGet(workspaceId, mapId, dataId, record);
}

void StateQueueAgent::handleMapGetMultiple(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  std::string mapId;

  if (!message.get("message-map-id",  mapId) || mapId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-map-id.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command GET. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-map-id: " << mapId
          << " workspace: " << workspace);


  StateQueueRecord record;
  if (!mgetm(appId, mapId, workspace, record))
  {
    sendErrorResponse(message.getType(), conn, id, "Message non existent or has expired.");
    return;
  }

  sendOkResponse(message.getType(), conn, id, record.data);
}

bool StateQueueAgent::mgetm(const std::string& appId, const std::string& mapId,
        int workspaceId, StateQueueRecord& record)
{
  StateQueuePersistence::RecordVector records;
  if (!_dataStore.mapGet(workspaceId, mapId, records))
    return false;

  StateQueueMessage message;
  message.setType(StateQueueMessage::Data);
  for (StateQueuePersistence::RecordVector::const_iterator iter = records.begin(); iter != records.end(); iter++)
    message.set(iter->id.c_str(), iter->data);
  record.data = message.data();
  record.id = mapId;
  return true;
}

void StateQueueAgent::handleMapGetInc(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  std::string mapId;
  std::string dataId;

  if (!message.get("message-map-id",  mapId) || mapId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-map-id.");
    return;
  }

  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << "Received new command GET. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data-id: " << dataId
          << " workspace: " << workspace);


  StateQueueRecord record;
  if (!mgeti(appId, mapId, dataId, workspace, record))
  {
    sendErrorResponse(message.getType(), conn, id, "Message non existent or has expired.");
    return;
  }

  sendOkResponse(message.getType(), conn, id, record.data);
}

bool StateQueueAgent::mgeti(const std::string& appId, const std::string& mapId,
        const std::string& dataId, int workspaceId, StateQueueRecord& record)
{
  return _dataStore.mapGetInc(workspaceId, mapId, dataId, record);
}

void StateQueueAgent::handleMapSet(StateQueueConnection& conn, StateQueueMessage& message,
  const std::string& id, const std::string& appId)
{
  int expires;
  if (!message.get("message-expires",  expires) || expires <= 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument expires.");
    return;
  }

  std::string mapId;
  if (!message.get("message-map-id",  mapId) || mapId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-map-id.");
    return;
  }

  std::string dataId;
  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  std::string data;
  if (!message.get("message-data",  data) || data.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data.");
    return;
  }

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  if (((unsigned)workspace) == _queueWorkSpaceIndex)
  {
    sendErrorResponse(message.getType(), conn, id, "Persisting to default workspace is now allowed.");
    return;
  }

  StateQueueRecord record(dataId, data, expires);

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " message-id: " << id
          << " message-app-id: " << appId
          << " workspace: "    << workspace
          << " message-map-id: "    << mapId
          << " message-data-id: " << dataId
          << " message-data: "    << data
          << " message-expires: " << expires);

  mset(mapId, workspace, record, expires);

  sendOkResponse(message.getType(), conn, id, "");
}

void StateQueueAgent::mset(const std::string& mapId, int workspaceId, StateQueueRecord& record, int expires)
{
    OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
            << " mapId: " << mapId
            << " workspaceId: " << workspaceId
          << " record.id: " << record.id
          << " record.data: " << record.data
          << " record.expires: " << record.expires
          << " expires: " << expires);

  _dataStore.mapSet(workspaceId, mapId, record, expires);
}

void StateQueueAgent::handleRemove(StateQueueConnection& conn, StateQueueMessage& message, const std::string& id, const std::string& appId)
{
  std::string dataId;
  if (!message.get("message-data-id",  dataId) || dataId.empty())
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument message-data-id.");
    return;
  }

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command REMOVE. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " message-data-id: " << id
          << " message-app-id: " << dataId);

  int workspace;
  if (!message.get("workspace",  workspace) || workspace < 0)
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument workspace.");
    return;
  }

  StateQueueRecord record;
  if (!remove(appId, dataId, workspace))
  {
    sendErrorResponse(message.getType(), conn, id, "Message non existent or has expired.");
    return;
  }

  sendOkResponse(message.getType(), conn, id, "");
}

bool StateQueueAgent::remove(const std::string& appId, const std::string& dataId, int workspaceId)
{
  return _dataStore.erase(workspaceId, dataId);
}


void StateQueueAgent::handleErase(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  std::string eraseId;
  if (!message.get("erase-id", eraseId))
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument erase-id.");
    return;
  }

  OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command ERASE. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " erase-id: " << eraseId);

  erase(eraseId);
  sendOkResponse(message.getType(), conn, id, "");
}

void StateQueueAgent::erase(const std::string& id)
{
  _cache.erase(id);
  _dataStore.erase(_queueWorkSpaceIndex, id);
}

void StateQueueAgent::onQueueTimeout(const std::string& id, const boost::any& data)
{

  StateQueueRecord record = boost::any_cast<const StateQueueRecord&>(data);
   
  if (record.retry < 2)
  {
    OS_LOG_INFO(FAC_NET, LOG_TAG_WID(_agentId)
          << " Message has expired in queue. "
          << " message-id: " << id
          << " retry-count: " << record.retry);
    enqueue(record);
  }
  else
  {
    OS_LOG_WARNING(FAC_NET, LOG_TAG_WID(_agentId)
          << " Message has expired in queue more than once.  Dropping. "
          << " message-id: " << id);
  }
}

void StateQueueAgent::handleSignin(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  int subscriptionExpires;
  if (!message.get("subscription-expires", subscriptionExpires))
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument subscription-expires.");
    return;
  }

  std::string subscriptionEvent;
  if (!message.get("subscription-event", subscriptionEvent))
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument subscription-event.");
    return;
  }

  std::string serviceType;
  if (!message.get("service-type", serviceType))
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument service-type.");
    return;
  }

  if (SQAUtil::getClientStr(SQAUtil::SQAClientWorker) == serviceType)
    _publisher->addSubscriber(subscriptionEvent, appId, subscriptionExpires);

  OS_LOG_NOTICE(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command SIGNIN. "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " subscription-event: " << subscriptionEvent
          << " service-type: " << serviceType
          << " subscriptionExpires: " << subscriptionExpires);

  sendOkResponse(message.getType(), conn, id, _publisherAddress);

  conn.setApplicationId(appId);

  StateQueueRecord record;
  fillEventRecord(record, conn, ConnectionEventSignin);

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << "Publish record: "
          << " record.id: " << record.id
          << " record.data: " << record.data);
  publish(record);

}

void StateQueueAgent::handleLogout(StateQueueConnection& conn, StateQueueMessage& message,
    const std::string& id, const std::string& appId)
{
  std::string subscriptionEvent;
  if (!message.get("subscription-event", subscriptionEvent))
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument subscription-event.");
    return;
  }

  std::string serviceType;
  if (!message.get("service-type", serviceType))
  {
    sendErrorResponse(message.getType(), conn, id, "Missing required argument service-type.");
    return;
  }

  if (SQAUtil::getClientStr(SQAUtil::SQAClientWorker) == serviceType)
    _publisher->removeSubscriber(subscriptionEvent, appId);

  OS_LOG_NOTICE(FAC_NET, LOG_TAG_WID(_agentId)
          << " Received new command Logout "
          << " message-id: " << id
          << " message-app-id: " << appId
          << " subscription-event: " << subscriptionEvent
          << " service-type: " << serviceType);

  sendOkResponse(message.getType(), conn, id, "bfn!");

  StateQueueRecord record;
  fillEventRecord(record, conn, ConnectionEventLogout);

  OS_LOG_DEBUG(FAC_NET, LOG_TAG_WID(_agentId)
          << " Publish record: "
          << " record.id: " << record.id
          << " record.data: " << record.data);
  publish(record);
}

void StateQueueAgent::fillEventRecord(
        StateQueueRecord &record,
        StateQueueConnection& conn,
        ConnectionEvent connnectionEvent
        )
{
    SQAUtil::generateRecordId(record.id, connnectionEvent);

    record.data = conn.getApplicationId();
    record.data += "|";
    record.data += conn.getRemoteAddress();
}

void StateQueueAgent::fillEventRecord(
        StateQueueRecord &record,
        const std::string &messageId,
        const std::string &messageData,
        int expires,
        bool watcherData
        )
{
  if (watcherData)
  {
    record.id = PublisherWatcherPrefix;
  }
  else
  {
    record.id = DealerWorkerPrefix;
  }
  record.id += messageId.substr(3);

  record.expires = expires;
  record.data = messageData;
}

void StateQueueAgent::setPublisher(StateQueuePublisher* publisher)
{
  if (_publisher)
  {
    _publisher->stop();
    delete _publisher;
  }

  _publisher = publisher;
  _publisher->run();
}

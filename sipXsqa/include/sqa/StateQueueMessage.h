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

#ifndef STATEQUEUEMESSAGE_H
#define	STATEQUEUEMESSAGE_H


#include "sqa/json/reader.h"
#include "sqa/json/writer.h"
#include "sqa/json/elements.h"
#include "utl/cJSON.h"
#include <cassert>
#include <boost/noncopyable.hpp>

class StateQueueMessage : boost::noncopyable
{
public:
  enum Type
  {
    Unknown,
    Signin,
    Logout,
    Publish, /// publish an event
    PublishAndPersist, /// Publish an event a persist it right after
    Enqueue, /// enqueue a state
    EnqueueAndPublish, /// Enqueue the message then publish it as watcher data
    Pop, /// Pop the state from the queue
    Erase, /// Erase the state after a Pop
    Remove, /// Remove the record after a Set or a Persist
    Persist, /// Persist the state
    Set, /// Set a value in cache
    Get, /// Retrieve the value from cache
    MapSet, /// Set a union item
    MapGet, /// Get a union item
    MapGetInc, /// Get incremented value of an integer (this also sets the incremented value as the new value)
    MapSetMultiple, /// set multiple union items
    MapGetMultiple, /// get multiple union items
    MapRemove, /// Remove a union item
    MapClear, /// Clear all items in a union
    Ping, /// Ping request
    Pong, /// Ping response
    Data, /// Message that contains data only
    NumType
  };

  StateQueueMessage();
  StateQueueMessage(const std::string& rawData);
  ~StateQueueMessage();


  Type getType() const;
  void setType(Type type);
  cJSON* object();

  bool get(const char* name, std::string& value) const;
  bool get(const char* name, int& value) const;
  bool get(const char* name, double& value) const;
  void set(const char* name, const std::string& value);
  void set(const char* name, int value);
  void set(const char* name, double value);


  std::string data() const;
  bool parseData(const std::string& rawData);
  bool getMap(std::map<std::string, std::string>& smap);
protected:
  mutable Type _type;
  cJSON* _pObject;
};

//
// Inlines
//

inline StateQueueMessage::StateQueueMessage() :
  _type(Unknown),
  _pObject(0)
{
  _pObject = cJSON_CreateObject();
}

inline StateQueueMessage::StateQueueMessage(const std::string& rawData) :
  _type(Unknown),
  _pObject(0)
{
  parseData(rawData);
}

inline StateQueueMessage::~StateQueueMessage()
{
  if (_pObject)
    cJSON_Delete(_pObject);
}

inline bool StateQueueMessage::parseData(const std::string& rawData)
{
  if (_pObject)
    cJSON_Delete(_pObject);
	_pObject = cJSON_Parse(rawData.c_str());
  if (!_pObject)
    return false;
  return getType() != Unknown;
}

inline std::string StateQueueMessage::data() const
{
  std::string ret;
  if (!_pObject)
    return ret;
  char* out=cJSON_Print(_pObject);
  if (out)
  {
    ret = out;
    ::free(out);
  }
  return ret;
}


inline StateQueueMessage::Type StateQueueMessage::getType() const
{
  if (!_pObject)
    return Unknown;

  if (_type != Unknown && _type < NumType)
    return _type;

  try
  {
    cJSON *stype = cJSON_GetObjectItem(_pObject,"message-type");
    std::string messageType;
    
    if (!stype || stype->type != cJSON_String || !stype->valuestring)
      messageType = "unknown";
    else
      messageType = stype->valuestring;

    if (messageType.empty() || messageType == "unknown")
    {
      _type = Unknown;
    }
    else if (messageType == "signin")
    {
      _type = Signin;
    }
    else if (messageType == "logout")
    {
      _type = Logout;
    }
    else if (messageType == "publish")
    {
      _type = Publish;
    }
    else if (messageType == "pap")
    {
      _type = PublishAndPersist;
    }
    else if (messageType == "enqueue")
    {
      _type = Enqueue;
    }
    else if (messageType == "eap")
    {
      _type = EnqueueAndPublish;
    }
    else if (messageType == "pop")
    {
      _type = Pop;
    }
    else if (messageType == "erase")
    {
      _type = Erase;
    }
    else if (messageType == "remove")
    {
      _type = Remove;
    }
    else if (messageType == "persist")
    {
      _type = Persist;
    }
    else if (messageType == "get")
    {
      _type = Get;
    }
    else if (messageType == "set")
    {
      _type = Set;
    }
    else if (messageType == "mget")
    {
      _type = MapGet;
    }
    else if (messageType == "mgeti")
    {
      _type = MapGetInc;
    }
    else if (messageType == "mset")
    {
      _type = MapSet;
    }
    else if (messageType == "mgetm")
    {
      _type = MapGetMultiple;
    }
    else if (messageType == "msetm")
    {
      _type = MapSetMultiple;
    }
    else if (messageType == "mrem")
    {
      _type = MapRemove;
    }
    else if (messageType == "mclr")
    {
      _type = MapClear;
    }
    else if (messageType == "ping")
    {
      _type = Ping;
    }
    else if (messageType == "pong")
    {
      _type = Pong;
    }
    else if (messageType == "data")
    {
      _type = Data;
    }
    else
    {
      _type = Unknown;
    }
  }catch(...)
  {
    return Unknown;
  }
  return _type;
}

inline void StateQueueMessage::setType(Type type)
{
  assert(type < NumType);
  _type = type;
  
  assert(_pObject);
  std::string newType;
  switch(type)
  {
    case NumType:
      break;
    case Signin:
      newType = "signin";
      break;
    case Logout:
      newType = "logout";
      break;
    case Publish:
      newType = "publish";
      break;
    case PublishAndPersist:
      newType = "pap";
      break;
    case Enqueue:
      newType = "enqueue";
      break;
    case EnqueueAndPublish:
      newType = "eap";
      break;
    case Pop:
      newType = "pop";
      break;
    case Erase:
      newType = "erase";
      break;
    case Remove:
      newType = "remove";
      break;
    case Persist:
      newType = "persist";
      break;
    case Get:
      newType = "get";
      break;
    case Set:
      newType = "set";
      break;
    case MapGet:
      newType = "mget";
      break;
    case MapGetInc:
      newType = "mgeti";
      break;
    case MapSet:
      newType = "mset";
      break;
    case MapGetMultiple:
      newType = "mgetm";
      break;
    case MapSetMultiple:
      newType = "msetm";
      break;
    case MapRemove:
      newType = "mrem";
      break;
    case MapClear:
     newType = "mclr";
      break;
    case Ping:
      newType = "ping";
      break;
    case Pong:
      newType = "pong";
      break;
     case Data:
      newType = "data";
      break;
    case Unknown:
      newType = "unknown";
      break;
  }
  cJSON_DeleteItemFromObject(_pObject, "message-type");
  cJSON_AddItemToObject(_pObject,"message-type", cJSON_CreateString(newType.c_str()));
}

inline bool StateQueueMessage::get(const char* name, std::string& value) const
{
  assert(_pObject);
  cJSON *stype = cJSON_GetObjectItem(_pObject,name);

  if (stype && stype->type == cJSON_String && stype->valuestring)
  {
    value = stype->valuestring;
    return true;
  }
  return false;
}

inline bool StateQueueMessage::get(const char* name, double& value) const
{
  assert(_pObject);
  cJSON *itype = cJSON_GetObjectItem(_pObject,name);

  if (itype && itype->type == cJSON_Number)
  {
    value = itype->valueint;
    return true;
  }
  return false;
}

inline bool StateQueueMessage::get(const char* name, int& value) const
{
  assert(_pObject);
  cJSON *itype = cJSON_GetObjectItem(_pObject,name);

  if (itype && itype->type == cJSON_Number)
  {
    value = itype->valueint;
    return true;
  }
  return false;
}

inline void StateQueueMessage::set(const char* name, const std::string& value)
{
  assert(_pObject);
  cJSON_DeleteItemFromObject(_pObject, name);
  cJSON_AddItemToObject(_pObject, name, cJSON_CreateString(value.c_str()));
}

inline void StateQueueMessage::set(const char* name, int value)
{
  assert(_pObject);
  cJSON_DeleteItemFromObject(_pObject, name);
  cJSON_AddNumberToObject(_pObject, name, value);
}

inline void StateQueueMessage::set(const char* name, double value)
{
  assert(_pObject);
  cJSON_DeleteItemFromObject(_pObject, name);
  cJSON_AddNumberToObject(_pObject, name, value);
}

inline cJSON* StateQueueMessage::object()
{
  return _pObject;
}

inline bool StateQueueMessage::getMap(std::map<std::string, std::string>& smap)
{
  assert(false);
  return false;
}

#endif	/* STATEQUEUEMESSAGE_H */


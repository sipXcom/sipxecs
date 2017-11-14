// Copyright (c) eZuce, Inc. All rights reserved.
// Contributed to SIPfoundry under a Contributor Agreement
//
// This software is free software; you can redistribute it and/or modify it under
// the terms of the Affero General Public License (AGPL) as published by the
// Free Software Foundation; either version 3 of the License, or (at your option)
// any later version.
//
// This software is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
// details.

#include <string>
#include "sipdb/EntityDB.h"
#include "os/OsLogger.h"

class EmergencyDB : public EntityDB
{
public:
  EmergencyDB(const MongoDB::ConnectionInfo& info) : EntityDB(info){};
  bool findE911LineIdentifier(
    const std::string& userId,
    std::string& e911,
    std::string& address,
    std::string& location);

  bool findE911InstrumentIdentifier(
    const std::string& instrument,
    std::string& e911,
    std::string& address,
    std::string& location);

  bool findE911Location(
    MongoDB::ScopedDbConnectionPtr& conn,
    const std::string& e911,
    std::string& address,
    std::string& location);
};

bool EmergencyDB::findE911Location(
  MongoDB::ScopedDbConnectionPtr& conn,
  const std::string& e911,
  std::string& address,
  std::string& location)
{
  mongo::BSONObj e911LocationQuery = BSON("ent" << "e911location" << "elin" << e911);

  mongo::BSONObjBuilder e911LocBuilder;
  BaseDB::nearest(e911LocBuilder, e911LocationQuery);

  mongo::BSONObj e911LocationObj = conn->get()->findOne(ns(), e911LocBuilder.obj(), 0, mongo::QueryOption_SlaveOk);
  if (!e911LocationObj.isEmpty())
  {
    if (e911LocationObj.hasField("addrinfo"))
    {
      address = e911LocationObj.getStringField("addrinfo");
    }

    if (e911LocationObj.hasField("loctn"))
    {
      location = e911LocationObj.getStringField("loctn");
    }
  }
  return true;
}

bool EmergencyDB::findE911LineIdentifier(
  const std::string& userId,
    std::string& e911,
    std::string& address,
    std::string& location)
{
  mongo::BSONObj query = BSON(EntityRecord::identity_fld() << userId);
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query);

  mongo::BSONObj entityObj = conn->get()->findOne(ns(), readQueryMaxTimeMS(builder.obj()), 0, mongo::QueryOption_SlaveOk);
  if (!entityObj.isEmpty())
  {
    if (entityObj.hasField("elin"))
    {
      e911 = entityObj.getStringField("elin");
      if (!e911.empty())
      {
        findE911Location(conn, e911, address, location);
      }
      conn->done();
      return !e911.empty();
    }
  }
  conn->done();
  return false;
}

bool EmergencyDB::findE911InstrumentIdentifier(
    const std::string& instrument,
    std::string& e911,
    std::string& address,
    std::string& location)
{

  OS_LOG_INFO(FAC_SIP, "");
  mongo::BSONObj query = BSON("mac" << instrument);

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));

  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query);

  mongo::BSONObj instrumentObj = conn->get()->findOne(ns(), readQueryMaxTimeMS(builder.obj()), 0, mongo::QueryOption_SlaveOk);
  if (!instrumentObj.isEmpty())
  {
    if (instrumentObj.hasField("elin"))
    {
      e911 = instrumentObj.getStringField("elin");

      if (!e911.empty())
      {
        findE911Location(conn, e911, address, location);
      }
      conn->done();
      return !e911.empty();
    }
  }
  conn->done();
  return false;
}

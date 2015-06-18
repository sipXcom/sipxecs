/*
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

#include <mongo/client/dbclient.h>

#include "os/OsTime.h"
#include "os/OsDateTime.h"
#include "os/OsLogger.h"
#include "sipdb/MongoMod.h"

// Defines the maximum delay accepted for mongo connection
#define MAX_CONNECTION_DELAY_MSEC 100

//
// Rewrites the mongo::minKey external variable defined in Mongo c++ driver
// implementation - mongo/db/jsonobj.cpp file
//
static struct MinKeyData
{
  MinKeyData()
  {
    totsize=7;
    minkey=mongo::MinKey;
    name=0;
    eoo=mongo::EOO;
  }
  int totsize;
  char minkey;
  char name;
  char eoo;
} minkeydata;
mongo::BSONObj mongoMod::minKey((const char *) &minkeydata);

const int mongoMod::EXPIRES_AFTER_SECONDS_MINIMUM_SECS = 1;

mongo::ScopedDbConnection* mongoMod::ScopedDbConnection::getScopedDbConnection(const std::string& host, double socketTimeout)
{
  OS_LOG_DEBUG(FAC_ODBC, "ScopedDbConnection::getScopedDbConnection() - host '" << host << "', socketTimeout '" << socketTimeout << "' seconds");

  OsTime start;
  OsDateTime::getCurTime(start);

  mongo::ScopedDbConnection* connection = new mongo::ScopedDbConnection(host, socketTimeout);
  if (connection->ok())
  {
    OsTime stop;
    OsDateTime::getCurTime(stop);
    int connectionDelayMsec = (stop - start).cvtToMsecs();

    OsSysLogPriority priority = PRI_DEBUG;
    if (MAX_CONNECTION_DELAY_MSEC < connectionDelayMsec)
    {
      priority = PRI_ALERT;
    }
    Os::Logger::instance().log(FAC_ODBC, priority, "ScopedDbConnection::getScopedDbConnection() - returned connection %p in %d milliseconds",
        connection->get(),
        connectionDelayMsec);
  }
  else
  {
    OS_LOG_ERROR(FAC_ODBC, "ScopedDbConnection::getScopedDbConnection() - failed for host " << host);
  }

  return connection;
}

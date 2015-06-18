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

#ifndef _MONGO_MOD_H__
#define _MONGO_MOD_H__

#include <string>
#include <mongo/client/connpool.h>


/**
 * This namespace is meant to add extra and/or backward-compatible functionality
 * for the MongoDb driver
 */
namespace mongoMod
{
  //
  // Overwrites the mongo::minKey variable which is static (hiden) starting
  // with the Mongo driver 2.6 version
  //
  extern mongo::BSONObj minKey;

  //
  // Defines the minimum number of seconds used by the mongo TTL thread, as
  // 'expireAfterSeconds' value, to expire documents
  // !Note!: This time should be 0 (as specified in
  //         http://docs.mongodb.org/manual/tutorial/expire-data/#expire-documents-at-a-specific-clock-time
  //         but due to the limitation in the C++ driver
  //         (see https://github.com/mongodb/mongo/commit/85b1a93def9416ce3fb00faa077dd871183ef39a#diff-7cd4c4d808fb366aeb57036d60ed1806R1110)
  //         we'll have to use the minimum possible value
  extern const int EXPIRES_AFTER_SECONDS_MINIMUM_SECS;

  namespace ScopedDbConnection
  {
    //
    // Returns pointer to a new Mongo connection
    // Note: The function is added here because it was included in directly in
    //        Mongo driver in the 2.4.X versions
    //
    mongo::ScopedDbConnection* getScopedDbConnection(const std::string& host, double socketTimeout = 0);
  }
}


#endif /* _MONGO_MOD_H__ */

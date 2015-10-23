/*
 * Copyright (c) 2011 eZuce, Inc. All rights reserved.
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

#include <mongo/client/connpool.h>
#include <mongo/client/dbclient.h>
#include "os/OsLogger.h"
#include "sipdb/EntityDB.h"
#include "sipdb/MongoDB.h"
#include "sipdb/MongoMod.h"
#include <boost/algorithm/string.hpp>
#include <boost/asio.hpp>
#include <vector>

using namespace std;

static const std::string ID_SIPX_PROVISION = "~~id~sipXprovision";

const std::string EntityDB::NS("imdb.entity");


static std::string validate_identity_string(const std::string& identity)
{
  //
  // There are special cases where we might want to rewrite certain identity.
  // A good example is if a set of users share a common credential like ~~id~sipXprovision
  //
  // Rewrite:  ~~id~sipXprovision~XXXYYY => ~~id~sipXprovision
  // Rewrite:  ~~id~sipXprovision~XXXYYY@somedomain.com  => ~~id~sipXprovision@somedomain.com
  //
  if (identity.find(ID_SIPX_PROVISION) == 0)
  {
    std::vector<std::string> tokens;
    boost::split(tokens, identity, boost::is_any_of("@"), boost::token_compress_on);

    if (tokens.size() <= 1)
    {
      return ID_SIPX_PROVISION;
    }
    else if (tokens.size() == 2)
    {
      return  ID_SIPX_PROVISION + std::string("@") + tokens[1];
    }
    else
    {
      OS_LOG_WARNING(FAC_ODBC, "validate_identity_string - " << identity << " appears to be malformed");
    }
  }

  return identity;
}

static bool wildcard_compare(const char* wild, const std::string& str)
{
  const char* string = str.c_str();

  const char *cp = NULL, *mp = NULL;

  while ((*string) && (*wild != '*')) {
    if ((*wild != *string) && (*wild != '?')) {
      return 0;
    }
    wild++;
    string++;
  }

  while (*string) {
    if (*wild == '*') {
      if (!*++wild) {
        return 1;
      }
      mp = wild;
      cp = string+1;
    } else if ((*wild == *string) || (*wild == '?')) {
      wild++;
      string++;
    } else {
      wild = mp;
      string = cp++;
    }
  }

  while (*wild == '*') {
    wild++;
  }
  return !*wild;
}

static bool cidr_compare(const std::string& cidr, const std::string& ip)
{
  std::vector<std::string> cidr_tokens;
  boost::split(cidr_tokens, cidr, boost::is_any_of("/-"), boost::token_compress_on);

  unsigned bits = 24;
  std::string start_ip;
  if (cidr_tokens.size() == 2)
  {
    bits = (unsigned)::atoi(cidr_tokens[1].c_str());
    start_ip = cidr_tokens[0];
  }
  else
  {
    start_ip = cidr;
  }

  boost::system::error_code ec;
  boost::asio::ip::address_v4 ipv4;
  ipv4 = boost::asio::ip::address_v4::from_string(ip, ec);
  if (!ec)
  {
    unsigned long ipv4ul = ipv4.to_ulong();
    boost::asio::ip::address_v4 start_ip_ipv4;
    start_ip_ipv4 = boost::asio::ip::address_v4::from_string(start_ip, ec);
    if (!ec)
    {
      long double numHosts = pow((long double)2, (int)(32-bits)) - 1;
      unsigned long start_ip_ipv4ul = start_ip_ipv4.to_ulong();
      unsigned long ceiling = start_ip_ipv4ul + numHosts;
      return ipv4ul >= start_ip_ipv4ul && ipv4ul <= ceiling;
    }
  }
  return false;
}

bool EntityDB::findByIdentity(const string& ident, EntityRecord& entity) const
{
  MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
  std::string identity = validate_identity_string(ident);
  
  OS_LOG_INFO(FAC_ODBC, "EntityDB::findByIdentity - Finding entity record for " << identity << " from namespace " << _ns);
  //
  // Check if we have it cache
  //
  ExpireCacheable pCacheObj = const_cast<ExpireCache&>(_cache).get(identity);
  if (pCacheObj)
  {
    OS_LOG_DEBUG(FAC_ODBC, identity << " is present in namespace " << _ns << " (CACHED)");
    entity = *pCacheObj;
    return true;
  }

  mongo::BSONObj query = BSON(EntityRecord::identity_fld() << identity);

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));

  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query);

  mongo::BSONObj entityObj = conn->get()->findOne(_ns, readQueryMaxTimeMS(builder.obj()), 0, mongo::QueryOption_SlaveOk);
  if (!entityObj.isEmpty())
  {
    OS_LOG_DEBUG(FAC_ODBC, identity << " is present in namespace " << _ns);
    entity = entityObj;
    conn->done();
    //
    // Cache the entity
    //
    const_cast<ExpireCache&>(_cache).add(identity, ExpireCacheable(new EntityRecord(entity)));
    return true;
  }

  OS_LOG_DEBUG(FAC_ODBC, identity << " is NOT present in namespace " << _ns);
  OS_LOG_INFO(FAC_ODBC, "EntityDB::findByIdentity - Unable to find entity record for " << identity << " from namespace " << _ns);
  conn->done();
  return false;
}

void EntityDB::getEntitiesByType(const std::string& entityType, Entities& entities, bool nocache)
{
  MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
  OS_LOG_INFO(FAC_ODBC, "EntityDB::getEntitiesByType - Finding entity records for type " << entityType << " from namespace " << _ns);

  //
  // Check if we have it in cache
  //
  if (!nocache)
  {
    EntityTypeCacheable pCacheObj = const_cast<EntityTypeCache&>(_typeCache).get(entityType);
    if (pCacheObj)
    {
      OS_LOG_DEBUG(FAC_ODBC, "EntityDB::getEntitiesByType - " << entityType << " is present in namespace " << _ns << " (CACHED)");
      entities = *pCacheObj;
      return;
    }
  }
  
  mongo::BSONObj query = BSON(EntityRecord::entity_fld() << entityType);

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));

  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query);

  /** query N objects from the database into an array.  makes sense mostly when you want a small number of results.  if a huge number, use
            query() and iterate the cursor.
      void findN(vector<BSONObj>& out, const string&ns, Query query, int nToReturn, int nToSkip = 0, const BSONObj *fieldsToReturn = 0, int queryOptions = 0);
    */
      
  BSONObjects objects;
  conn->get()->findN(
    objects, // out
    _ns, // ns
    readQueryMaxTimeMS(builder.obj()), // query
    1024, // nToReturn
    0, // nToSkip,
    0, // fieldsToReturn
    mongo::QueryOption_SlaveOk // queryOptions
  );

  entities.clear();
  for (BSONObjects::iterator iter = objects.begin(); iter != objects.end(); iter++)
  {
    if (!iter->isEmpty())
    {
      EntityRecord entity;
      entity = (*iter);
      entities.push_back(entity);
    }
  }
  
  if (entities.empty())
  {
    OS_LOG_DEBUG(FAC_ODBC, entityType << " is NOT present in namespace " << _ns);
    OS_LOG_INFO(FAC_ODBC, "EntityDB::getEntitiesByType - Unable to find entity record for type " << entityType << " from namespace " << _ns);
  }
  else
  {
    const_cast<EntityTypeCache&>(_typeCache).add(entityType, EntityTypeCacheable(new Entities(entities)));
    OS_LOG_DEBUG(FAC_ODBC, "EntityDB::getEntitiesByType - " << entityType << " is present in namespace " << _ns);
  }
  
  conn->done();
}

void EntityDB::getCallerLocation(CallerLocations& locations, std::string& fallbackLocation, const std::string& identity, const std::string& host, const std::string& address)
{
  Entities branches;
  getEntitiesByType(EntityRecord::entity_branch_str(), branches);
  
  OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation( identity=" << identity << ", host=" << host << ", address=" << address << ")");
  
  EntityRecord userEntity;
  if (findByIdentity(identity, userEntity) && !userEntity.allowedLocations().empty())
  {   
    //
    // Now that we have the locations for this user, check for branch associated locations
    //
    for (std::set<std::string>::iterator locations_iter = userEntity.allowedLocations().begin(); locations_iter != userEntity.allowedLocations().end(); locations_iter++)
    {
      for (Entities::iterator branch_iter = branches.begin(); branch_iter != branches.end(); branch_iter++)
      {
        if (branch_iter->location() == *locations_iter)
        {
          OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation - Setting associated locations based  on identity " << identity << " branch " << branch_iter->location()); 
          locations = branch_iter->associatedLocations();
          fallbackLocation = branch_iter->associatedLocationFallback();
          return;
        }
      }
    }
    
    return;
  }
  
  //
  // Get locations by domain or subnet
  //
  for (Entities::iterator host_iter = branches.begin(); host_iter != branches.end(); host_iter++) // this loop iterates the branch record we have queried
  {
    if (!host.empty() && !host_iter->loc_restr_dom().empty() && !host_iter->inboundAssociatedLocations().empty()) // only if locations and wild card matching is specified by the branch
    {
      for (EntityRecord::LocationDomain::iterator domainIter = host_iter->loc_restr_dom().begin(); domainIter != host_iter->loc_restr_dom().end(); domainIter++)
      {
        if (wildcard_compare(domainIter->c_str(), host))
        {
          OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation - Inserting location based  on " << *domainIter << " wildcard match for domain " << host); 
          locations = host_iter->inboundAssociatedLocations();
          fallbackLocation = host_iter->associatedLocationFallback();
          return;
        }
        else
        {
          OS_LOG_DEBUG(FAC_ODBC,"EntityDB::getCallerLocation - " << host_iter->location() << "/" << *domainIter << " does not own domain " << host);
        }
      }
    }
     
    if (!address.empty() && !host_iter->loc_restr_sbnet().empty() && !host_iter->inboundAssociatedLocations().empty())
    {
      for (EntityRecord::LocationSubnet::iterator subnetIter = host_iter->loc_restr_sbnet().begin(); subnetIter != host_iter->loc_restr_sbnet().end(); subnetIter++)
      {
        if (cidr_compare(*subnetIter, address))
        {
          OS_LOG_INFO(FAC_ODBC, "EntityDB::getCallerLocation - Inserting location based  on " << *subnetIter << " CIDR match for address " << address); 
          locations = host_iter->inboundAssociatedLocations();
          fallbackLocation = host_iter->associatedLocationFallback();
          return;
        }
        else
        {
          OS_LOG_DEBUG(FAC_ODBC,"EntityDB::getCallerLocation - " << host_iter->location() << "/" << *subnetIter << " does not own address " << address);
        }
      }
    }
  }
}

bool EntityDB::findByUserId(const string& uid, EntityRecord& entity) const
{
  MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
  
  std::string userId = validate_identity_string(uid);
  
  OS_LOG_INFO(FAC_ODBC, "EntityDB::findByUserId - Finding entity record for " << userId << " from namespace " << _ns);
  ExpireCacheable pCacheObj = const_cast<ExpireCache&>(_cache).get(userId);
  if (pCacheObj)
  {
    OS_LOG_DEBUG(FAC_ODBC, userId << " is present in namespace " << _ns << " (CACHED)");
    entity = *pCacheObj;
    return true;
  }

  mongo::BSONObj query = BSON(EntityRecord::userId_fld() << userId);
  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query);
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));

  mongo::BSONObj entityObj = conn->get()->findOne(_ns, readQueryMaxTimeMS(builder.obj()), 0, mongo::QueryOption_SlaveOk);
  if (!entityObj.isEmpty())
  {
    entity = entityObj;
    conn->done();
    //
    // Cache the entity
    //
    const_cast<ExpireCache&>(_cache).add(userId, ExpireCacheable(new EntityRecord(entity)));
    
    return true;
  }
  
  OS_LOG_INFO(FAC_ODBC, "EntityDB::findByUserId - Unable to find entity record for " << userId << " from namespace " << _ns);
  conn->done();
  return false;
}

bool EntityDB::findByIdentityOrAlias(const Url& uri, EntityRecord& entity) const
{
	UtlString identity;
	UtlString userId;
	uri.getIdentity(identity);
	uri.getUserId(userId);
	return findByIdentityOrAlias(identity.str(), userId.str(), entity);
}

bool EntityDB::findByIdentityOrAlias(const string& identity, const string& alias,
		EntityRecord& entity) const
{
	bool found = false;
	if (!identity.empty())
		found = findByIdentity(identity, entity);

	if (!found && !alias.empty())
		found = findByAliasUserId(alias, entity);

	return found;
}

bool EntityDB::findByAliasUserId(const string& alias, EntityRecord& entity) const
{

  MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
  
  ExpireCacheable pCacheObj = const_cast<ExpireCache&>(_cache).get(alias);
  if (pCacheObj)
  {
    OS_LOG_DEBUG(FAC_ODBC, "EntityDB::findByAliasUserId - " << alias << " is present in namespace " << _ns << " (CACHED)");
    entity = *pCacheObj;
    return true;
  }

  mongo::BSONObj query = BSON( EntityRecord::aliases_fld() <<
  BSON_ELEM_MATCH( BSON(EntityRecord::aliasesId_fld() << alias) ) );

  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query);
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));

  mongo::BSONObj entityObj = conn->get()->findOne(_ns, readQueryMaxTimeMS(builder.obj()), 0, mongo::QueryOption_SlaveOk);
  if (!entityObj.isEmpty())
  {
    entity = entityObj;
    conn->done();
    //
    // Cache the entity
    //
    const_cast<ExpireCache&>(_cache).add(alias, ExpireCacheable(new EntityRecord(entity)));
    return true;
  }
  OS_LOG_INFO(FAC_ODBC, "EntityDB::findByAliasUserId - Unable to find entity record for alias " << alias << " from namespace " << _ns);
  conn->done();
  return false;
}

bool EntityDB::findByAliasIdentity(const std::string& identity, EntityRecord& entity) const
{
  std::vector<std::string> tokens;
  boost::split(tokens, identity, boost::is_any_of("@"), boost::token_compress_on);
  if (tokens.size() != 2)
    return false;
  std::string userId = tokens[0];
  std::string host = tokens[1];
  if (!findByAliasUserId(userId, entity))
    return false;
  size_t i = entity.identity().rfind(host);
  return (i != std::string::npos) && (i == (entity.identity().length() - host.length()));
}

/// Retrieve the SIP credential check values for a given identity and realm
bool EntityDB::getCredential(const Url& uri, const UtlString& realm, UtlString& userid, UtlString& passtoken,
		UtlString& authType) const
{
	UtlString identity;
	uri.getIdentity(identity);

	EntityRecord entity;
	if (!findByIdentity(identity.str(), entity))
		return false;

	if (entity.realm() != realm.str())
		return false;

	userid = entity.userId();
	passtoken = entity.password();
	authType = entity.authType();

	return true;
}

/// Retrieve the SIP credential check values for a given userid and realm
bool EntityDB::getCredential(const UtlString& userid, const UtlString& realm, Url& uri, UtlString& passtoken,
		UtlString& authType) const
{
	EntityRecord entity;
	if (!findByUserId(userid.str(), entity))
		return false;

	if (entity.realm() != realm.str())
		return false;

	uri = entity.identity().c_str();
	passtoken = entity.password();
	authType = entity.authType();

	return true;
}

void EntityDB::getAliasContacts(const Url& aliasIdentity, Aliases& aliases, bool& isUserIdentity) const
{
	UtlString alias;
	aliasIdentity.getUserId(alias);
	if (alias.isNull())
		return;

  UtlString identity;
	aliasIdentity.getIdentity(identity);
  if (identity.isNull())
		return;

	EntityRecord entity;
	if (findByAliasIdentity(identity.str(), entity))
	{
		Aliases result = entity.aliases();
		for (Aliases::iterator iter = result.begin(); iter != result.end(); iter++)
		{
			if (iter->id == alias.data())
				aliases.push_back(*iter);
		}
		isUserIdentity = !entity.realm().empty() && !entity.password().empty();
	}
}

bool EntityDB::findByIdentity(const Url& uri, EntityRecord& entity) const
{
	UtlString identity;
	uri.getIdentity(identity);
	return findByIdentity(identity.str(), entity);
}


bool  EntityDB::tail(std::vector<std::string>& opLogs) {
  // minKey is smaller than any other possible value

  MongoDB::ReadTimer readTimer(const_cast<EntityDB&>(*this));
  
  static bool hasLastTailId = false;
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString()));
  if (!hasLastTailId)
  {
    mongo::Query query = QUERY( "_id" << mongo::GT << _lastTailId
          << "ns" << NS);

    mongo::BSONObjBuilder builder;
    BaseDB::nearest(builder, query.obj);

    // natural order
    //builder.append("orderby", BSON("$natural" << 1));

    std::auto_ptr<mongo::DBClientCursor> c =
      conn->get()->query("local.oplog", builder.obj(), 0, 0, 0,
      mongo::QueryOption_CursorTailable | mongo::QueryOption_AwaitData | mongo::QueryOption_SlaveOk);
    while(true)
    {
      if( !c->more() )
      {
        if( c->isDead() )
        {
          // we need to requery
          conn->done();
          return false;
        }
        // No need to wait here, cursor will block for several sec with _AwaitData
        break;
      }
      mongo::BSONObj o = c->next();
      _lastTailId = o["_id"];
      hasLastTailId = true;
    }
  }

  mongo::Query query = QUERY( "_id" << mongo::GT << _lastTailId
          << "ns" << NS);
  mongo::BSONObjBuilder builder;
  BaseDB::nearest(builder, query.obj);

  // natural order
  //builder.append("orderby", BSON("$natural" << 1));

  // capped collection insertion order

  std::auto_ptr<mongo::DBClientCursor> c =
    conn->get()->query("local.oplog", builder.obj(), 0, 0, 0,
        mongo::QueryOption_CursorTailable | mongo::QueryOption_AwaitData | mongo::QueryOption_SlaveOk);
  while(true)
  {
    if( !c->more() )
    {
      if( c->isDead() )
      {
        // we need to requery
        conn->done();
        return false;
      }
      // No need to wait here, cursor will block for several sec with _AwaitData
      conn->done();
      return !opLogs.empty();
    }
    mongo::BSONObj o = c->next();
    _lastTailId = o["_id"];
    opLogs.push_back(o.toString());
  }
  conn->done();
  return true;
}



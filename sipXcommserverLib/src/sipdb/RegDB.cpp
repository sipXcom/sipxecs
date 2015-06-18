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

#include <fstream>
#include <mongo/client/dbclient.h>
#include <mongo/client/connpool.h>
#include <os/OsDateTime.h>
#include <os/OsLogger.h>
#include "sipdb/RegDB.h"
#include "sipdb/RegExpireThread.h"
#include "sipdb/MongoMod.h"

using namespace std;

const string RegDB::NS("node.registrar");

RegDB* RegDB::CreateInstance(bool ensureIndexes) {
   RegDB* lRegDb = NULL;

   MongoDB::ConnectionInfo local = MongoDB::ConnectionInfo::localInfo();
   if (!local.isEmpty()) {
     Os::Logger::instance().log(FAC_SIP, PRI_INFO, "Regional database defined");
     Os::Logger::instance().log(FAC_SIP, PRI_INFO, local.getConnectionString().toString().c_str());
     lRegDb = new RegDB(local);

     // ensure indexes, if requested
     if (ensureIndexes)
     {
       lRegDb->ensureIndexes();
     }
   } else {
     Os::Logger::instance().log(FAC_SIP, PRI_INFO, "No regional database found");
   }

   MongoDB::ConnectionInfo global = MongoDB::ConnectionInfo::globalInfo();
   RegDB* regDb = new RegDB(global, lRegDb);

   // ensure indexes, if requested
   if (ensureIndexes)
   {
     regDb->ensureIndexes();
   }
   return regDb;
}

//
// Creates/updates the indexes needed for RegDB
//
void RegDB::ensureIndexes(mongo::DBClientBase* client)
{
  MongoDB::ScopedDbConnectionPtr conn(0);
  mongo::DBClientBase* clientPtr = client;

  OS_LOG_INFO(FAC_SIP, "RegDB::ensureIndexes "
                        << "existing client: " << (client ? "yes" : "no")
                        << ", expireGracePeriod: " << _expireGracePeriod
                        << ", expirationTimeIndexTTL: " << _expirationTimeIndexTTL);

  // in case no client was provided, create a new connection
  if (!clientPtr)
  {
    conn.reset(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    clientPtr = conn->get();
  }

  clientPtr->ensureIndex(_ns, BSON(RegBinding::identity_fld() << 1 ));

  // shape the new expirationtime index TTL
  int newExpirationTimeIndexTTL = (mongoMod::EXPIRES_AFTER_SECONDS_MINIMUM_SECS > static_cast<int>(_expireGracePeriod)) ? mongoMod::EXPIRES_AFTER_SECONDS_MINIMUM_SECS : static_cast<int>(_expireGracePeriod);

  // Note: Since we're not allowed to create the same index using different parameters,
  //       we'll have to drop the existing index in case we're setting it for the first
  //       time or in case the expireGracePeriod was changed
  if (newExpirationTimeIndexTTL != _expirationTimeIndexTTL)
  {
    _expirationTimeIndexTTL = newExpirationTimeIndexTTL;
    safeDropIndex(clientPtr, RegBinding::expirationTime_fld());
  }

  safeEnsureTTLIndex(clientPtr, RegBinding::expirationTime_fld(), _expirationTimeIndexTTL);

  // close the connection, if it was created
  if (conn)
  {
    conn->done();
  }
}

void RegDB::updateBinding(const RegBinding::Ptr& pBinding)
{
	updateBinding(*(pBinding.get()));
}

void RegDB::updateBinding(RegBinding& binding)
{
	if (_local != NULL) {
		_local->updateBinding(binding);
		return;
	}
  
  MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));
  
	if (binding.getTimestamp() == 0)
	{
		binding.setTimestamp(OsDateTime::getSecsSinceEpoch());
	}

	if (binding.getLocalAddress().empty())
	{
		string serverId = _localAddress;
		binding.setLocalAddress(serverId);
	}
  
  if (binding.getBinding().empty())
  {
    Url curl(binding.getContact().c_str());
    UtlString hostPort;
    UtlString user;
    curl.getHostWithPort(hostPort);
    curl.getUserId(user);
    
    std::ostringstream strm;
    strm << "sip:";
    if (!user.isNull())
      strm << user.data() << "@";
    strm << hostPort.data();
    
    binding.setBinding(strm.str());
  }

  binding.setShardId(getShardId());

  mongo::BSONObj query = BSON(
        RegBinding::identity_fld() << binding.getIdentity() <<
        RegBinding::contact_fld() << binding.getContact() <<
        RegBinding::shardId_fld() << binding.getShardId());

  bool isExpired = (binding.getExpirationTime() == 0);
  binding.setExpired(isExpired);

  mongo::BSONObj update = binding.toBSONObj();

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
  mongo::DBClientBase* client = conn->get();

  client->remove(_ns, query);
  client->insert(_ns, update);
  ensureIndexes(client);

  string e = client->getLastError();
  if ( !e.empty() )
  {
    Os::Logger::instance().log(FAC_SIP, PRI_ERR, e.c_str());
  }
  else
  {
    Os::Logger::instance().log(FAC_SIP, PRI_DEBUG, "Save reg ok");
  }

	conn->done();
}

void RegDB::expireOldBindings(const string& identity, const string& callId, unsigned int cseq,
		unsigned long timeNow)
{
	if (_local != NULL) {
		_local->expireOldBindings(identity, callId, cseq, timeNow);
		return;
	}
  
  MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));
	mongo::BSONObj query = BSON(
	    RegBinding::identity_fld() << identity <<
	    RegBinding::callId_fld() << callId <<
	    RegBinding::cseq_fld() << BSON_LESS_THAN(cseq) <<
	    RegBinding::shardId_fld() << getShardId());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    mongo::DBClientBase* client = conn->get();

	client->remove(_ns, query);
	ensureIndexes(client);

	conn->done();
}

void RegDB::expireAllBindings(const string& identity, const string& callId, unsigned int cseq,
		unsigned long timeNow)
{
	if (_local != NULL) {
		_local->expireAllBindings(identity, callId, cseq, timeNow);
		return;
	}
  
  MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));
	mongo::BSONObj query = BSON(
	    RegBinding::shardId_fld() << getShardId() <<
	    RegBinding::identity_fld() << identity);

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
  mongo::DBClientBase* client = conn->get();

  client->remove(_ns, query);
  ensureIndexes(client);

	conn->done();
}

void RegDB::removeAllExpired()
{
  if (_local != NULL)
  {

    _local->removeAllExpired();
    return;
  }

  unsigned long timeNow = OsDateTime::getSecsSinceEpoch() - _expireGracePeriod;
  
  
  OS_LOG_INFO(FAC_SIP, "RegDB::removeAllExpired INVOKED for shard == " << getShardId() << " and expireTime <= " << timeNow << " gracePeriod: " << _expireGracePeriod << " sec");

  MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));
  mongo::BSONObj query = BSON(
            RegBinding::shardId_fld() << getShardId() <<
            RegBinding::expirationTime_fld() << BSON_LESS_THAN_EQUAL(BaseDB::dateFromSecsSinceEpoch(timeNow)));

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
  mongo::DBClientBase* client = conn->get();

  client->remove(_ns, query);
  ensureIndexes(client);

  conn->done();
}

bool RegDB::isOutOfSequence(const string& identity, const string& callId, unsigned int cseq) const
{
    // Remove this method altogether?!?!? -- Conversation between douglas and joegen on 6/18/13
	return false;
}

bool RegDB::isRegisteredBinding(const Url& curl, bool preferPrimary)
{
  bool isRegistered = false;
  UtlString hostPort;
  UtlString user;
  curl.getHostWithPort(hostPort);
  curl.getUserId(user);

  std::ostringstream binding;
  binding << "sip:";
  if (!user.isNull())
    binding << user.data() << "@";
  binding << hostPort.data();
  
  mongo::BSONObjBuilder query;
	query.append(RegBinding::binding_fld(), binding.str());

	if (_local)
  {
		preferPrimary = false;
		_local->isRegisteredBinding(curl, preferPrimary);
		query.append(RegBinding::shardId_fld(), BSON_NOT_EQUAL(_local->getShardId()));
	} 

  MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));
  
	mongo::BSONObjBuilder builder;
	if (!preferPrimary)
	  BaseDB::nearest(builder, query.obj());
	else
	  BaseDB::primaryPreferred(builder, query.obj());

	MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
	auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);

  if (!pCursor.get())
  {
    throw mongo::DBException("mongo query returned null cursor", 0);
  }

	isRegistered = pCursor->more();
	conn->done();
  
  OS_LOG_INFO(FAC_SIP, "RegDB::isRegisteredBinding returning " << (isRegistered ? "TRUE" : "FALSE") << " for binding " <<  binding.str());
   
	return isRegistered;
}


static void push_or_replace_binding(RegDB::Bindings& bindings, const RegBinding& binding)
{
  //
  // Check if the call-id or contact of this binding has been previously pushed
  //
  for (RegDB::Bindings::iterator iter = bindings.begin(); iter != bindings.end(); iter++)
  {
    if (iter->getCallId() == binding.getCallId() || iter->getContact() == binding.getContact())
    {
      //
      // This is already inserted previously most probably by local shard.
      // Check which one of them has the larger timestamp, and therefore
      // the most up-to-date
      //

      if (binding.getTimestamp() > iter->getTimestamp())
      {
        //
        // The new binding has a more recent timestamp value.  replace the old one
        //
        OS_LOG_INFO(FAC_SIP, "RegDB::findAndReplaceOlderBindings - replacing duplicate binding for " << binding.getUri());
        *iter = binding;
      }
      else
      {
        //
        // Simply ignore this binding.  It is older (or equal) than what was previously pushed
        //
        OS_LOG_INFO(FAC_SIP, "RegDB::findAndReplaceOlderBindings - dropping older binding for " << binding.getUri());
      }
      return;
    }
  }
  //
  // We haven't found any older duplicate
  //
  bindings.push_back(binding);
}

bool RegDB::getUnexpiredRegisteredBinding(
      const Url& registeredBinding,
      Bindings& bindings,
      bool preferPrimary)
{
  bool isRegistered = false;
  UtlString hostPort;
  UtlString user;
  registeredBinding.getHostWithPort(hostPort);
  registeredBinding.getUserId(user);

  std::ostringstream binding;
  binding << "sip:";
  if (!user.isNull())
    binding << user.data() << "@";
  binding << hostPort.data();
    
  unsigned long timeNow = OsDateTime::getSecsSinceEpoch();
  
  mongo::BSONObjBuilder query;
  query.append(RegBinding::binding_fld(), binding.str());
  query.append(RegBinding::expirationTime_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));

	if (_local)
  {
		preferPrimary = false;
		_local->getUnexpiredRegisteredBinding(registeredBinding, bindings, preferPrimary);
		query.append(RegBinding::shardId_fld(), BSON_NOT_EQUAL(_local->getShardId()));
	} 

  MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));
  
	mongo::BSONObjBuilder builder;
	if (!preferPrimary)
	  BaseDB::nearest(builder, query.obj());
	else
	  BaseDB::primaryPreferred(builder, query.obj());

	MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
	auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);

  if (!pCursor.get())
  {
   throw mongo::DBException("mongo query returned null cursor", 0);
  }

  if ((isRegistered = pCursor->more()))
  {
    RegBinding binding(pCursor->next());
      
    if (binding.getExpirationTime() > timeNow)
    {
      OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredRegisteredBinding "
      << " Identity: " << binding.getIdentity()
      << " Contact: " << binding.getContact()
      << " Expires: " << binding.getExpirationTime() - OsDateTime::getSecsSinceEpoch() << " sec"
      << " Call-Id: " << binding.getCallId());

      push_or_replace_binding(bindings, binding);
    }
    else
    {
      OS_LOG_WARNING(FAC_SIP, "RegDB::getUnexpiredRegisteredBinding returned an expired record?!?!"
        << " Identity: " << binding.getIdentity()
        << " Contact: " << binding.getContact()
        << " Call-Id: " << binding.getCallId()
        << " Expires: " <<  binding.getExpirationTime() << " epoch"
        << " TimeNow: " << timeNow << " epoch");
    }
  }
  
	conn->done();
  
  OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredRegisteredBinding returning " << (isRegistered ? "TRUE" : "FALSE") << " for binding " <<  binding.str());
   
	return isRegistered;
}

bool RegDB::getUnexpiredContactsUser(const string& identity, unsigned long timeNow, Bindings& bindings, bool preferPrimary) const
{
	static string gruuPrefix = GRUU_PREFIX;

	bool isGruu = identity.substr(0, gruuPrefix.size()) == gruuPrefix;

	mongo::BSONObjBuilder query;
	query.append(RegBinding::expirationTime_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));
 
	if (_local)
  {
		preferPrimary = false;
		_local->getUnexpiredContactsUser(identity, timeNow, bindings, preferPrimary);
		query.append(RegBinding::shardId_fld(), BSON_NOT_EQUAL(_local->getShardId()));
	}
  
   MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));

	if (isGruu) {
		string searchString(identity);
		searchString += ";";
		searchString += SIP_GRUU_URI_PARAM;
		query.append(RegBinding::gruu_fld(), searchString);
	}
	else {
		query.append(RegBinding::identity_fld(), identity);
	}

	mongo::BSONObjBuilder builder;
	if (!preferPrimary)
	  BaseDB::nearest(builder, query.obj());
	else
	  BaseDB::primaryPreferred(builder, query.obj());

	MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
	auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
	if (!pCursor.get())
	{
	  throw mongo::DBException("mongo query returned null cursor", 0);
	}
	else if (pCursor->more())
	{
		while (pCursor->more())
		{
      RegBinding binding(pCursor->next());
      
      if (binding.getExpirationTime() > timeNow)
      {
        OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredContactsUser "
        << " Identity: " << identity
        << " Contact: " << binding.getContact()
        << " Expires: " << binding.getExpirationTime() - OsDateTime::getSecsSinceEpoch() << " sec"
        << " Call-Id: " << binding.getCallId());

        push_or_replace_binding(bindings, binding);
      }
      else
      {
        OS_LOG_WARNING(FAC_SIP, "RegDB::getUnexpiredContactsUser returned an expired record?!?!"
          << " Identity: " << identity
          << " Contact: " << binding.getContact()
          << " Call-Id: " << binding.getCallId()
          << " Expires: " <<  binding.getExpirationTime() << " epoch"
          << " TimeNow: " << timeNow << " epoch");
      }

		}
	}
  else
  {
    OS_LOG_INFO(FAC_SIP, "RegDB::getUnexpiredContactsUser returned empty recordset for identity " << identity);
  }
	conn->done();


	return bindings.size() > 0;
}

bool RegDB::getUnexpiredContactsUserContaining(const string& matchIdentity, unsigned long timeNow, Bindings& bindings, bool preferPrimary) const
{
	mongo::BSONObjBuilder query;
	query.append(RegBinding::expirationTime_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));

	if (_local)
  {
		preferPrimary = false;
		_local->getUnexpiredContactsUserContaining(matchIdentity, timeNow, bindings, preferPrimary);
		query.append(RegBinding::shardId_fld(), BSON_NOT_EQUAL(_local->getShardId()));
	} 

  MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));
   
	mongo::BSONObjBuilder builder;
	if (!preferPrimary)
	  BaseDB::nearest(builder, query.obj());
	else
	  BaseDB::primaryPreferred(builder, query.obj());

	MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
	auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);

  if (!pCursor.get())
  {
   throw mongo::DBException("mongo query returned null cursor", 0);
  }
  else if (pCursor->more())
	{
		while (pCursor->more())
		{
			RegBinding binding(pCursor->next());     
      if (binding.getContact().find(matchIdentity) == string::npos)
      {
        continue;
      }

      push_or_replace_binding(bindings, binding);
		}
		conn->done();
		return bindings.size() > 0;
	}

	conn->done();
	return false;
}


bool RegDB::getUnexpiredContactsUserWithAddress(const string& identity, const std::string& address, unsigned long timeNow, Bindings& bindings, bool preferPrimary) const
{
  Bindings regRecords;
  
  if (!getUnexpiredContactsUser(identity, timeNow, regRecords, preferPrimary))
  {
    return false;
  }
  
  bindings.clear();
  
  for (Bindings::const_iterator iter = regRecords.begin(); iter != regRecords.end(); iter++)
  {
    if (iter->getContact().find(address) != std::string::npos)
    {
      bindings.push_back(*iter);
    }
  }
  
  return !bindings.empty();
}


bool RegDB::getUnexpiredContactsUserInstrument(const string& identity, const string& instrument, unsigned long timeNow,
		Bindings& bindings, bool preferPrimary) const
{
	mongo::BSONObjBuilder query;
	query.append(RegBinding::identity_fld(), identity);
	query.append(RegBinding::instrument_fld(), instrument);
	query.append(RegBinding::expirationTime_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));

	if (_local)
  {
		preferPrimary = false;
		_local->getUnexpiredContactsUserInstrument(identity, instrument, timeNow, bindings, preferPrimary);
		query.append(RegBinding::shardId_fld(), BSON_NOT_EQUAL(_local->getShardId()));
	} 

  MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));
   
	mongo::BSONObjBuilder builder;
	if (!preferPrimary)
	  BaseDB::nearest(builder, query.obj());
	else
	  BaseDB::primaryPreferred(builder, query.obj());

	MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
	auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
  if (!pCursor.get())
  {
   throw mongo::DBException("mongo query returned null cursor", 0);
  }
  else if (pCursor->more())
	{
		while (pCursor->more())
		{
      RegBinding binding(pCursor->next());
			push_or_replace_binding(bindings, binding);
		}
		conn->done();
		return true;
	}

	conn->done();
	return false;
}

// TODO : Unclear how big this dataset would be, decide if this should be removed
bool RegDB::getUnexpiredContactsInstrument(const string& instrument, unsigned long timeNow, Bindings& bindings, bool preferPrimary) const
{
	mongo::BSONObjBuilder query;
  query.append(RegBinding::instrument_fld(), instrument);
  query.append(RegBinding::expirationTime_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));

	if (_local)
  {
  		preferPrimary = false;
		_local->getUnexpiredContactsInstrument(instrument, timeNow, bindings, preferPrimary);
    query.append(RegBinding::shardId_fld(), BSON_NOT_EQUAL(_local->getShardId()));
	} 

  MongoDB::ReadTimer readTimer(const_cast<RegDB&>(*this));
	
  mongo::BSONObjBuilder builder;
	if (!preferPrimary)
	  BaseDB::nearest(builder, query.obj());
	else
	  BaseDB::primaryPreferred(builder, query.obj());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
	auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
  if (!pCursor.get())
  {
   throw mongo::DBException("mongo query returned null cursor", 0);
  }
  else if (pCursor->more())
	{
		while (pCursor->more()) 
    {
			RegBinding binding(pCursor->next());
			push_or_replace_binding(bindings, binding);
		}
		conn->done();
		return true;
	}

	conn->done();
	return false;
}

void RegDB::cleanAndPersist(int currentExpireTime)
{
	if (_local) {
		_local->cleanAndPersist(currentExpireTime);
		return;
	}
  
  MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));

  mongo::BSONObj query = BSON(RegBinding::expirationTime_fld() << BSON_LESS_THAN(BaseDB::dateFromSecsSinceEpoch(currentExpireTime)));
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));

  conn->get()->remove(_ns, query);
	conn->done();
}

void RegDB::clearAllBindings()
{
	if (_local) {
		_local->clearAllBindings();
		return;
	}
  
  MongoDB::UpdateTimer updateTimer(const_cast<RegDB&>(*this));
  
  mongo::BSONObj all;
  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
  conn->get()->remove(_ns, all);
  conn->done();
}

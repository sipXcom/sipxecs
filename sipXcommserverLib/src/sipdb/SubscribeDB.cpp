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

#include <string>
#include <vector>
#include <mongo/client/dbclient.h>
#include <mongo/client/connpool.h>
#include "sipdb/SubscribeDB.h"
#include "sipdb/SubscribeExpireThread.h"
#include "sipdb/MongoMod.h"
#include "os/OsDateTime.h"
#include "os/OsLogger.h"

using namespace std;

const string SubscribeDB::NS("node.subscription");

SubscribeDB* SubscribeDB::CreateInstance(bool ensureIndexes)
{
  SubscribeDB* ldb = NULL;

  MongoDB::ConnectionInfo local = MongoDB::ConnectionInfo::localInfo();
  if (!local.isEmpty())
  {
    ldb = new SubscribeDB(local);

    // ensure the indexes
    if (ensureIndexes)
    {
      ldb->ensureIndexes();
    }
  }

  MongoDB::ConnectionInfo global = MongoDB::ConnectionInfo::globalInfo();
  SubscribeDB* db = new SubscribeDB(global, ldb);

  // ensure the indexes
  if (ensureIndexes)
  {
    db->ensureIndexes();
  }

  return db;
}

void SubscribeDB::getAll(Subscriptions& subscriptions, bool preferPrimary)
{
  mongo::BSONObjBuilder query;
  if (_local) {
    preferPrimary = false;
    _local->getAll(subscriptions, preferPrimary);
    query.append(Subscription::shardId_fld(), BSON_NOT_EQUAL(getShardId()));
  }

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
  MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));
  
  mongo::BSONObjBuilder builder;
	if (preferPrimary)
    BaseDB::primaryPreferred(builder, query.obj());
  else
    BaseDB::nearest(builder, query.obj());


  auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
  if (!pCursor.get())
  {
    throw mongo::DBException("mongo query returned null cursor", 0);
  }

	while (pCursor->more())
  {
    subscriptions.push_back(Subscription(pCursor->next()));
  }
  conn->done();
}

void SubscribeDB::upsert (
    const UtlString& component,
    const UtlString& uri,
    const UtlString& callId,
    const UtlString& contact,
    unsigned int expires,
    unsigned int subscribeCseq,
    const UtlString& eventTypeKey,
    const UtlString& eventType,
    const UtlString& id,
    const UtlString& toUri,
    const UtlString& fromUri,
    const UtlString& key,
    const UtlString& recordRoute,
    unsigned int notifyCseq,
    const UtlString& accept,
    unsigned int version)
{
    if  (_local) { 
      _local->upsert(
            component,
            uri,
            callId,
            contact,
            expires,
            subscribeCseq,
            eventTypeKey,
            eventType,
            id,
            toUri,
            fromUri,
            key,
            recordRoute,
            notifyCseq,
            accept,
            version);
      return;
    }
    
    MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObj query = BSON(
        Subscription::toUri_fld() << toUri.str() <<
        Subscription::fromUri_fld() << fromUri.str() <<
        Subscription::callId_fld() << callId.str() <<
        Subscription::eventTypeKey_fld() << eventTypeKey.str());

    mongo::BSONObjBuilder objBuilder;
    objBuilder.append(Subscription::component_fld(), component.str());
    objBuilder.append(Subscription::uri_fld(), uri.str());
    objBuilder.append(Subscription::callId_fld(), callId.str());
    objBuilder.append(Subscription::contact_fld(), contact.str());
    objBuilder.append(Subscription::expires_fld(), BaseDB::dateFromSecsSinceEpoch(expires));
    objBuilder.append(Subscription::subscribeCseq_fld(), subscribeCseq);
    objBuilder.append(Subscription::eventTypeKey_fld(), eventTypeKey.str());
    objBuilder.append(Subscription::eventType_fld(), eventType.str());
    objBuilder.append(Subscription::id_fld(), id.str());
    objBuilder.append(Subscription::toUri_fld(), toUri.str());
    objBuilder.append(Subscription::fromUri_fld(), fromUri.str());
    objBuilder.append(Subscription::key_fld(), key.str());
    objBuilder.append(Subscription::shardId_fld(), getShardId());

    //do not include RecordRoute if was not provided, because it will delete existing entry
    if (!recordRoute.isNull())
    {
        objBuilder.append(Subscription::recordRoute_fld(), recordRoute.str());
    }

    // do not include the notifysequence in the update if it is zero
    if (notifyCseq)
    {
        objBuilder.append(Subscription::notifyCseq_fld(), notifyCseq);
    }

    objBuilder.append(Subscription::accept_fld(), accept.str());
    objBuilder.append(Subscription::version_fld(),version);

    mongo::BSONObjBuilder opBuilder;
    opBuilder.append("$set", objBuilder.obj());

    mongo::BSONObj update = opBuilder.obj();

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    mongo::DBClientBase* client = conn->get();
    client->update(_ns, query, update, true, false);
    ensureIndexes(client);
    conn->done();
}

void SubscribeDB::ensureIndexes(mongo::DBClientBase* client)
{
  MongoDB::ScopedDbConnectionPtr conn(0);
  mongo::DBClientBase* clientPtr = client;

  OS_LOG_INFO(FAC_SIP, "SubscribeDB::ensureIndexes "
                        << "existing client: " << (client ? "yes" : "no")
                        << ", isFirstEnsureIndexes: " << _isFirstEnsureIndexes);

  // in case no client was provided, create a new connection
  if (!clientPtr)
  {
    conn.reset(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    clientPtr = conn->get();
  }

  clientPtr->ensureIndex(_ns,  BSON(Subscription::key_fld() << 1));
  clientPtr->ensureIndex(_ns,  BSON(Subscription::toUri_fld() << 1));
  clientPtr->ensureIndex(_ns,  BSON(Subscription::shardId_fld() << 1));

  // drop the 'expires' index in case this is the 1st call
  if (_isFirstEnsureIndexes)
  {
    safeDropIndex(clientPtr, Subscription::expires_fld());
    _isFirstEnsureIndexes = false;
  }

  // recreate the 'expires' index by also specifying TTL
  safeEnsureTTLIndex(clientPtr, Subscription::expires_fld(), mongoMod::EXPIRES_AFTER_SECONDS_MINIMUM_SECS);

  // close the connection, if it was created
  if (conn)
  {
    conn->done();
  }
}

//delete methods - delete a subscription session
void SubscribeDB::remove (
   const UtlString& component,
   const UtlString& to,
   const UtlString& from,
   const UtlString& callid,
   const int& subscribeCseq)
{
  if (_local) {
    _local->remove(component, to, from, callid, subscribeCseq);
    return;
  }
  
  MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
  
    mongo::BSONObj query = BSON(
        Subscription::toUri_fld() << to.str() <<
        Subscription::fromUri_fld() << from.str() <<
        Subscription::callId_fld() << callid.str() <<
        Subscription::subscribeCseq_fld() << BSON_LESS_THAN(subscribeCseq));

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    conn->get()->remove(_ns, query);
    conn->done();
}

void SubscribeDB::removeError (
    const UtlString& component,
    const UtlString& to,
    const UtlString& from,
    const UtlString& callid )
{
  if (_local) {
    _local->removeError(component, to, from, callid);
    return;
  }
  
  MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
  
    mongo::BSONObj query = BSON(
        Subscription::toUri_fld() << to.str() <<
        Subscription::fromUri_fld() << from.str() <<
        Subscription::callId_fld() << callid.str());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    conn->get()->remove(_ns, query);
    conn->done();
}

bool SubscribeDB::subscriptionExists (
   const UtlString& component,
   const UtlString& toUri,
   const UtlString& fromUri,
   const UtlString& callId,
   const unsigned long timeNow,
   bool preferPrimary)
{
  mongo::BSONObjBuilder query;
  query.append(Subscription::toUri_fld(), toUri.str());
  query.append(Subscription::fromUri_fld(), fromUri.str());
  query.append(Subscription::callId_fld(), callId.str());
  query.append(Subscription::expires_fld(), BSON_GREATER_THAN_EQUAL(BaseDB::dateFromSecsSinceEpoch(timeNow)));

  if (_local)
  {
    preferPrimary = false;
    if (_local->subscriptionExists(component, toUri, fromUri, callId, timeNow, preferPrimary))
    {
      return true;
    }
    query.append(Subscription::shardId_fld(), getShardId());
  } 

  MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));
  
  mongo::BSONObjBuilder builder;
  if (preferPrimary)
    BaseDB::primaryPreferred(builder, query.obj());
  else
    BaseDB::nearest(builder, query.obj());

  MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));

  mongo::BSONObj subscriptionObj = conn->get()->findOne(_ns, readQueryMaxTimeMS(builder.obj()), 0, mongo::QueryOption_SlaveOk);
  if (!subscriptionObj.isEmpty())
  {
    conn->done();
    return true;
  }

  conn->done();
  return false;
}

//void SubscribeDB::removeRows(const UtlString& key)
//{
//    mongo::BSONObj query = BSON(Subscription::key_fld() << key.str());
//    mongo::ScopedDbConnection conn(_info.getConnectionString());
//    conn->remove(_ns, query);
//}

void SubscribeDB::removeExpired( const UtlString& component, const unsigned long timeNow )
{
    if (_local) {
      _local->removeExpired(component, timeNow);
      return;
    }
    
    MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObj query = BSON(
        Subscription::component_fld() << component.str() <<
        Subscription::expires_fld() << BSON_LESS_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));
    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    conn->get()->remove(_ns, query);
    conn->done();
}

void SubscribeDB::getUnexpiredSubscriptions (
    const UtlString& component,
    const UtlString& key,
    const UtlString& eventTypeKey,
    const unsigned long& timeNow,
    Subscriptions& subscriptions,
    bool preferPrimary)
{
    //query="key=",key,"and eventtypekey=",eventTypeKey;
    if (_local) {
      preferPrimary = false;
      _local->getUnexpiredSubscriptions(component, key, eventTypeKey, timeNow, subscriptions, preferPrimary);
      return;
    }
    
    MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));
    mongo::BSONObjBuilder query;
    query.append(Subscription::key_fld(), key.str());
    query.append(Subscription::eventTypeKey_fld(), eventTypeKey.str());
    query.append(Subscription::shardId_fld(), getShardId());
    query.append(Subscription::expires_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));

    mongo::BSONObjBuilder builder;
    if (preferPrimary)
      BaseDB::primaryPreferred(builder, query.obj());
    else
      BaseDB::nearest(builder, query.obj());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
    auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
    if (!pCursor.get())
    {
     throw mongo::DBException("mongo query returned null cursor", 0);
    }

    while (pCursor->more())
    {
        subscriptions.push_back(Subscription(pCursor->next()));
    }
    conn->done();
}

void SubscribeDB::getUnexpiredContactsFieldsContaining(
    UtlString& substringToMatch,
    const unsigned long& timeNow,
    std::vector<string>& matchingContactFields,
    bool preferPrimary ) const
{
    mongo::BSONObjBuilder query;
    query.append(Subscription::expires_fld(), BSON_GREATER_THAN(BaseDB::dateFromSecsSinceEpoch(timeNow)));
    if (_local)
    {
      preferPrimary = false;
      _local->getUnexpiredContactsFieldsContaining(substringToMatch, timeNow, matchingContactFields, preferPrimary);
      query.append(Subscription::shardId_fld(), BSON_NOT_EQUAL(getShardId()));
    } 

    MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObjBuilder builder;
    if (preferPrimary)
      BaseDB::primaryPreferred(builder, query.obj());
    else
      BaseDB::nearest(builder, query.obj());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
    auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
    if (!pCursor.get())
    {
     throw mongo::DBException("mongo query returned null cursor", 0);
    }

    while (pCursor->more())
    {
        string contact;
        mongo::BSONObj bsonObj = pCursor->next();
        if (bsonObj.hasField(Subscription::contact_fld()))
            contact = bsonObj.getStringField(Subscription::contact_fld());
        if (contact.find(substringToMatch.str()) != string::npos)
            matchingContactFields.push_back(contact);
    }
    conn->done();
}

void SubscribeDB::updateNotifyUnexpiredSubscription(
    const UtlString& component,
    const UtlString& to,
    const UtlString& from,
    const UtlString& callid,
    const UtlString& eventTypeKey,
    const UtlString& id,
    unsigned long timeNow,
    int updatedNotifyCseq,
    int version)
{
    if (_local) {
      _local->updateNotifyUnexpiredSubscription(
            component,
            to,
            from,
            callid,
            eventTypeKey,
            id,
            timeNow,
            updatedNotifyCseq,
            version);
      return;
    }
    
    MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObj query = BSON(
        Subscription::toUri_fld() << to.str() <<
        Subscription::callId_fld() << callid.str() <<
        Subscription::eventTypeKey_fld() << eventTypeKey.str() <<
        Subscription::id_fld() << id.str() );

    mongo::BSONObj update = BSON("$set" << BSON(
        Subscription::notifyCseq_fld() << updatedNotifyCseq <<
        Subscription::version_fld() << version));

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    mongo::DBClientBase* client = conn->get();
    client->update(_ns, query, update);
    ensureIndexes(client);
    conn->done();
}

//void SubscribeDB::updateSubscribeUnexpiredSubscription (
//    const UtlString& component,
//    const UtlString& to,
//    const UtlString& from,
//    const UtlString& callid,
//    const UtlString& eventTypeKey,
//    const UtlString& id,
//    const int& timeNow,
//    const int& expires,
//    const int& updatedSubscribeCseq) const
//{
//    const_cast<SubscribeDB*>(this)->removeExpired(component, timeNow);
//
//    mongo::BSONObj query = BSON(
//        Subscription::toUri_fld() << to.str() <<
//        Subscription::fromUri_fld() << from.str() <<
//        Subscription::callId_fld() << callid.str() <<
//        Subscription::eventTypeKey_fld() << eventTypeKey.str() <<
//        Subscription::id_fld() << id.str() );
//
//    mongo::BSONObj update = BSON("$set" << BSON(
//        Subscription::expires_fld() << expires <<
//        Subscription::subscribeCseq_fld() << updatedSubscribeCseq));
//
//	mongo::ScopedDbConnection conn(_info.getConnectionString());
//    conn->update(_ns, query, update);
//
//
//    if (!ret)
//    {
//       // Add a new row.
//
//       // This call assumes that eventTypeKey (as set by the
//       // handler) is OK for use as the <eventtypekey>,
//       // and that the NOTIFY CSeq's will start at 1.  0 is used as
//       // the initial XML version.
//       ret = mDB.insertRow(
//          mComponent, requestUri, callId, contactEntry,
//          expires, subscribeCseq, eventTypeKey, eventType, "",
//          to, from, resourceId, route, 1, accept, 0);
//
//       if (!ret)
//       {
//          Os::Logger::instance().log(FAC_SIP, PRI_ERR,
//                        "SipPersistantSubscriptionMgr::addSubscription "
//                        "Could not update or insert record in database");
//       }
//    }
//
//}

void SubscribeDB::updateToTag(
   const UtlString& callid,
   const UtlString& fromtag,
   const UtlString& totag)
{
    if (_local) {
      _local->updateToTag(callid, fromtag, totag);
      return;
    }
    
    MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObj query = BSON(Subscription::callId_fld() << callid.str());
    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    mongo::DBClientBase* client = conn->get();
    auto_ptr<mongo::DBClientCursor> pCursor = client->query(_ns, writeQueryMaxTimeMS(query));
    if (!pCursor.get())
    {
     throw mongo::DBException("mongo query returned null cursor", 0);
    }

    while (pCursor->more())
    {
        mongo::BSONObj bsonObj = pCursor->next();
        if (bsonObj.hasField(Subscription::fromUri_fld()))
        {
            string fromUri = bsonObj.getStringField(Subscription::fromUri_fld());
            Url from_uri(fromUri.c_str(), FALSE);
            UtlString seen_tag;
            if (from_uri.getFieldParameter("tag", seen_tag) && seen_tag.compareTo(fromtag) == 0)
            {
                if (bsonObj.hasField(Subscription::toUri_fld()))
                {
                    string toUri = bsonObj.getStringField(Subscription::toUri_fld());
                    Url to_uri(toUri.c_str(), FALSE);
                    UtlString dummy;
                    if (!to_uri.getFieldParameter("tag", dummy))
                    {
                        to_uri.setFieldParameter("tag", totag);
                        to_uri.toString(dummy); // un-parse as name-addr

                        mongo::BSONElement _id_field;
                        if (true == bsonObj.getObjectID(_id_field))
                        {
                            mongo::OID oid;
                            _id_field.Val(oid);

                            mongo::BSONObj query = BSON("_id" << oid);
                            mongo::BSONObj update = BSON("$set" << BSON(Subscription::toUri_fld() << dummy.data()));

                            client->update(_ns, query, update);
                            ensureIndexes(client);
                        }
                        else
                        {
                            // could not retrieve object id so update will fail
                            OS_LOG_INFO(FAC_ODBC, "SubscribeDB::updateToTag get object id failed");
                        }
                    }
                }
            }
        }
    }
    conn->done();
}

bool SubscribeDB::findFromAndTo(
   const UtlString& callid,
   const UtlString& fromtag,
   const UtlString& totag,
   UtlString& from,
   UtlString& to,
   bool preferPrimary) const
{
    mongo::BSONObjBuilder query;
    query.append(Subscription::callId_fld(), callid.str());
    if (_local) {
      preferPrimary = false;
      if (_local->findFromAndTo(callid, fromtag, totag, from, to, preferPrimary)) {
        return true;
      }
      query.append(Subscription::shardId_fld(), BSON_NOT_EQUAL(getShardId()));
    }

    MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObjBuilder builder;
    if (preferPrimary)
      BaseDB::primaryPreferred(builder, query.obj());
    else
      BaseDB::nearest(builder, query.obj());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
    auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
    if (!pCursor.get())
    {
     throw mongo::DBException("mongo query returned null cursor", 0);
    }

    while (pCursor->more())
    {
        Subscription row = pCursor->next();
        UtlBoolean r;
        UtlString seen_tag;

        // Get the tag on the URI in the "from" column.
        Url fromUri(row.fromUri().c_str(), FALSE);
        r = fromUri.getFieldParameter("tag", seen_tag);

        // If it matches...
        if (r && seen_tag.compareTo(fromtag) == 0)
        {
           // Get the tag on the URI in the "to" column.
           Url toUri(row.toUri().c_str(), FALSE);
           r = toUri.getFieldParameter("tag", seen_tag);

           // If it matches...
           if (r && seen_tag.compareTo(totag) == 0)
           {
              // We have found a match.  Record the full URIs.
              from = row.fromUri().c_str();
              to = row.toUri().c_str();
              conn->done();
              return true;
           }
        }
    }
    conn->done();
    return false;
}

int SubscribeDB::getMaxVersion(const UtlString& uri, bool preferPrimary) const
{
    mongo::BSONObjBuilder query;
    query.append(Subscription::uri_fld(), uri.str());
    unsigned int value = 0;
    if (_local) {
      preferPrimary = false;
      value = _local->getMaxVersion(uri, preferPrimary);
      query.append(Subscription::shardId_fld(), BSON_NOT_EQUAL(getShardId()));
    }

    MongoDB::ReadTimer readTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObjBuilder builder;
    if (preferPrimary)
      BaseDB::primaryPreferred(builder, query.obj());
    else
      BaseDB::nearest(builder, query.obj());

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getReadQueryTimeout()));
    auto_ptr<mongo::DBClientCursor> pCursor = conn->get()->query(_ns, readQueryMaxTimeMS(builder.obj()), 0, 0, 0, mongo::QueryOption_SlaveOk);
    if (!pCursor.get())
    {
     throw mongo::DBException("mongo query returned null cursor", 0);
    }

    while (pCursor->more())
    {
        Subscription row = pCursor->next();
        if (value < row.version())
            value = row.version();
    }
    conn->done();
    return value;
}

void SubscribeDB::removeAllExpired()
{
    if (_local) {
      _local->removeAllExpired();
      return;
    }
    unsigned long timeNow = OsDateTime::getSecsSinceEpoch();
    
    OS_LOG_INFO(FAC_SIP, "SubscribeDB::removeAllExpired INVOKED for shard == " << getShardId() << " and expireTime <= " << timeNow);

    MongoDB::UpdateTimer updateTimer(const_cast<SubscribeDB&>(*this));
    
    mongo::BSONObj query = BSON(
      Subscription::shardId_fld() << getShardId() <<
      Subscription::expires_fld() << BSON_LESS_THAN_EQUAL(BaseDB::dateFromSecsSinceEpoch(timeNow)));

    MongoDB::ScopedDbConnectionPtr conn(mongoMod::ScopedDbConnection::getScopedDbConnection(_info.getConnectionString().toString(), getWriteQueryTimeout()));
    conn->get()->remove(_ns, query);
    conn->done();
}


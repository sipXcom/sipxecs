/**
 *
 *
 * Copyright (c) 2015 sipXcom, Inc. All rights reserved.
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
package org.sipfoundry.sipxcallback.common;

import static org.sipfoundry.commons.mongo.MongoConstants.CALLBACK_LIST;
import static org.sipfoundry.commons.mongo.MongoConstants.ENTITY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.sipfoundry.commons.mongo.MongoConstants;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class CallbackUtil {

    private MongoTemplate m_imdbTemplate;

    public DBObject findUserByName(String userName, DBCollection entityCollection) {
        DBObject query = QueryBuilder.start(ENTITY_NAME).is("user").and(UID).is(userName).get();
        return entityCollection.findOne(query);
    }

    /**
     * Retrieves the current date in UTC timezone
     */
    public long getCurrentTimestamp() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        return date.getTime();
    }

    public void updateCallbackList(DBCollection entityCollection,
            BasicDBList callbackList, DBObject user,
            List<DBObject> objectsToBeRemoved) {
        if (objectsToBeRemoved != null && !objectsToBeRemoved.isEmpty()) {
            callbackList.removeAll(objectsToBeRemoved);
        }
        if (callbackList.isEmpty()) {
            user.removeField(MongoConstants.CALLBACK_LIST);
        } else {
            user.put(MongoConstants.CALLBACK_LIST, callbackList);
        }
        entityCollection.save(user);
    }

    public void updateCallbackInformation(String calleeUserName,
            String callerChannelName, boolean insertNewRequest)
            throws CallbackException {
        if (callerChannelName.contains(".")) {
            callerChannelName = callerChannelName.replace(".", ";");
        }
        DBCollection entityCollection = m_imdbTemplate.getCollection("entity");
        DBObject user = findUserByName(calleeUserName, entityCollection);
        if (user == null) {
            // callback user was not found
            throw new CallbackException("Callback user: " + calleeUserName + " not found !");
        }
        BasicDBList callbackList = null;
        if (user.keySet().contains(MongoConstants.CALLBACK_LIST)) {
            callbackList = (BasicDBList) user.get(MongoConstants.CALLBACK_LIST);
        } else {
            callbackList = new BasicDBList();
        }
        for (Object callerObject : callbackList.toArray()) {
            DBObject callerDbObject = (DBObject) callerObject;
            if (((DBObject) callerDbObject).containsField(callerChannelName)) {
                callbackList.remove(callerDbObject);
            }
        }
        if (insertNewRequest) {
            DBObject callback = new BasicDBObject(callerChannelName, getCurrentTimestamp());
            callbackList.add(callback);
        }
        updateCallbackList(entityCollection, callbackList, user, null);
    }

    public DBCursor getCallbackUsers(DBCollection entityCollection) {
        DBObject query = QueryBuilder.start(ENTITY_NAME).is("user").and(CALLBACK_LIST).exists(true).get();
        return entityCollection.find(query);
    }

    public MongoTemplate getImdbTemplate() {
        return m_imdbTemplate;
    }

    @Required
    public void setImdbTemplate(MongoTemplate imdbTemplate) {
        m_imdbTemplate = imdbTemplate;
    }

}

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class CallbackServiceImpl implements CallbackService {

    private MongoTemplate m_imdbTemplate;
    private int m_expires;

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

    public Map<String, String> runCallbackTimer() {
        DBCollection entityCollection = m_imdbTemplate.getCollection("entity");
        // get all users which have callback on busy set
        DBCursor users = getCallbackUsers(entityCollection);

        // iterate over users
        Map<String,String> callsMap = new HashMap<String,String>();
        for (DBObject user : users) {
            BasicDBList callbackList = (BasicDBList) user.get(CALLBACK_LIST);
            String calleeName = ValidUsers.getStringValue(user, UID);
            // iterate over callback requests for each user
            // keep tabs if any callback flag has expired and then update the callback list
            List<DBObject> objectsToBeRemoved = new ArrayList<DBObject>();
            for (Object callerDbObject : callbackList) {
                callsMap.putAll( handleCallbackAction(calleeName, (DBObject) callerDbObject,
                        callbackList, objectsToBeRemoved) );
            }
            updateCallbackList(entityCollection, callbackList, user, objectsToBeRemoved);
        }
        return callsMap;
    }

    private DBObject findUserByName(String userName, DBCollection entityCollection) {
        DBObject query = QueryBuilder.start(ENTITY_NAME).is("user").and(UID).is(userName).get();
        return entityCollection.findOne(query);
    }

    /**
     * Retrieves the current date in UTC timezone
     */
    private long getCurrentTimestamp() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        return date.getTime();
    }

    private void updateCallbackList(DBCollection entityCollection,
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

    /**
     * Returns a list with objects to be removed because their callback duration has expired
     * @return 
     */
    private Map<String, String> handleCallbackAction(String calleeName,
            DBObject callbackObject, BasicDBList callbackList,
            List<DBObject> objectsToBeRemoved) {
        Map<String,String> callsMap = new HashMap<String,String>();
        for (String callerName : callbackObject.keySet()) {
            long callerDate = (long) callbackObject.get(callerName);
            long currentDate = getCurrentTimestamp();
            long timeDiff = currentDate - callerDate;
            // check if the flag for callback has expired
            if (timeDiff < m_expires * 60000) {
                callsMap.put(calleeName, callerName);
            } else {
                objectsToBeRemoved.add(callbackObject);
            }
        }
        return callsMap;
    }

    private DBCursor getCallbackUsers(DBCollection entityCollection) {
        DBObject query = QueryBuilder.start(ENTITY_NAME).is("user").and(CALLBACK_LIST).exists(true).get();
        return entityCollection.find(query);
    }

    @Required
    public void setImdbTemplate(MongoTemplate imdbTemplate) {
        m_imdbTemplate = imdbTemplate;
    }

    @Required
    public void setExpires(int expires) {
        m_expires = expires;
    }

}

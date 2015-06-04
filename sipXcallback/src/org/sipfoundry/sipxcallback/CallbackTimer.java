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
package org.sipfoundry.sipxcallback;

import static org.sipfoundry.commons.mongo.MongoConstants.CALLBACK_LIST;
import static org.sipfoundry.commons.mongo.MongoConstants.ENTITY_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.UID;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.ConfBasicThread;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocket;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.sipfoundry.sipxcallback.common.CallbackUtil;
import org.sipfoundry.sipxcallback.common.FreeSwitchConfigurationImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

/**
 *  Daemon task class that handles callback requests registered in the system
 */
public class CallbackTimer implements BeanFactoryAware{
    private static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxcallback");

    private int m_expires;
    private MongoTemplate m_imdbTemplate;

    private FreeSwitchConfigurationImpl fsConfig;
    private FreeSwitchEventSocket m_fsCmdSocket;
    private ThreadPoolTaskExecutor m_taskExecutor;
    private BeanFactory m_beanFactory;

    public void run() throws ParseException {
        if (m_fsCmdSocket == null) {
            try {
                if (m_fsCmdSocket == null) {
                    fsConfig = new FreeSwitchConfigurationImpl();
                }
                m_fsCmdSocket = new FreeSwitchEventSocket(fsConfig);
                m_fsCmdSocket.connect(getSocket(), ConfBasicThread.fsPassword);
            } catch (IOException e) {
                LOG.error(e);
                return;
            }
        }

        DBCollection entityCollection = m_imdbTemplate.getCollection("entity");
        // get all users which have callback on busy set
        DBCursor users = findCallbackUsers(entityCollection);

        // iterate over users
        for (DBObject user : users) {
            BasicDBList callbackList = (BasicDBList) user.get(CALLBACK_LIST);
            String calleeName = ValidUsers.getStringValue(user, UID);
            // iterate over callback requests for each user
            // keep tabs if any callback flag has expired and the update the callback list
            List<DBObject> objectsToBeRemoved = new ArrayList<DBObject>();
            for (Object callerDbObject : callbackList) {
                List<DBObject> objectsToBeRemovedToken = handleCallbackAction(
                        calleeName, (DBObject) callerDbObject, callbackList);
                objectsToBeRemoved.addAll(objectsToBeRemovedToken);
            }
            CallbackUtil.updateCallbackList(entityCollection, callbackList, user, objectsToBeRemoved);
        }
    }

    /**
     * Returns a list with objects to be removed because their callback duration has expired
     */
    private List<DBObject> handleCallbackAction(String calleeName, DBObject callbackObject, BasicDBList callbackList) {
        List<DBObject> objectsToBeRemoved = new ArrayList<DBObject>();
        for (String callerName : callbackObject.keySet()) {
            long callerDate = (long) callbackObject.get(callerName);
            long currentDate = CallbackUtil.getCurrentTimestamp();
            long timeDiff = currentDate - callerDate;
            // check if the flag for callback has expired
            if (timeDiff < m_expires * 60000) {
                executeCallbackThread(calleeName, callerName);
            } else {
                objectsToBeRemoved.add(callbackObject);
            }
        }
        return objectsToBeRemoved;
    }

    public void executeCallbackThread(String callerName, String calleeName) {
        try {
            if (m_taskExecutor.getThreadPoolExecutor().getQueue().isEmpty()) {
                CallbackThread callbackThread = (CallbackThread) m_beanFactory
                        .getBean(CallbackThread.BEAN_NAME);
                callbackThread.initiate(callerName, calleeName, m_fsCmdSocket);
                m_taskExecutor.execute(callbackThread);
            } else {
                LOG.debug("Wait for queue to become empty and look "+
                 "for more users that need to receive callbacks") ;
            }
        } catch (Exception ex) {
            LOG.error("Error during callback execution: ", ex);
        }
    }

    private DBCursor findCallbackUsers(DBCollection entityCollection) {
        DBObject query = QueryBuilder.start(ENTITY_NAME).is("user").and(CALLBACK_LIST).exists(true).get();
        return entityCollection.find(query);
    }

    public void setExpires(int expires) {
        m_expires = expires;
    }

    private Socket getSocket() {
        Socket socket = null;
        while(socket == null) {
            // freeswitch may be slow to start especially on a different machine
            try {
                socket = new Socket("localhost", Integer.parseInt(ConfBasicThread.fsListenPort));
            } catch (UnknownHostException e) {
                LOG.error("Can't create connection to freeswitch " + e.getMessage());
            } catch (IOException e) {
                // freeswitch likely is not up yet
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e1) {
                    LOG.error(e1);
                }
            }
        }
        return socket;
    }

    @Required
    public void setImdbTemplate(MongoTemplate imdbTemplate) {
        m_imdbTemplate = imdbTemplate;
    }

    @Required
    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        m_taskExecutor = taskExecutor;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }

}

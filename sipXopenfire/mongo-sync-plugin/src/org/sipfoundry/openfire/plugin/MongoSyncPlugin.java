/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.plugin;

import java.io.File;

import org.apache.log4j.Logger;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.sipfoundry.openfire.plugin.job.JobFactory;
import org.sipfoundry.openfire.plugin.listener.ImdbOplogListener;

public class MongoSyncPlugin implements Plugin {
    private static Logger logger = Logger.getLogger(MongoSyncPlugin.class);

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        logger.info("Mongo sync plugin initializing");
        ThreadGroup group = new ThreadGroup("mongoSync");
        Thread imdbOpLogThread = new Thread(group, new ImdbOplogListener(new JobFactory()), "imdbOpLogListener");
        // Disable avatar operations due to problems, see UC-2765
        // Thread profilesdbOpLogThread = new Thread(group, new ProfilesOplogListener(),
        // "profilesOpLogListener");
        imdbOpLogThread.start();
        // profilesdbOpLogThread.start();
    }

    @Override
    public void destroyPlugin() {
        logger.info("Mongo sync plugin stopping");
    }
}

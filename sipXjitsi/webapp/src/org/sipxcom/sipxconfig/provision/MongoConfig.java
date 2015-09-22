/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.provision;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {

    private static final Log LOG = LogFactory.getLog(MongoConfig.class);

    public static final String DATABASE_NAME = "imdb";
    public static final String CONFIG_GILE = "/mongo-client.ini";
    public static final String CONNECTION_URL_KEY = "connectionUrl";

    @Override
    protected String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    @Bean
    public Mongo mongo() throws Exception {
        String mongoClientIni = this.getClass().getResource(CONFIG_GILE).getFile();
        LOG.info("Mongo configuration file: " + mongoClientIni);
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(mongoClientIni);
        prop.load(input);
        String connString = prop.getProperty(CONNECTION_URL_KEY);
        LOG.debug("Mongo connection string: " + connString);
        return new MongoClient(new MongoClientURI(connString));
    }
}

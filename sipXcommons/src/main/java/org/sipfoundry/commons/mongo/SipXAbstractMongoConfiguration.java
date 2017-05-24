/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.commons.mongo;

import java.io.FileInputStream;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class SipXAbstractMongoConfiguration extends AbstractMongoConfiguration {

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
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(mongoClientIni);
        prop.load(input);
        String connString = prop.getProperty(CONNECTION_URL_KEY);
        return new MongoClient(new MongoClientURI(connString));
    }
}

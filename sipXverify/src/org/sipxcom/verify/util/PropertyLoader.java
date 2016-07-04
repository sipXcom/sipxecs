package org.sipxcom.verify.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by HP on 2/4/2016.
 */
public class PropertyLoader {
    private static final Log LOG = LogFactory.getLog(PropertyLoader.class);
    private static Properties m_properties = new Properties();

    static {
        try {
            InputStream propertiesFile = PropertyLoader.class.getResourceAsStream("/conf.properties");
            m_properties.load(propertiesFile);
        } catch (Exception e) {
            LOG.error("Error reading conf.properties:", e);
            System.exit(1);
        }
    }

    public static String getSiteURL() {
        return m_properties.getProperty("site.url");
    }

    public static String getProperty(String propertyKey){
        return m_properties.getProperty(propertyKey);
    }

    public static String getUserName(String userName) {
        return m_properties.getProperty(userName + ".name");
    }

    public static String getUserPassword(String userName) {
        return m_properties.getProperty(userName + ".password");
    }

}

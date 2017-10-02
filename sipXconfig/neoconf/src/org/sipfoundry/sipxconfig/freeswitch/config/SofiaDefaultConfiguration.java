package org.sipfoundry.sipxconfig.freeswitch.config;

import org.apache.velocity.VelocityContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchSettings;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by mirceac on 9/28/17.
 */
public class SofiaDefaultConfiguration extends AbstractFreeswitchConfiguration {

    @Override
    protected String getTemplate() {
        return "freeswitch/sofia_default.conf.xml.vm";
    }

    @Override
    protected String getFileName() {
        return "autoload_configs/sofia.conf.xml";
    }

    @Override
    public void write(Writer writer, Location location, FreeswitchSettings settings) throws IOException {
        File f = new File(getFileName());
        if (!f.exists()) {
            VelocityContext context = new VelocityContext();
            write(writer, context);
        }
    }

    @Override
    public String getBeanId() {
        return "SofiaDefaultConfiguration";
    }
}

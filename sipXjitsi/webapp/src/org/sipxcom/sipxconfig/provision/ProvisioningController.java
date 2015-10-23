/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.provision;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.security.UserDetailsImpl;
import org.sipxcom.sipxconfig.pojo.Entity;
import org.sipxcom.sipxconfig.provision.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProvisioningController {
    private static final Log LOG = LogFactory.getLog(ProvisioningController.class);

    private static final String JITSI_CONF = "/jitsi/jitsiconf.properties";

    @Autowired
    private UserRepository repository;

    @RequestMapping(value = "/provisioning", method = RequestMethod.POST)
    @ResponseBody
    public String provision(ModelMap model, Authentication auth) {
        String jitsiConfFilePath = this.getClass().getResource(JITSI_CONF).getFile();
        Properties props = new Properties();
        try {
            FileInputStream input = new FileInputStream(jitsiConfFilePath);
            props.load(input);
        } catch (Exception e) {
            LOG.error("Cannot load properties file: " + jitsiConfFilePath, e);
            return StringUtils.EMPTY;
        }
        String provisionPath = props.getProperty("provision.dir");

        UserDetailsImpl user = (UserDetailsImpl)auth.getPrincipal();
        String uid = user.getUsername();
        LOG.info("Authenticated username " + uid);

        StringBuilder builder = new StringBuilder();
        //make sure to recreate line settings (avoid duplicates when multiple jitsi logins)
        builder.append("net.java.sip.communicator.impl.protocol.sip=\\${null}\n");
        builder.append("net.java.sip.communicator.impl.protocol.jabber=\\${null}\n");

        Entity entity = repository.findOneByEntAndModelAndPhLinesContaining("phone", "Jitsi", uid);
        if (entity == null) {
            LOG.error("No jitsi phone created for user: " + uid);
            return builder.toString();
        }

        String mac = entity.getMac();

        LOG.info("Provision file path: " + provisionPath);
        LOG.info("Jitsi phone mac: " + mac);

        String provisionFilePath = provisionPath + mac + ".properties";

        String content = StringUtils.EMPTY;
        try {
            content = FileUtils.readFileToString(new File(provisionFilePath));
        } catch (Exception e) {
            LOG.error("Cannot load provisioned content " + provisionFilePath, e);
            return builder.toString();
        }

        builder.append("provisioning.ALLOW_PREFIX=net.java|org.jitsi|service.gui|org.ice4j|plugin|impl.gui|systray|java.net\n");
        builder.append(content);
        builder.append('\n');

        builder.append("provisioning.ENFORCE_PREFIX=net.java|org.jitsi|service.gui|org.ice4j|plugin|impl.gui|systray|java.net\n");

        LOG.info("Provisioned content returned.");

        return builder.toString();
    }
}

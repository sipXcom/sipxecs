/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.provision;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.mongo.Entity;
import org.sipfoundry.sipxconfig.security.UserDetailsImpl;
import org.sipxcom.zoiper.provision.dao.ZoiperEntityRepository;
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

    private static final String ZOIPER_CONF = "/zoiper/zoiperconf.properties";

    @Autowired
    private ZoiperEntityRepository repository;

    @RequestMapping(value = "/provisioning", method = RequestMethod.GET)
    @ResponseBody
    public String provision(ModelMap model, Authentication auth) {
        String zoiperConfFilePath = this.getClass().getResource(ZOIPER_CONF).getFile();
        Properties props = new Properties();
        try {
            FileInputStream input = new FileInputStream(zoiperConfFilePath);
            props.load(input);
        } catch (Exception e) {
            LOG.error("Cannot load properties file: " + zoiperConfFilePath, e);
            return StringUtils.EMPTY;
        }
        String provisionPath = props.getProperty("provision.dir");

        UserDetailsImpl user = (UserDetailsImpl)auth.getPrincipal();
        String uid = user.getUsername();
        LOG.info("Authenticated username " + uid);

        StringBuilder builder = new StringBuilder();

        Entity entity = repository.findOneByEntAndModelAndPhLinesContaining("phone", "Zoiper", uid);
        if (entity == null) {
            LOG.error("No zoiper phone created for user: " + uid);
            return builder.toString();
        }

        String mac = entity.getMac();

        LOG.info("Provision file path: " + provisionPath);
        LOG.info("Zoiper phone mac: " + mac);

        String provisionFilePath = provisionPath + mac + ".xml";

        String content = StringUtils.EMPTY;
        try {
            content = FileUtils.readFileToString(new File(provisionFilePath));
        } catch (Exception e) {
            LOG.error("Cannot load provisioned content " + provisionFilePath, e);
            return builder.toString();
        }

        builder.append(content);

        LOG.info("Provisioned content returned.");

        return builder.toString();
    }
}

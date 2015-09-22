/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.provision;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipxcom.sipxconfig.pojo.Entity;
import org.sipxcom.sipxconfig.pojo.MongoUserDetails;
import org.sipxcom.sipxconfig.provision.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class MongoUserDetailsService implements UserDetailsService {
    private static final Log LOG = LogFactory.getLog(MongoUserDetailsService.class);

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Entity user = repository.findByUid(userName);
        if (user == null) {
            return null;
        }
        LOG.info("User found for uid: " + userName);
        MongoUserDetails mongoUser = new MongoUserDetails();
        try {
            BeanUtils.copyProperties(mongoUser, user);
        } catch (Exception e) {
            return null;
        }
        return mongoUser;
    }

}

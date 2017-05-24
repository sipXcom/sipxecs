/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.provision;

import org.sipfoundry.commons.security.SipXWebSecurityConfigurerAdapter;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;


@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends SipXWebSecurityConfigurerAdapter {
    
}

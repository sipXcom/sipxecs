/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.commons.security;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SipXWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private ServletContext context;

    private static String SIPXCONFIG_PATH = "/sipxconfig";

    private static String AUTHENTICATION_MANAGER = "authenticationManager";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable()
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        ServletContext sipxconfigCtx = context.getContext(SIPXCONFIG_PATH);
        ApplicationContext webContext = WebApplicationContextUtils
                .getRequiredWebApplicationContext(sipxconfigCtx);
        ProviderManager authManager = (ProviderManager) webContext.getBean(AUTHENTICATION_MANAGER);
        auth.parentAuthenticationManager(authManager);
    }
}

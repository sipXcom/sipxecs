/*
 * Copyright (C) 2011 Your Company Here,
 * Licensed to the User under the AGPL license.
 * $
 *
 */
package org.sipfoundry.sipxconfig.web.plugin;

import org.apache.tapestry.annotations.InjectObject;
import org.sipfoundry.Example;
import org.sipfoundry.sipxconfig.components.SipxBasePage;

/**
 * Tapestry 4 page to do whatever you want.
 */
public abstract class PluginTestPage extends SipxBasePage {

    /**
     * inject your service into the page to perform operations, read/write data, etc. Tapestry integration
     * performs the magic of setting the instance on this page, you just have to get the bean name correct.
     */
    @InjectObject("spring:example")
    public abstract Example getExample();

    public String getTestNoValue() {
        Example e = getExample();
        return getMessages().format("msg.plugintest.hello",  e.hello(), e.getNumberOfUsers());
    }
}

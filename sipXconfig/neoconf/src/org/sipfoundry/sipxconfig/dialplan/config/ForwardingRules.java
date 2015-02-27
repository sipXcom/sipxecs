/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.dialplan.config;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.dialplan.IDialingRule;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.registrar.Registrar;
import org.sipfoundry.sipxconfig.sbc.DefaultSbc;
import org.sipfoundry.sipxconfig.sbc.SbcDevice;
import org.sipfoundry.sipxconfig.sbc.SbcDeviceManager;
import org.sipfoundry.sipxconfig.sbc.SbcManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Controls very initial SIP message routing from proxy based on SIP method and potentialy message
 * content.
 */
public class ForwardingRules extends RulesFile implements ApplicationContextAware {
    private SbcManager m_sbcManager;
    private List<String> m_routes;
    private SbcDeviceManager m_sbcDeviceManager;
    private AddressManager m_addressManager;
    private VelocityEngine m_velocityEngine;
    private ApplicationContext m_context;
    private FeatureManager m_featureManager;

    public void setSbcManager(SbcManager sbcManager) {
        m_sbcManager = sbcManager;
    }

    public void setSbcDeviceManager(SbcDeviceManager sbcDeviceManager) {
        m_sbcDeviceManager = sbcDeviceManager;
    }

    public SbcDeviceManager getSbcDeviceManager() {
        return m_sbcDeviceManager;
    }

    public void begin() {
        m_routes = new ArrayList<String>();
    }

    public void generate(IDialingRule rule) {
        m_routes.addAll(Arrays.asList(rule.getHostPatterns()));
    }

    public void end() {
    }

    @Override
    public void write(Writer writer) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("routes", m_routes);

        DefaultSbc sbc = m_sbcManager.loadDefaultSbc();
        context.put("sbc", sbc);

        if (sbc != null) {
            context.put("exportLocalIpAddress", !sbc.getRoutes().isEmpty());
        }

        context.put("auxSbcs", m_sbcManager.loadAuxSbcs());
        context.put("dollar", "$");

        // set required sipx services in context
        context.put("domainName", getDomainName());
        context.put("proxyAddress", m_addressManager.getSingleAddress(ProxyManager.TCP_ADDRESS, getLocation()));
        context.put("statusAddress", m_addressManager.getSingleAddress(Mwi.SIP_TCP, getLocation()));
        context.put("regEventAddress", m_addressManager.getSingleAddress(Registrar.EVENT_ADDRESS, getLocation()));
        context.put("regAddress", m_addressManager.getSingleAddress(Registrar.TCP_ADDRESS, getLocation()));
        context.put("location", getLocation());

        List<BridgeSbc> bridgeSbcs = new ArrayList<BridgeSbc>();
        List<SbcDevice> sbcDevices = m_sbcDeviceManager.getSbcDevices();
        for (SbcDevice device : sbcDevices) {
            if (device.getModel().isInternalSbc()) {
                bridgeSbcs.add((BridgeSbc) device);
            }
        }
        context.put("bridgeSbcs", bridgeSbcs);

        Map<String, ForwardingRulesPlugin> beans = m_context.getBeansOfType(ForwardingRulesPlugin.class);
        List<ForwardingRulesPlugin> subscribePlugins = new ArrayList<ForwardingRulesPlugin>();
        List<ForwardingRulesPlugin> notifyPlugins = new ArrayList<ForwardingRulesPlugin>();
        if (beans != null) {
            for (ForwardingRulesPlugin bean : beans.values()) {
                String featureId = bean.getFeatureId();
                if (featureId == null  || (m_featureManager.isFeatureEnabled(new LocationFeature(featureId))
                        || m_featureManager.isFeatureEnabled(new GlobalFeature(featureId)))) {
                    if (bean.getMethodPattern().equals(ForwardingRulesPlugin.NOTIFY)) {
                        notifyPlugins.add(bean);
                    }
                    if (bean.getMethodPattern().equals(ForwardingRulesPlugin.SUBSCRIBE)) {
                        subscribePlugins.add(bean);
                    }
                }
            }
        }

        if (!subscribePlugins.isEmpty()) {
            context.put("subscribeFieldMatches", subscribePlugins);
        }

        if (!notifyPlugins.isEmpty()) {
            context.put("addNotify", true);
            context.put("notifyFieldMatches", notifyPlugins);
        }

        try {
            m_velocityEngine.mergeTemplate("commserver/forwardingrules.vm", context, writer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        m_context = context;
    }

    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }
}

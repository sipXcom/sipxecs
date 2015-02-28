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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.bridge.BridgeSbc;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.dialplan.IDialingRule;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.gateway.GatewayContext;
import org.sipfoundry.sipxconfig.gateway.SipTrunk;
import org.sipfoundry.sipxconfig.mwi.Mwi;
import org.sipfoundry.sipxconfig.phone.PhoneTestDriver;
import org.sipfoundry.sipxconfig.proxy.ProxyManager;
import org.sipfoundry.sipxconfig.registrar.Registrar;
import org.sipfoundry.sipxconfig.sbc.AuxSbc;
import org.sipfoundry.sipxconfig.sbc.DefaultSbc;
import org.sipfoundry.sipxconfig.sbc.Sbc;
import org.sipfoundry.sipxconfig.sbc.SbcDescriptor;
import org.sipfoundry.sipxconfig.sbc.SbcDevice;
import org.sipfoundry.sipxconfig.sbc.SbcDeviceManager;
import org.sipfoundry.sipxconfig.sbc.SbcManager;
import org.sipfoundry.sipxconfig.sbc.SbcRoutes;
import org.sipfoundry.sipxconfig.setting.ModelFilesContext;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.sipfoundry.sipxconfig.test.XmlUnitHelper;
import org.springframework.context.ApplicationContext;

public class ForwardingRulesTest extends XMLTestCase {
    private Location m_statusLocation;
    private SbcDeviceManager m_sbcDeviceManager;
    private ApplicationContext m_applicationContext;
    private AddressManager m_addressManager;
    private Location m_location;

    @Override
    protected void setUp() throws Exception {
        TestHelper.initDefaultDomain();
        XmlUnitHelper.setNamespaceAware(false);
        XMLUnit.setIgnoreWhitespace(true);

        DomainManager domainManager = TestHelper.getMockDomainManager();
        replay(domainManager);

        m_location = TestHelper.createDefaultLocation();

        m_addressManager = createMock(AddressManager.class);
        m_addressManager.getSingleAddress(ProxyManager.TCP_ADDRESS, m_location);
        expectLastCall().andReturn(new Address(ProxyManager.TCP_ADDRESS, "proxy.example.org", 9901)).once();
        m_addressManager.getSingleAddress(Mwi.SIP_TCP, m_location);
        expectLastCall().andReturn(new Address(Mwi.SIP_TCP, "mwi.example.org", 9902)).once();
        m_addressManager.getSingleAddress(Registrar.EVENT_ADDRESS, m_location);
        expectLastCall().andReturn(new Address(Registrar.EVENT_ADDRESS, "regevent.example.org", 9903)).once();
        m_addressManager.getSingleAddress(Registrar.TCP_ADDRESS, m_location);
        expectLastCall().andReturn(new Address(Registrar.TCP_ADDRESS, "reg.example.org", 9904)).once();
        replay(m_addressManager);

        List<Location> locations = new ArrayList<Location>();
        m_statusLocation = new Location();
        m_statusLocation.setAddress("192.168.1.5");
        locations.add(m_statusLocation);
        m_sbcDeviceManager = createMock(SbcDeviceManager.class);

        GatewayContext gatewayContext = createMock(GatewayContext.class);
        gatewayContext.getGatewayByType(SipTrunk.class);
        expectLastCall().andReturn(null);
        replay(gatewayContext);

        m_sbcDeviceManager.getBridgeSbc(m_statusLocation);
        expectLastCall().andReturn(null).anyTimes();

        m_sbcDeviceManager.getSbcDevices();
        expectLastCall().andReturn(Collections.emptyList());
        replay(m_sbcDeviceManager);

        m_applicationContext = createMock(ApplicationContext.class);
        m_applicationContext.getBeansOfType(ForwardingRulesPlugin.class);
        expectLastCall().andReturn(null);
        replay(m_applicationContext);

    }

    public void testGenerate() throws Exception {

        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3"),
                Arrays.asList("*.example.org", "*.example.net"), Arrays.asList("10.1.2.3/16"));
        sbc.setAddress("10.1.2.3");
        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setLocation(m_location);
        rules.setAddressManager(m_addressManager);
        rules.setApplicationContext(m_applicationContext);
        String actual = toString(rules);
        InputStream referenceXmlStream = ForwardingRulesTest.class.getResourceAsStream("forwardingrules.test.xml");
        assertXMLEqual(new InputStreamReader(referenceXmlStream), new StringReader(actual));
        assertXpathEvaluatesTo("gander", "/routes/route/routeFrom[10]", actual);

        verify(rule, sbcManager);
    }

    String toString(ForwardingRules rules) throws IOException {
        rules.setLocation(TestHelper.createDefaultLocation());
        StringWriter w = new StringWriter();
        rules.write(w);
        return w.toString();
    }

    public void testGenerateWithPlugins() throws Exception {
        ApplicationContext applicationContext = createMock(ApplicationContext.class);
        applicationContext.getBeansOfType(ForwardingRulesPlugin.class);
        expectLastCall().andReturn(getPlugins());
        replay(applicationContext);

        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3"),
                Arrays.asList("*.example.org", "*.example.net"), Arrays.asList("10.1.2.3/16"));
        sbc.setAddress("10.1.2.3");
        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setLocation(m_location);
        rules.setAddressManager(m_addressManager);
        rules.setApplicationContext(applicationContext);
        rules.setFeatureManager(new MockFeatureManager());
        String actual = toString(rules);
        String expected = IOUtils.toString(getClass().getResourceAsStream("forwardingrules-plugins.test.xml"));
        assertEquals(expected, actual.toString());

        verify(rule, sbcManager);
    }

    public Map<String, ForwardingRulesPlugin> getPlugins() {
        Map<String, ForwardingRulesPlugin> plugins = new LinkedHashMap<String, ForwardingRulesPlugin>();
        ForwardingRulesPlugin plugin1 = new MockForwardingPlugin(ForwardingRulesPlugin.SUBSCRIBE, "To",
                ".*sip:saa@ezuce.ro.*", "regevent.example.org;transport=tcp", "enabled", null);
        plugins.put("plugin1", plugin1);
        ForwardingRulesPlugin plugin2 = new MockForwardingPlugin(ForwardingRulesPlugin.SUBSCRIBE, "From",
                ".*sip:saa@ezuce.ro.*", "regevent.example.org;transport=tcp", "enabled", "sipx-noroute=Voicemail");
        plugins.put("plugin2", plugin2);
        ForwardingRulesPlugin plugin3 = new MockForwardingPlugin(ForwardingRulesPlugin.NOTIFY, "Contact",
                "dialog;sla", "regevent.example.org;transport=tcp", "enabled", "sipx-noroute=Voicemail");
        plugins.put("plugin3", plugin3);
        ForwardingRulesPlugin plugin4 = new MockForwardingPlugin(ForwardingRulesPlugin.NOTIFY, "Contact",
                "dialog;sla", "regevent.example.org;transport=tcp", "disabled", "sipx-noroute=Voicemail");
        plugins.put("plugin4", plugin4);
        return plugins;
    }

    public void testGenerateWithEmptyDomainsAndIntranets() throws Exception {

        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3"), new ArrayList<String>(),
                new ArrayList<String>());
        sbc.setAddress("10.1.2.3");
        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setAddressManager(m_addressManager);
        rules.setApplicationContext(m_applicationContext);
        String generatedXml = toString(rules);
        InputStream referenceXmlStream = ForwardingRulesTest.class
                .getResourceAsStream("forwardingrules-no-local-ip.test.xml");

        assertXMLEqual(new InputStreamReader(referenceXmlStream), new StringReader(generatedXml));

        assertXpathEvaluatesTo("gander", "/routes/route/routeFrom[10]", generatedXml);

        verify(rule, sbcManager);
    }

    // TODO : Fix then when adding DNS server back in
    public void DISABLED_testGenerateMultipleRegistrars() throws Exception {

        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        m_statusLocation = new Location();
        m_statusLocation.setAddress("192.168.1.5");

        Location location2 = new Location();

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3"), new ArrayList<String>(),
                new ArrayList<String>());
        sbc.setAddress("10.1.2.3");
        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setAddressManager(m_addressManager);
        String generatedXml = toString(rules);
        InputStream referenceXmlStream = ForwardingRulesTest.class
                .getResourceAsStream("forwardingrules-ha.test.xml");

        // assertEquals(IOUtils.toString(referenceXmlStream), generatedXml);
        assertEquals(generatedXml, IOUtils.toString(referenceXmlStream));

        assertXpathEvaluatesTo("gander", "/routes/route/routeFrom[10]", generatedXml);

        verify(rule, sbcManager);
    }

    public void testGenerateAuxSbcs() throws Exception {

        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3", 5070),
                Arrays.asList("*.example.org", "*.example.net"), Arrays.asList("10.1.2.3/16"));
        Sbc aux1 = configureSbc(new AuxSbc(), configureSbcDevice("10.1.2.4"),
                Arrays.asList("*.sipfoundry.org", "*.sipfoundry.net"), new ArrayList<String>());
        Sbc aux2 = configureSbc(new AuxSbc(), configureSbcDevice("sbc.example.org"),
                Arrays.asList("*.xxx", "*.example.tm"), Arrays.asList("10.4.4.1/24"));
        sbc.setAddress("10.1.2.3");
        sbc.setPort(5070);
        aux1.setAddress("10.1.2.4");
        aux2.setAddress("sbc.example.org");
        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);
        sbcManager.loadAuxSbcs();
        expectLastCall().andReturn(Arrays.asList(aux1, aux2));

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setAddressManager(m_addressManager);
        rules.setApplicationContext(m_applicationContext);
        String generatedXml = toString(rules);
        InputStream referenceXmlStream = ForwardingRulesTest.class
                .getResourceAsStream("forwardingrules-aux.test.xml");

        assertXMLEqual(new InputStreamReader(referenceXmlStream), new StringReader(generatedXml));
        verify(rule, sbcManager);
    }

    public void testGenerateAuxSbcsDisabled() throws Exception {

        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3"),
                Arrays.asList("*.example.org", "*.example.net"), Arrays.asList("10.1.2.3/16"));
        sbc.setAddress("10.1.2.3");
        Sbc aux1 = configureSbc(new AuxSbc(), configureSbcDevice("10.1.2.4"),
                Arrays.asList("*.sipfoundry.org", "*.sipfoundry.net"), new ArrayList<String>());
        aux1.setAddress("10.1.2.4");
        aux1.setEnabled(false);
        Sbc aux2 = configureSbc(new AuxSbc(), configureSbcDevice("sbc.example.org"),
                Arrays.asList("*.xxx", "*.example.tm"), Arrays.asList("10.4.4.1/24"));
        aux2.setEnabled(false);
        aux2.setAddress("sbc.example.org");

        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);
        sbcManager.loadAuxSbcs();
        expectLastCall().andReturn(Arrays.asList(aux1, aux2));

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setAddressManager(m_addressManager);
        rules.setApplicationContext(m_applicationContext);
        String generatedXml = toString(rules);
        InputStream referenceXmlStream = ForwardingRulesTest.class.getResourceAsStream("forwardingrules.test.xml");

        assertXMLEqual(new InputStreamReader(referenceXmlStream), new StringReader(generatedXml));
        verify(rule, sbcManager);
    }

    public void testGenerateWithItspNameCallback() throws Exception {
        IDialingRule rule = createNiceMock(IDialingRule.class);
        rule.getHostPatterns();
        expectLastCall().andReturn(new String[] {
            "gander"
        });

        ModelFilesContext modelFilesContext = TestHelper.getModelFilesContext();
        DeviceDefaults deviceDefaults = PhoneTestDriver.getDeviceDefaults();

        SbcDescriptor sbcDescriptor = new SbcDescriptor();
        sbcDescriptor.setInternalSbc(true);

        BridgeSbc bridgeSbc = new BridgeSbc();
        bridgeSbc.setModel(sbcDescriptor);
        bridgeSbc.setDefaults(deviceDefaults);
        bridgeSbc.setAddress("192.168.5.240");
        bridgeSbc.setPort(5090);

        SipTrunk sipTrunk = new SipTrunk();
        sipTrunk.setDefaults(deviceDefaults);
        sipTrunk.setModelFilesContext(modelFilesContext);
        sipTrunk.setSbcDevice(bridgeSbc);
        sipTrunk.setAddress("itsp.example.com");
        sipTrunk.setSettingValue("itsp-account/itsp-proxy-domain", "default.itsp.proxy.domain");

        GatewayContext gatewayContext = createMock(GatewayContext.class);
        gatewayContext.getGatewayByType(SipTrunk.class);
        expectLastCall().andReturn(Arrays.asList(sipTrunk));
        replay(gatewayContext);
        bridgeSbc.setGatewayContext(gatewayContext);

        m_sbcDeviceManager = createNiceMock(SbcDeviceManager.class);
        m_sbcDeviceManager.getSbcDevices();
        expectLastCall().andReturn(Collections.singletonList(bridgeSbc));
        replay(m_sbcDeviceManager);

        Sbc sbc = configureSbc(new DefaultSbc(), configureSbcDevice("10.1.2.3"),
                Arrays.asList("*.example.org", "*.example.net"), Arrays.asList("10.1.2.3/16"));
        sbc.setAddress("10.1.2.3");

        SbcManager sbcManager = createNiceMock(SbcManager.class);
        sbcManager.loadDefaultSbc();
        expectLastCall().andReturn(sbc);
        sbcManager.loadAuxSbcs();
        expectLastCall().andReturn(null);

        replay(rule, sbcManager);

        ForwardingRules rules = generate(rule, sbcManager);
        rules.setAddressManager(m_addressManager);
        rules.setApplicationContext(m_applicationContext);
        String generatedXml = toString(rules);
        InputStream referenceXmlStream = ForwardingRulesTest.class
                .getResourceAsStream("forwardingrules-itsp-callback-test.xml");

        assertXMLEqual(new InputStreamReader(referenceXmlStream), new StringReader(generatedXml));
    }

    private ForwardingRules generate(IDialingRule rule, SbcManager sbcManager) {
        ForwardingRules rules = new ForwardingRules();
        rules.setVelocityEngine(TestHelper.getVelocityEngine());
        rules.setSbcManager(sbcManager);
        rules.setSbcDeviceManager(m_sbcDeviceManager);
        rules.setDomainName("example.org");
        rules.setApplicationContext(m_applicationContext);
        rules.begin();
        rules.generate(rule);
        rules.end();
        return rules;
    }

    private Sbc configureSbc(Sbc sbc, SbcDevice sbcDevice, List<String> domains, List<String> subnets) {
        SbcRoutes routes = new SbcRoutes();
        routes.setDomains(domains);
        routes.setSubnets(subnets);

        sbc.setRoutes(routes);
        sbc.setSbcDevice(sbcDevice);
        sbc.setEnabled(true);
        return sbc;
    }

    private SbcDevice configureSbcDevice(String address, int port) {
        SbcDevice sbcDevice = new SbcDevice();
        sbcDevice.setAddress(address);
        sbcDevice.setPort(port);
        return sbcDevice;
    }

    private SbcDevice configureSbcDevice(String address) {
        return configureSbcDevice(address, 0);
    }

    private static class MockForwardingPlugin implements ForwardingRulesPlugin {

        private String m_method;
        private String m_match;
        private String m_pattern;
        private String m_routeTo;
        private String m_featureId;
        private String m_ruri;

        public MockForwardingPlugin(String... params) {
            m_method = params[0];
            m_match = params[1];
            m_pattern = params[2];
            m_routeTo = params[3];
            m_featureId = params[4];
            m_ruri = params[5];
        }

        @Override
        public String getMethodPattern() {
            return m_method;
        }

        @Override
        public String getFieldMatch() {
            return m_match;
        }

        @Override
        public String getFieldPattern() {
            return m_pattern;
        }

        @Override
        public String getRouteTo(Location location) {
            return m_routeTo;
        }

        @Override
        public String getFeatureId() {
            return m_featureId;
        }

        @Override
        public String getRuriParams() {
            return m_ruri;
        }

    }

    private static class MockFeatureManager implements FeatureManager {

        @Override
        public Set<GlobalFeature> getEnabledGlobalFeatures() {
            return null;
        }

        @Override
        public Set<LocationFeature> getEnabledLocationFeatures() {
            return null;
        }

        @Override
        public Set<LocationFeature> getEnabledLocationFeatures(Location location) {
            return null;
        }

        @Override
        public void enableLocationFeature(LocationFeature feature, Location location, boolean enable) {
        }

        @Override
        public void enableLocationFeatures(Set<LocationFeature> features, Location location, boolean enable) {
        }

        @Override
        public void enableGlobalFeature(GlobalFeature feature, boolean enable) {
        }

        @Override
        public void enableGlobalFeatures(Set<GlobalFeature> features, boolean enable) {
        }

        @Override
        public void validateFeatureChange(FeatureChangeValidator validator) {
        }

        @Override
        public void applyFeatureChange(FeatureChangeValidator validator) {
        }

        @Override
        public boolean isFeatureEnabled(LocationFeature feature, Location location) {
            return false;
        }

        @Override
        public boolean isFeatureEnabled(LocationFeature feature) {
            if (feature.getId().equalsIgnoreCase("enabled")) {
                return true;
            }
            return false;
        }

        @Override
        public boolean isFeatureEnabled(GlobalFeature feature) {
            return false;
        }

        @Override
        public Set<GlobalFeature> getAvailableGlobalFeatures() {
            return null;
        }

        @Override
        public Set<LocationFeature> getAvailableLocationFeatures(Location location) {
            return null;
        }

        @Override
        public List<Location> getLocationsForEnabledFeature(LocationFeature feature) {
            return null;
        }

        @Override
        public Bundle getBundle(String id) {
            return null;
        }

        @Override
        public List<Bundle> getBundles() {
            return null;
        }
    }
}

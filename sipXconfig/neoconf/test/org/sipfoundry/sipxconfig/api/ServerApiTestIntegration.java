package org.sipfoundry.sipxconfig.api;

import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.test.RestApiIntegrationTestCase;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.data.mongodb.core.MongoTemplate;

public class ServerApiTestIntegration extends RestApiIntegrationTestCase {
    private MongoTemplate m_imdb;
    LocationsManager m_locationsManager;

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
        m_imdb.dropCollection("entity");

    }

    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        super.onTearDownAfterTransaction();
        m_imdb.dropCollection("entity");
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();

    }

    public void testGetServers() throws Exception {
        loadDataSetXml("commserver/seedLocations.xml");
        flush();
        commit();
        String servers = getAsJson("/servers/");
        String expected = "{'servers':[{'registered':true,'id':101,'host':'localhost','description':'Config Server, Media Server and Comm Server','primary':true,'ip':'192.168.0.26'},{'registered':false,'id':102,'host':'remotehost.example.org','description':'Distributed Comm Server','primary':false,'ip':'192.168.0.27'}]}";
        JSONAssert.assertEquals(expected, servers, false);
    }

    public void testNewServer() throws Exception {
        disableDaoEventPublishing();
        loadDataSetXml("commserver/seedLocations.xml");
        flush();
        String newServer = "{\"host\":\"newserver1.example.org\",\"description\":\"Server description\",\"ip\":\"192.168.0.43\"}";
        int code = postJsonString(newServer, "/servers/");

        assertEquals(200, code);
        int id = m_locationsManager.getLocationByFqdn("newserver1.example.org").getId();
        commit();
        String expected = "{'servers':["
                + "{'registered':true,'id':101,'host':'localhost','description':'Config Server, Media Server and Comm Server','primary':true,'ip':'192.168.0.26'},"
                + "{'registered':false,'id':102,'host':'remotehost.example.org','description':'Distributed Comm Server','primary':false,'ip':'192.168.0.27'},"
                + "{'registered':false,'id':"
                + id
                + ",'host':'newserver1.example.org','description':'Server description','primary':false,'ip':'192.168.0.43'}"
                + "]}";
        String servers = getAsJson("/servers/");
        JSONAssert.assertEquals(expected, servers, false);

        String server = "{'registered':false,'id':"
                + id
                + ",'host':'newserver1.example.org','description':'Server description','primary':false,'ip':'192.168.0.43'}";
        JSONAssert.assertEquals(server, getAsJson("/servers/" + id), false);
        JSONAssert.assertEquals(server, getAsJson("/servers/newserver1.example.org"), false);

        String serverUpdated = "{\"registered\":false,\"id\":"
                + id
                + ",\"host\":\"newserver2.example.org\",\"description\":\"Server description2\",\"primary\":false,\"ip\":\"192.168.0.44\"}";
        int codeupd = putJsonString(serverUpdated, "/servers/" + id);
        assertEquals(200, codeupd);
        JSONAssert.assertEquals(serverUpdated, getAsJson("/servers/" + id), false);

        int codeDel = delete("/servers/" + id);
        assertEquals(200, codeDel);
        String serversAfterDel = getAsJson("/servers/");
        String expectedAfterDel = "{'servers':[{'registered':true,'id':101,'host':'localhost','description':'Config Server, Media Server and Comm Server','primary':true,'ip':'192.168.0.26'},{'registered':false,'id':102,'host':'remotehost.example.org','description':'Distributed Comm Server','primary':false,'ip':'192.168.0.27'}]}";
        JSONAssert.assertEquals(expectedAfterDel, serversAfterDel, false);
    }

    public void testFeaturesAndBundles() throws Exception {
        loadDataSetXml("commserver/seedLocations.xml");
        commit();
        String bundles = getAsJson("/servers/bundles/");
        String expectedBundles = "{\"bundles\":[{\"globalFeatures\":[\"snmp\",\"sipxlogwatcher\",\"mail\",\"firewall\",\"ntpd\",\"fail2ban\",\"alarms\"],\"locationFeatures\":[\"dhcpd\",\"sipxdns\"],\"name\":\"core\"},{\"globalFeatures\":[\"intercom\"],\"locationFeatures\":[\"freeSwitch\",\"authCode\",\"ivr\",\"redis\",\"moh\",\"saa\",\"park\",\"registrar\",\"sbcBridge\",\"rls\",\"sipxcdr\",\"sipxsqa\",\"mwi\",\"page\",\"proxy\",\"callback\",\"conference\",\"restServer\",\"mysql\"],\"name\":\"coreTelephony\"},{\"globalFeatures\":[],\"locationFeatures\":[],\"name\":\"callCenter\"},{\"globalFeatures\":[],\"locationFeatures\":[\"imbot\"],\"name\":\"im\"},{\"globalFeatures\":[],\"locationFeatures\":[\"dhcpd\",\"phonelog\",\"ftp\",\"tftp\"],\"name\":\"provision\"},{\"globalFeatures\":[],\"locationFeatures\":[],\"name\":\"experimental\"}]}";
        JSONAssert.assertEquals(bundles, expectedBundles, false);

        String bundle = getAsJson("/servers/bundles/core");
        String expectedBundle = "{\"globalFeatures\":[\"snmp\",\"sipxlogwatcher\",\"mail\",\"firewall\",\"ntpd\",\"fail2ban\",\"alarms\"],\"locationFeatures\":[\"dhcpd\",\"sipxdns\"],\"name\":\"core\"}";
        JSONAssert.assertEquals(bundle, expectedBundle, false);

        String features = getAsJson("/servers/features/");
        String expectedFeatures = "{\"features\":[{\"enabled\":false,\"bundle\":\"core\",\"name\":\"snmp\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"sipxlogwatcher\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"mail\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"dhcpd\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"firewall\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"sipxdns\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"ntpd\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"fail2ban\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"alarms\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"freeSwitch\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"authCode\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"ivr\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"redis\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"moh\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"saa\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"park\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"registrar\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sbcBridge\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"rls\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sipxcdr\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"intercom\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sipxsqa\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"mwi\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"page\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"proxy\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"callback\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"conference\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"restServer\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"mysql\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"im\",\"name\":\"imbot\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"dhcpd\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"phonelog\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"ftp\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"tftp\",\"type\":\"location\"}]}";
        JSONAssert.assertEquals(expectedFeatures, features, false);

        String serverFeatures = getAsJson("/servers/101/features/");
        String expectedServerFeatures = "{\"features\":[{\"bundle\":\"core\",\"enabled\":false,\"name\":\"dhcpd\",\"type\":\"location\"},{\"bundle\":\"core\",\"enabled\":false,\"name\":\"sipxdns\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"freeSwitch\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"authCode\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"ivr\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"redis\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"moh\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"saa\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"park\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"registrar\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"sbcBridge\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"rls\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"sipxcdr\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"sipxsqa\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"mwi\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"page\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"proxy\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"callback\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"conference\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"restServer\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"mysql\",\"type\":\"location\"},{\"bundle\":\"im\",\"enabled\":false,\"name\":\"imbot\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"dhcpd\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"phonelog\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"ftp\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"tftp\",\"type\":\"location\"}]}";
        JSONAssert.assertEquals(expectedServerFeatures, serverFeatures, false);
        
        int code = putPlainText("", "/servers/features/snmp");
        assertEquals(200, code);
        String serverFeaturesSnmp = getAsJson("/servers/features/");
        String expectedServerFeaturesSnmp = "{\"features\":[{\"enabled\":true,\"bundle\":\"core\",\"name\":\"snmp\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"sipxlogwatcher\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"mail\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"dhcpd\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"firewall\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"sipxdns\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"ntpd\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"fail2ban\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"alarms\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"freeSwitch\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"authCode\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"ivr\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"redis\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"moh\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"saa\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"park\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"registrar\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sbcBridge\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"rls\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sipxcdr\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"intercom\",\"type\":\"global\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sipxsqa\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"mwi\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"page\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"proxy\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"callback\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"conference\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"restServer\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"mysql\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"im\",\"name\":\"imbot\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"dhcpd\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"phonelog\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"ftp\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"tftp\",\"type\":\"location\"}]}";
        JSONAssert.assertEquals(expectedServerFeaturesSnmp, serverFeaturesSnmp, false);
        
        int codeDisable = delete("/servers/features/snmp");
        assertEquals(200, codeDisable);
        JSONAssert.assertEquals(expectedFeatures, getAsJson("/servers/features/"), false);
        
        assertEquals(200,putPlainText("", "/servers/101/features/dhcpd"));
        JSONAssert.assertEquals("{\"features\":[{\"bundle\":\"core\",\"enabled\":true,\"name\":\"dhcpd\",\"type\":\"location\"},{\"bundle\":\"core\",\"enabled\":false,\"name\":\"sipxdns\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"freeSwitch\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"authCode\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"ivr\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"redis\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"moh\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"saa\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"park\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"registrar\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"sbcBridge\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"rls\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"sipxcdr\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"sipxsqa\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"mwi\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"page\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"proxy\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"callback\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"conference\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"restServer\",\"type\":\"location\"},{\"bundle\":\"coreTelephony\",\"enabled\":false,\"name\":\"mysql\",\"type\":\"location\"},{\"bundle\":\"im\",\"enabled\":false,\"name\":\"imbot\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":true,\"name\":\"dhcpd\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"phonelog\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"ftp\",\"type\":\"location\"},{\"bundle\":\"provision\",\"enabled\":false,\"name\":\"tftp\",\"type\":\"location\"}]}", getAsJson("/servers/101/features"), false);
        
        assertEquals(200,delete("/servers/101/features/dhcpd"));
        JSONAssert.assertEquals("{\"features\":[{\"enabled\":false,\"bundle\":\"core\",\"name\":\"dhcpd\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"core\",\"name\":\"sipxdns\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"freeSwitch\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"authCode\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"ivr\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"redis\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"moh\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"saa\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"park\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"registrar\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sbcBridge\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"rls\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sipxcdr\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"sipxsqa\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"mwi\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"page\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"proxy\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"callback\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"conference\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"restServer\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"coreTelephony\",\"name\":\"mysql\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"im\",\"name\":\"imbot\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"dhcpd\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"phonelog\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"ftp\",\"type\":\"location\"},{\"enabled\":false,\"bundle\":\"provision\",\"name\":\"tftp\",\"type\":\"location\"}]}", getAsJson("/servers/101/features"), false);
        
    }

    public void setImdb(MongoTemplate imdb) {
        m_imdb = imdb;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

}

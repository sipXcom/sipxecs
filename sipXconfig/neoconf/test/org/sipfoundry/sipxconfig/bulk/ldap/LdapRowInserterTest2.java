package org.sipfoundry.sipxconfig.bulk.ldap;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.classextension.EasyMock.createMock;

import java.util.Collections;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.easymock.IMocksControl;
import org.sipfoundry.sipxconfig.admin.AdminContext;
import org.sipfoundry.sipxconfig.bulk.RowInserter.RowStatus;
import org.sipfoundry.sipxconfig.bulk.csv.Index;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setting.SettingDao;

public class LdapRowInserterTest2 extends LdapRowInserterTest {        
    
    //The inserted user will be "joe"
    //1. Strip first two characters (AM)
    //2. Strip all characters that match regular expression [^a-zA-Z] 
    //which means all characters except uppercase and lowercasse characters
    //3. Add prefix "j"
    //4. Add suffix "e"
    
    @Override
    protected void setUp() {
        m_joe = "AMo (4) 777 ((}}}}}}]]]";
        m_rowInserter = new LdapRowInserter();
        m_adminContext = createMock(AdminContext.class);
        m_adminContext.getNewLdapUserGroupNamePrefix();
        expectLastCall().andReturn("grPrefix_").anyTimes();
        m_adminContext.getStripUserName();
        expectLastCall().andReturn(2).anyTimes();
        m_adminContext.getRegexUsername();
        expectLastCall().andReturn("[^a-zA-Z]").anyTimes();
        m_adminContext.getPrefixUsername();
        expectLastCall().andReturn("j").anyTimes();
        m_adminContext.getSuffixUsername();
        expectLastCall().andReturn("e").anyTimes();
        replay(m_adminContext);
        Group newGroup = new Group();
        newGroup.setName("grPrefix_example.com");
        newGroup.setResource(CoreContext.USER_GROUP_RESOURCE_ID);
        m_settingDao = createMock(SettingDao.class);
        m_settingDao.getGroupCreateIfNotFound(CoreContext.USER_GROUP_RESOURCE_ID, "grPrefix_example.com");
        expectLastCall().andReturn(newGroup);
        replay(m_settingDao);
        AttrMap attrMap = new AttrMap();
        attrMap.setDefaultGroupName("test-import");
        attrMap.setAttribute(Index.USERNAME.getName(), "identity");
        m_rowInserter.setAttrMap(attrMap);
        m_rowInserter.setAdminContext(m_adminContext);
        m_rowInserter.setSettingDao(m_settingDao);
    }
    
    /**
     * Test applies in cases when username is not valid when not formatted and valid otherwise not @see LdapRowInserter.formatUserName
     */
    @Override
    public void testCheckRowDataNotValid() throws Exception {
        IMocksControl control = org.easymock.classextension.EasyMock.createNiceControl();
        UserMapper userMapper = control.createMock(UserMapper.class);
        SearchResult searchResult = control.createMock(SearchResult.class);
        Attributes attributes = control.createMock(Attributes.class);
        Attribute attribute = control.createMock(Attribute.class);
        
        searchResult.getAttributes();
        control.andReturn(attributes);
        attributes.get("identity");
        control.andReturn(attribute);
        userMapper.getUserName(attributes);
        control.andReturn("@McQueen");
        userMapper.getGroupNames(searchResult);
        control.andReturn(Collections.singleton(SALES));
        control.replay();
        m_rowInserter.setUserMapper(userMapper);
        m_rowInserter.beforeInserting(null);
        assertEquals(RowStatus.SUCCESS, m_rowInserter.checkRowData(searchResult).getRowStatus());
    }    
}

/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.admin.dialplan.attendant;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.sipfoundry.sipxconfig.admin.dialplan.config.XmlFile;
import org.sipfoundry.sipxconfig.admin.forwarding.AliasMapping;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.domain.DomainManager;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.springframework.beans.factory.annotation.Required;

public class ValidUsersConfig extends XmlFile {

    private static final String NAMESPACE = "http://www.sipfoundry.org/sipX/schema/xml/validusers-00-00";

    private CoreContext m_coreContext;

    private DomainManager m_domainManager;

    @Override
    public Document getDocument() {
        Document document = FACTORY.createDocument();
        QName validUsersName = FACTORY.createQName("validusers", NAMESPACE);
        Element usersEl = document.addElement(validUsersName);
        // FIXME: should be paging here...
        List<User> users = m_coreContext.loadUsers();
        for (User user : users) {
            generateUser(usersEl, user);
        }
        return document;
    }

    private void generateUser(Element usersEl, User user) {
        String domainName = m_domainManager.getDomain().getName();

        boolean inDirectory = user.hasPermission(PermissionName.AUTO_ATTENDANT_DIALING);
        List<AliasMapping> aliasMappings = user.getAliasMappings(domainName);
        aliasMappings.add(0, createIdentityMapping(user, domainName));
        for (AliasMapping aliasMapping : aliasMappings) {
            Element userEl = usersEl.addElement("user");
            userEl.addElement("identity").setText(aliasMapping.getIdentity());
            userEl.addElement("userName").setText(user.getUserName());
            String displayName = user.getDisplayName();
            if (displayName != null) {
                userEl.addElement("displayName").setText(displayName);
            }
            userEl.addElement("contact").setText(aliasMapping.getContact());
            userEl.addElement("pintoken").setText(user.getPintoken());
            userEl.addElement("inDirectory").setText(Boolean.toString(inDirectory));
            // only for the first entry
            inDirectory = false;
        }
    }

    private AliasMapping createIdentityMapping(User user, String domainName) {
        String identity = AliasMapping.createUri(user.getUserName(), domainName);
        String contact = user.getUri(domainName);
        return new AliasMapping(identity, contact);
    }

    @Required
    public void setDomainManager(DomainManager domainManager) {
        m_domainManager = domainManager;
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }
}

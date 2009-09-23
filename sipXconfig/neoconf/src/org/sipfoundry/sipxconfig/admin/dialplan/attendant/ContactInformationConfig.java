/*
 *
 *
 * Copyright (C) 2009 Nortel, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.admin.dialplan.attendant;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.sipfoundry.sipxconfig.admin.dialplan.config.XmlFile;
import org.sipfoundry.sipxconfig.common.Closure;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.conference.ConferenceBridgeContext;
import org.sipfoundry.sipxconfig.phonebook.Address;
import org.sipfoundry.sipxconfig.phonebook.AddressBookEntry;
import org.springframework.beans.factory.annotation.Required;

import static org.sipfoundry.sipxconfig.common.DaoUtils.forAllUsersDo;

public class ContactInformationConfig extends XmlFile {
    private static final Log LOG = LogFactory.getLog(ContactInformationConfig.class);

    private static final String NAMESPACE = "http://www.sipfoundry.org/sipX/schema/xml/contactinfo-00-00";
    private CoreContext m_coreContext;
    private ConferenceBridgeContext m_conferenceBridgeContext;

    @Override
    public Document getDocument() {
        Document document = FACTORY.createDocument();
        final Element contactInfos = document.addElement("contact-info", NAMESPACE);

        Closure<User> closure = new Closure<User>() {
            @Override
            public void execute(User user) {
                generateUser(user, contactInfos);
            }
        };
        forAllUsersDo(m_coreContext, closure);
        return document;
    }

    private void generateUser(User user, Element element) {
        Element userEl = element.addElement("user");
        userEl.addElement("userName").setText(user.getUserName());
        AddressBookEntry abe = user.getAddressBookEntry();
        if (abe != null) {
            addElements(userEl, abe, "imId", "imDisplayName", "alternateImId", "jobTitle", "jobDept", "companyName",
                    "assistantName", "assistantPhoneNumber", "faxNumber", "location", "homePhoneNumber",
                    "cellPhoneNumber");

            Element homeAddressEl = userEl.addElement("homeAddress");
            addAddressInfo(homeAddressEl, abe.getHomeAddress());
            Element officeAddressEl = userEl.addElement("officeAddress");
            addAddressInfo(officeAddressEl, abe.getOfficeAddress());
            addElements(officeAddressEl, abe.getOfficeAddress(), "officeDesignation");
        }

        List<Conference> conferences = m_conferenceBridgeContext.findConferencesByOwner(user);
        Element conferencesEl = userEl.addElement("conferences");
        for (Conference conference : conferences) {
            Element conferenceElement = conferencesEl.addElement("conference");
            // conference name and extension are required for a conference thus we need not to
            // check for null/empty here
            conferenceElement.addElement("name").setText(conference.getName());
            conferenceElement.addElement("extension").setText(conference.getExtension());
        }
    }

    private void addAddressInfo(Element element, Address address) {
        addElements(element, address, "street", "city", "country", "state", "zip");
    }

    private void addElement(Element userEl, Object bean, String name) {
        try {
            String value = BeanUtils.getSimpleProperty(bean, name);
            if (!StringUtils.isEmpty(value)) {
                userEl.addElement(name).setText(value);
            }
        } catch (IllegalAccessException e) {
            LOG.error(e);
        } catch (InvocationTargetException e) {
            LOG.error(e);
        } catch (NoSuchMethodException e) {
            LOG.error(e);
        }
    }

    private void addElements(Element userEl, Object bean, String... names) {
        for (String name : names) {
            addElement(userEl, bean, name);
        }
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Required
    public void setConferenceBridgeContext(ConferenceBridgeContext conferenceBridgeContext) {
        m_conferenceBridgeContext = conferenceBridgeContext;
    }
}

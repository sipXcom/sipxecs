/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.site.search;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.sipfoundry.sipxconfig.acccode.AuthCode;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.conference.Bridge;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.dialplan.AttendantRule;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;
import org.sipfoundry.sipxconfig.dialplan.CustomDialingRule;
import org.sipfoundry.sipxconfig.dialplan.EmergencyRule;
import org.sipfoundry.sipxconfig.dialplan.InternalRule;
import org.sipfoundry.sipxconfig.dialplan.InternationalRule;
import org.sipfoundry.sipxconfig.dialplan.LocalRule;
import org.sipfoundry.sipxconfig.dialplan.LongDistanceRule;
import org.sipfoundry.sipxconfig.dialplan.SiteToSiteDialingRule;
import org.sipfoundry.sipxconfig.gateway.Gateway;
import org.sipfoundry.sipxconfig.parkorbit.ParkOrbit;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.site.admin.EditAuthCode;
import org.sipfoundry.sipxconfig.site.admin.EditCallGroup;
import org.sipfoundry.sipxconfig.site.branch.EditBranch;
import org.sipfoundry.sipxconfig.site.conference.EditBridge;
import org.sipfoundry.sipxconfig.site.conference.EditConference;
import org.sipfoundry.sipxconfig.site.dialplan.EditAutoAttendant;
import org.sipfoundry.sipxconfig.site.gateway.EditGateway;
import org.sipfoundry.sipxconfig.site.park.EditParkOrbit;
import org.sipfoundry.sipxconfig.site.phone.EditPhone;
import org.sipfoundry.sipxconfig.site.setting.EditGroup;
import org.sipfoundry.sipxconfig.site.upload.EditUpload;
import org.sipfoundry.sipxconfig.site.user.EditUser;
import org.sipfoundry.sipxconfig.upload.Upload;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * This is a class in charge of determining which "edit" page should be used for an entity object
 * (bean). It does not seem to be any elegant way of solving this problem - we have object type
 * and id and we need to find an edit page for it.
 *
 */
public class EnumEditPageProvider implements EditPageProvider, BeanFactoryAware {
    public static final Log LOG = LogFactory.getLog(EnumEditPageProvider.class);

    public static final String RULE_ID = "ruleId";

    public static final Object[] PAGES = {
        User.class, new String[] {
            EditUser.PAGE, "userId"
        }, Group.class, new String[] {
            EditGroup.PAGE, "groupId"
        }, Phone.class, new String[] {
            EditPhone.PAGE, "phoneId"
        }, Gateway.class, new String[] {
            EditGateway.PAGE, "gatewayId"
        }, CallGroup.class, new String[] {
            EditCallGroup.PAGE, "callGroupId"
        }, Bridge.class, new String[] {
            EditBridge.PAGE, "bridgeId"
        }, Conference.class, new String[] {
            EditConference.PAGE, "conferenceId"
        }, ParkOrbit.class, new String[] {
            EditParkOrbit.PAGE, "parkOrbitId"
        }, AutoAttendant.class, new String[] {
            EditAutoAttendant.PAGE, "autoAttendantId"
        }, InternalRule.class, new String[] {
            "dialplan/EditInternalDialRule", RULE_ID
        }, CustomDialingRule.class, new String[] {
            "dialplan/EditCustomDialRule", RULE_ID
        }, LocalRule.class, new String[] {
            "dialplan/EditLocalDialRule", RULE_ID
        }, LongDistanceRule.class, new String[] {
            "dialplan/EditLongDistanceDialRule", RULE_ID
        }, EmergencyRule.class, new String[] {
            "dialplan/EditEmergencyDialRule", RULE_ID
        }, InternationalRule.class, new String[] {
            "dialplan/EditInternationalDialRule", RULE_ID
        }, AttendantRule.class, new String[] {
            "dialplan/EditAttendantDialRule", RULE_ID
        }, SiteToSiteDialingRule.class, new String[] {
            "dialplan/EditSiteToSiteDialRule", RULE_ID
        }, Upload.class, new String[] {
            EditUpload.PAGE, "uploadId"
        }, Branch.class, new String[] {
            EditBranch.PAGE, "branchId"
        }, AuthCode.class, new String[] {
            EditAuthCode.PAGE, "authCodeId"
        }
    };

    private ListableBeanFactory m_beanFactory;
    private Map m_classToPageInfo = null;

    private Map getClassToPageInfo() {
        if (m_classToPageInfo == null) {
            List<Object> classesList = new ArrayList<Object>(Arrays.asList(PAGES));
            Map<String, EnumEditPageProviderPlugin> enumEditPageProviderPlugin = m_beanFactory.getBeansOfType(EnumEditPageProviderPlugin.class);
            if (enumEditPageProviderPlugin != null) {
                Collection<EnumEditPageProviderPlugin> enumEditPageProviderPluginClasses = enumEditPageProviderPlugin.values();
                if (!enumEditPageProviderPluginClasses.isEmpty()) {
                    for (EnumEditPageProviderPlugin beanAdaptorPlugin : enumEditPageProviderPluginClasses) {
                        Object[] indexedClasses = beanAdaptorPlugin.getPages();
                        classesList.addAll(Arrays.asList(indexedClasses));
                    }
                }
            }

            int size = classesList.size();
            m_classToPageInfo = new HashMap(size / 2);
            for (int i = 0; i < size; i = i + 2) {
                Class klass = (Class) classesList.get(i);
                m_classToPageInfo.put(klass, classesList.get(i + 1));
            }
        }
        return m_classToPageInfo;
    }

    /**
     * This is used only in unit tests. We are making sure that all the pages that we reference
     * are actually available and that they have settebale "id" field.
     */
    public void validatePages(IRequestCycle cycle) {
        for (Iterator i = getClassToPageInfo().values().iterator(); i.hasNext();) {
            String[] pageInfo = (String[]) i.next();
            getEditPage(cycle, null, pageInfo, false);
        }
    }

    public IPage getPage(IRequestCycle cycle, Class klass, Object id) {
        for (Class k = klass; k != Object.class; k = k.getSuperclass()) {
            String[] pageInfo = (String[]) getClassToPageInfo().get(k);
            if (pageInfo != null) {
                return getEditPage(cycle, id, pageInfo, true);
            }
        }
        return null;
    }

    private IPage getEditPage(IRequestCycle cycle, Object id, String[] pageInfo, boolean ignoreExceptions) {
        Exception exception = null;
        try {
            IPage page = cycle.getPage(pageInfo[0]);
            // HACK: see http://issues.apache.org/bugzilla/show_bug.cgi?id=16525
            // we need to use copyProperty and not setProperty
            BeanUtils.copyProperty(page, pageInfo[1], id);
            return page;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        }
        if (!ignoreExceptions) {
            throw new RuntimeException(exception);
        }
        // if silent we only log it
        LOG.error(exception);
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = (ListableBeanFactory) beanFactory;
    }
}

/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.openfire;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sipfoundry.sipxconfig.bulk.ldap.AttrMap;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapConnectionParams;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
import org.sipfoundry.sipxconfig.bulk.ldap.LdapSystemSettings;
import org.sipfoundry.sipxconfig.common.AbstractUser;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.springframework.beans.factory.annotation.Required;

public class OpenfireConfigurationFile {
    private static final String PROVIDER_ADMIN_CLASSNAME = "org.jivesoftware.openfire.admin.DefaultAdminProvider";
    private static final String PROVIDER_LOCKOUT_CLASSNAME = "org.jivesoftware.openfire.lockout.DefaultLockOutProvider";
    private static final String PROVIDER_SECURITY_AUDIT_CLASSNAME =
        "org.jivesoftware.openfire.security.DefaultSecurityAuditProvider";
    private static final String SEPARATOR = ", ";
    private static final String ADMIN = "admin";

    private String m_providerAuthClassName;
    private String m_providerUserClassName;
    private String m_providerGroupClassName;
    private String m_providerVCardClassName;

    private String m_providerLdapAuthClassName;
    private String m_providerLdapUserClassName;
    private String m_providerLdapVCardClassName;
    private String m_multipleLdapConfFile;
    private boolean m_clusteringState;

    private Map<String, String> m_additionalProperties;

    private LdapManager m_ldapManager;
    private CoreContext m_coreContext;
    private VelocityEngine m_velocityEngine;

    public void write(Writer writer) throws IOException {
        VelocityContext context = new VelocityContext();
        LdapSystemSettings settings = m_ldapManager.getSystemSettings();
        boolean isEnableOpenfireConfiguration = settings.isEnableOpenfireConfiguration() && settings.isConfigured();
        context.put("isEnableOpenfireConfiguration", isEnableOpenfireConfiguration);
        List<LdapConnectionParams> allParams = m_ldapManager.getAllConnectionParams();
        if (!isEnableOpenfireConfiguration || allParams == null || allParams.isEmpty()) {
            context.put("adminProvider", PROVIDER_ADMIN_CLASSNAME);
            context.put("authProvider", m_providerAuthClassName);
            context.put("groupProvider", m_providerGroupClassName);
            context.put("userProvider", m_providerUserClassName);
            context.put("lockoutProvider", PROVIDER_LOCKOUT_CLASSNAME);
            context.put("securityAuditProvider", PROVIDER_SECURITY_AUDIT_CLASSNAME);
            context.put("sipxVcardProvider", m_providerVCardClassName);
        } else if (!allParams.isEmpty()) {
            LdapConnectionParams ldapConnectionParams = allParams.get(0);
            boolean isLdapAnonymousAccess = (StringUtils.isBlank(ldapConnectionParams.getPrincipal())) ? true
                    : false;
            context.put("isLdapAnonymousAccess", isLdapAnonymousAccess);
            context.put("ldapParams", ldapConnectionParams);
            context.put("attrMap", m_ldapManager.getAttrMap(ldapConnectionParams.getId()));
            context.put("ldapAuthProvider", m_providerLdapAuthClassName);
            context.put("ldapUserProvider", m_providerLdapUserClassName);
            context.put("ldapVcardProvider", m_providerLdapVCardClassName);
        }

        context.put("authorizedUsernames", getAuthorizedUsernames());
        context.put("clusteringState", m_clusteringState);

        try {
            m_velocityEngine.mergeTemplate("openfire/openfire.vm", context, writer);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void writeMultipleLdapConfiguration(Writer writer) throws IOException {
        if (m_multipleLdapConfFile != null) {
            List<LdapConnectionParams> allParams = m_ldapManager.getAllConnectionParams();
            VelocityContext context = new VelocityContext();
            List<LdapData> ldapDataList = new ArrayList<LdapData>();
            for (LdapConnectionParams params : allParams) {
                ldapDataList.add(new LdapData(params, m_ldapManager.getAttrMap(params.getId())));
            }
            context.put("ldapDataList", ldapDataList);
            try {
                m_velocityEngine.mergeTemplate(m_multipleLdapConfFile, context, writer);
            } catch(Exception e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Get authorized usernames. The defaults are admin and superadmin.
     * When you have LDAP-Openfire configured different other users
     * can be added with admin rights.
     */
    private String getAuthorizedUsernames() {
        List<User> admins = m_coreContext.loadUserByAdmin();
        Set<String> authorizedList = new TreeSet<String>();
        authorizedList.add(ADMIN);
        authorizedList.add(AbstractUser.SUPERADMIN);
        for (User user : admins) {
            authorizedList.add(user.getUserName());
        }
        return StringUtils.join(authorizedList, SEPARATOR);
    }

    @Required
    public void setLdapManager(LdapManager ldapManager) {
        m_ldapManager = ldapManager;
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        m_velocityEngine = velocityEngine;
    }

    @Required
    public void setProviderAuthClassName(String providerAuthClassName) {
        m_providerAuthClassName = providerAuthClassName;
    }

    public String getProviderAuthClassName() {
        return m_providerAuthClassName;
    }

    @Required
    public void setProviderUserClassName(String providerUserClassName) {
        m_providerUserClassName = providerUserClassName;
    }

    @Required
    public void setProviderGroupClassName(String providerGroupClassName) {
        m_providerGroupClassName = providerGroupClassName;
    }

    @Required
    public void setProviderVCardClassName(String providerVCardClassName) {
        m_providerVCardClassName = providerVCardClassName;
    }

    @Required
    public void setProviderLdapAuthClassName(String providerLdapAuthClassName) {
        m_providerLdapAuthClassName = providerLdapAuthClassName;
    }

    public String getProviderLdapAuthClassName() {
        return m_providerLdapAuthClassName;
    }

    @Required
    public void setProviderLdapUserClassName(String providerLdapUserClassName) {
        m_providerLdapUserClassName = providerLdapUserClassName;
    }

    @Required
    public void setProviderLdapVCardClassName(String providerLdapVCardClassName) {
        m_providerLdapVCardClassName = providerLdapVCardClassName;
    }

    public void setMultipleLdapConfFile(String multipleLdapConfFile) {
        m_multipleLdapConfFile = multipleLdapConfFile;
    }

    public void setAdditionalProperties(Map<String, String> properties) {
        m_additionalProperties = properties;
    }

    public Map<String, String> getAdditionalProperties() {
        return m_additionalProperties;
    }

    public void setClusteringState(boolean state) {
        m_clusteringState = state;
    }

    public static class LdapData {
        private final LdapConnectionParams m_ldapParams;
        private final AttrMap m_attrMap;
        private final boolean m_ldapAnonymousAccess;
        public LdapData(LdapConnectionParams ldapParams, AttrMap attrMap) {
            m_ldapParams = ldapParams;
            m_attrMap = attrMap;
            m_ldapAnonymousAccess = (StringUtils.isBlank(m_ldapParams.getPrincipal())) ? true : false;
        }
        public LdapConnectionParams getLdapParams() {
            return m_ldapParams;
        }

        public AttrMap getAttrMap() {
            return m_attrMap;
        }

        public String getImAttribute() {
            String imAttribute = m_attrMap.getImAttributeName();
            String usernameAttribute = m_attrMap.getIdentityAttributeName();
            //if im id is not mapped, default it to username -
            //because this is a rule, when a user gets created the im id has to automatically be defaulted to username
            return  imAttribute == null ?
                    usernameAttribute == null ? StringUtils.EMPTY : usernameAttribute : imAttribute;
        }

        public String getDomain() {
            String domain = m_ldapParams.getDomain();
            return domain == null ? StringUtils.EMPTY : domain;
        }

        public boolean isLdapAnonymousAccess() {
            return m_ldapAnonymousAccess;
        }
    }
}

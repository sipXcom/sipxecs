/*
 * Copyright (C) 2012 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.cert;

import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class CertificateSettings extends PersistableSettings {

    private static final String USE_LETS_ENCRYPT = "letsencrypt/useLetsEncrypt";
    private static final String CERTBOT_PARAMS = "letsencrypt/certbotParams";
    private static final String LETS_ENCRYPT_EMAIL = "letsencrypt/letsEncryptEmail";
    private static final String LETS_ENCRYPT_KEY_SIZE = "letsencrypt/letsEncryptKeySize";

    public CertificateSettings() {
        addDefaultBeanSettingHandler(new Defaults());
    }

    public class Defaults {
        @SettingEntry(path = "csr/organization")
        public String getOrganization() {
            return Domain.getDomain().getName();
        }

        @SettingEntry(path = "csr/email")
        public String getEmail() {
            return "root@" + getOrganization();
        }
    }

    public boolean getUseLetsEncrypt() {
        return (Boolean)getSettingTypedValue(USE_LETS_ENCRYPT);
    }

    public String getCertbotParams() {
        return (String)getSettingTypedValue(CERTBOT_PARAMS);
    }

    public String getLetsEncryptEmail() {
        return (String)getSettingTypedValue(LETS_ENCRYPT_EMAIL);
    }

    public Integer getLetsEncryptKeySize() {
        return (Integer)getSettingTypedValue(LETS_ENCRYPT_KEY_SIZE);
    }

    @Override
    public String getBeanId() {
        return "certificateSettings";
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("certificate/certificate.xml");
    }

    public void updateCertificateDetails(AbstractCertificateCommon info) {
        info.setCountry(getSettingValue("csr/country"));
        info.setState(getSettingValue("csr/state"));
        info.setLocality(getSettingValue("csr/locality"));
        info.setOrganization(getSettingValue("csr/organization"));
        info.setOrganizationUnit(getSettingValue("csr/organizationUnit"));
        info.setEmail(getSettingValue("csr/email"));
    }
}

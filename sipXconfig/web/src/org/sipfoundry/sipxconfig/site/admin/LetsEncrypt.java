/**
 *
 * Copyright (c) 2019 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.sipxconfig.site.admin;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.cert.CertificateManager;
import org.sipfoundry.sipxconfig.cert.CertificateSettings;
import org.sipfoundry.sipxconfig.cert.CommandExecutionStatus;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.site.common.IntegerPropertySelectionModel;

public abstract class LetsEncrypt extends BaseComponent implements PageBeginRenderListener {
    private static final Log LOG = LogFactory.getLog(LetsEncrypt.class);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject(value = "spring:certificateManager")
    public abstract CertificateManager getCertificateManager();

    public abstract String getLetsEncryptEmailValue();

    public abstract void setLetsEncryptEmailValue(String letsEncryptEmailValue);

    public abstract void setKeySize(int keySize);

    public abstract int getKeySize();

    @Override
    public void pageBeginRender(PageEvent evt) {
        if (!TapestryUtils.isValid(this)) {
            return;
        }

        setKeySize(getCertificateManager().getSettings().getLetsEncryptKeySize());
        setLetsEncryptEmailValue(getCertificateManager().getSettings().getLetsEncryptEmail());
    }

    public IPropertySelectionModel getKeySizeModel() {
        return new IntegerPropertySelectionModel(this, new int[] {
            2048, 4096
        });
    }

    public void applySettings() {
        String email = getLetsEncryptEmailValue();

        if (StringUtils.isBlank(email) || !email.matches(".+@.+\\..+")) {
            getValidator().record(new UserException("&msg.invalidEmail"), getMessages());
            return;
        }

        getCertificateManager().configureLetsEncryptService(email, getKeySize());
    }

    public void disableLetsEncryptService() {
        getCertificateManager().disableLetsEncryptService();
    }

    public String getLetsEncryptStatus() {
        CommandExecutionStatus status = getCertificateManager().getCertbotCommandStatus();
        String msg;

        switch (status) {
        case IN_PROGRESS:
            msg = getMessages().getMessage("label.inProgress"); break;
        case SUCCESS:
            msg = getMessages().getMessage("label.success"); break;
        default:
            msg = getMessages().getMessage("label.failed"); break;
        }

        return msg;
    }

    public String getButtonText() {
        if (getCertificateManager().getLetsEncryptStatus()) {
            return getMessages().getMessage("button.apply");
        } else {
            return getMessages().getMessage("button.enable");
        }
    }

    public boolean getAutoRefresh() {
        CertificateManager mgr = getCertificateManager();
        return (mgr.getLetsEncryptStatus() && mgr.getCertbotCommandStatus().equals(CommandExecutionStatus.IN_PROGRESS));
    }
}

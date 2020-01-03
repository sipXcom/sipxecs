/*
 * Copyright (C) 2019 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.domain;

import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class DomainSettings extends PersistableSettings {

    @Override
    public String getBeanId() {
        return "domainSettings";
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("domain/domain.xml");
    }

    public int getAliasLength() {
        return (Integer) getSettingTypedValue("domain/aliasListLength");
    }
}

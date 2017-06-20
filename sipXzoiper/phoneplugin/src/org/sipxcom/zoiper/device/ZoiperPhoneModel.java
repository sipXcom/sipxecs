/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.device;

import org.sipfoundry.sipxconfig.phone.PhoneModel;
import org.springframework.beans.factory.annotation.Required;

public class ZoiperPhoneModel extends PhoneModel {
    private String m_phonebookProfileTemplate;
    
    public ZoiperPhoneModel() {
        super();
    }

    public ZoiperPhoneModel(String beanId) {
        super(beanId);
    }

    public ZoiperPhoneModel(String beanId, String modelId) {
        super(beanId, modelId);
    }

    public String getPhonebookProfileTemplate() {
        return m_phonebookProfileTemplate;
    }

    @Required
    public void setPhonebookProfileTemplate(String phonebookProfileTemplate) {
        m_phonebookProfileTemplate = phonebookProfileTemplate;
    }            
}

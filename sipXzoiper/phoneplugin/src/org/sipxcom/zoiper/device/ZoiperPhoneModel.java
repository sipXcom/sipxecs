/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.zoiper.device;

import org.sipfoundry.sipxconfig.phone.PhoneModel;

public class ZoiperPhoneModel extends PhoneModel {
    public ZoiperPhoneModel() {
        super();
    }

    public ZoiperPhoneModel(String beanId) {
        super(beanId);
    }

    public ZoiperPhoneModel(String beanId, String modelId) {
        super(beanId, modelId);
    }
}

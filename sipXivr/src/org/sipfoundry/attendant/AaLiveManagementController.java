/**
 *
 *
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.attendant;

import java.util.Hashtable;

import org.sipfoundry.commons.freeswitch.Collect;
import org.sipfoundry.commons.freeswitch.eslrequest.AbstractEslRequestController;
import org.springframework.beans.factory.annotation.Required;

public class AaLiveManagementController extends AbstractEslRequestController {
    private String m_dialedNumber;
    private int m_maxDigits;
    private int m_firstDigitTimeout;
    private int m_interDigitTimeout;
    private int m_extraDigitTimeout;

    @Override
    public void extractParameters(Hashtable<String, String> parameters) {
        m_dialedNumber = parameters.get("dialed");
    }

    public String getDialedNumber() {
        return m_dialedNumber;
    }

    @Override
    public void loadConfig() {
        initLocalization("AutoAttendant", "org.sipfoundry.attendant.AutoAttendant");
    }

    public String promptForCode() {
        Collect c = new Collect(getFsEventSocket(), m_maxDigits,
                m_firstDigitTimeout * 1000, m_interDigitTimeout * 1000,
                m_extraDigitTimeout * 1000);
        c.setTermChars("#");
        c.go();
        return c.getDigits();
    }

    @Required
    public void setMaxDigits(int maxDigits) {
        this.m_maxDigits = maxDigits;
    }

    @Required
    public void setFirstDigitTimeout(int firstDigitsTimeout) {
        this.m_firstDigitTimeout = firstDigitsTimeout;
    }

    @Required
    public void setInterDigitTimeout(int interDigitsTimeout) {
        this.m_interDigitTimeout = interDigitsTimeout;
    }

    @Required
    public void setExtraDigitTimeout(int extraDigitsTimeout) {
        this.m_extraDigitTimeout = extraDigitsTimeout;
    }
}

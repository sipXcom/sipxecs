/*
 *
 *
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.dialplan;

import org.sipfoundry.sipxconfig.dialplan.config.Transform;
import org.sipfoundry.sipxconfig.dialplan.config.UrlTransform;

public class CallbackRule extends DialingRule {

    private static final String USER_PART = "CBB";
    private static final String CBB_FQDN_PREFIX = "cbb.";
    private final UrlTransform m_transform;
    private final DialPattern m_dialPattern;


    public CallbackRule(String prefix, String addrLocation) {
        setEnabled(true);
        m_dialPattern = new DialPattern(prefix, DialPattern.VARIABLE_DIGITS);
        m_transform = new UrlTransform();
        m_transform.setUrl(MappingRule.buildUrl(USER_PART, CBB_FQDN_PREFIX + addrLocation, null, null, null));
    }

    @Override
    public String getDescription() {
        return "Callback on Busy";
    }

    @Override
    public String[] getPatterns() {
        return new String[] {
            m_dialPattern.calculatePattern()
        };
    }

    @Override
    public Transform[] getTransforms() {
        return new Transform[] {
            m_transform
        };
    }

    @Override
    public DialingRuleType getType() {
        return DialingRuleType.CALLBACK;
    }

    public boolean isInternal() {
        return true;
    }

    public boolean isGatewayAware() {
        return false;
    }

    @Override
    public CallTag getCallTag() {
        return CallTag.CBB;
    }

}

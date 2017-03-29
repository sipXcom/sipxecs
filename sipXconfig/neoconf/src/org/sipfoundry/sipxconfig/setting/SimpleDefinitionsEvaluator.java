/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.setting;

import java.util.Set;

/**
 * Look for expression in the set: it supports alternative (denoted as "||" operator) and regular expressions
 */
public class SimpleDefinitionsEvaluator implements SettingExpressionEvaluator {
    private final Set m_defines;

    public SimpleDefinitionsEvaluator(Set defines) {
        m_defines = defines;
    }

    public boolean isExpressionTrue(String expression, Setting setting_) {
        for (Object define : m_defines) {
            if (define instanceof String) {
                if (((String) define).matches(expression)) {
                    return true;
                }
            }
        }
        return false;
    }
}

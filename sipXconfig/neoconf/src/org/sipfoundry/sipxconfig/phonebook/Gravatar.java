/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.phonebook;

import static java.lang.String.format;

import org.sipfoundry.sipxconfig.common.User;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * Calculates URL for the users avatar provided by gravatar.com service
 */
public class Gravatar {
    public enum DefaultType {
        identicon, monsterid, wavatar
    }

    private final User m_user;
    private DefaultType m_type = DefaultType.wavatar;
    private int m_size = 80;

    public Gravatar(User user) {
        m_user = user;
    }

    public void setType(DefaultType type) {
        m_type = type;
    }

    public void setSize(int size) {
        m_size = size;
    }

    /**
     * Calculates URL for the users avatar provided by gravatar.com service
     *
     * @return URL that can be used to retrieve gravatar
     */
    public String getUrl() {
        String email = m_user.getEmailAddress();
        if (email == null) {
            return null;
        }

        String md5Email = md5Hex(email.toLowerCase());
        return format("http://www.gravatar.com/avatar/%s?s=%d&d=%s", md5Email, m_size, m_type);
    }

    /**
     * Calculates sign-up URL - should be used to direct users to sign up for gravatar service.
     *
     * @param mailboxManager until we have independent source of mailbox preferences we need to
     *        pass mailbox manager around
     */
    public String getSignupUrl() {
        String email = defaultString(m_user.getEmailAddress()).toLowerCase();
        return format("http://en.gravatar.com/site/signup/%s", email);
    }
}

/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.commserver.imdb;

import java.util.Date;

import org.apache.commons.lang.builder.CompareToBuilder;

public class RegistrationItem implements Comparable {
    private String m_uri;
    private String m_contact;
    private Date m_expires;
    private String m_primary;
    private String m_instrument;
    private String m_regCallId;
    private String m_identity;

    public String getContact() {
        return m_contact;
    }

    public void setContact(String contact) {
        m_contact = contact;
    }

    public Date getExpires() {
        return m_expires;
    }

    public void setExpires(Date expires) {
        m_expires = expires;
    }

    public String getUri() {
        return m_uri;
    }

    public void setUri(String uri) {
        m_uri = uri;
    }

    public String getPrimary() {
        return m_primary;
    }

    public void setPrimary(String primary) {
        m_primary = primary;
    }

    public String getInstrument() {
        return m_instrument;
    }

    public void setInstrument(String instrument) {
        m_instrument = instrument;
    }

    public String getRegCallId() {
        return m_regCallId;
    }

    public void setRegCallId(String callId) {
        m_regCallId = callId;
    }

    public String getIdentity() {
        return m_identity;
    }

    public void setIdentity(String identity) {
        m_identity = identity;
    }

    public int compareTo(Object other) {
        RegistrationItem riOther = (RegistrationItem) other;
        return new CompareToBuilder().append(getExpires(), riOther.getExpires()).toComparison();
    }

    public long timeToExpireAsSeconds(long nowSeconds) {
        return getExpires().getTime() / 1000 - nowSeconds;
    }
}

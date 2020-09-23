package org.sipfoundry.sipxconfig.api.model;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Strings")
public class StringList {
    private Collection<String> m_strings;

    public Collection<String> getStrings() {
        return m_strings;
    }

    public void setStrings(Collection<String> strings) {
        m_strings = strings;
    }
}

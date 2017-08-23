package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({
        "Id", "Created"
})

public class ContainerBean {
    private String m_Id;
    private String m_Created;

    public String getId() {
        return m_Id;
    }

    public void setId(String Id) {
        m_Id = Id;
    }

    public String getCreated() {
        return m_Created;
    }

    public void setCreated(String Created) {
        m_Created = Created;
    }
}

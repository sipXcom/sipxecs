package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
@JsonPropertyOrder({
        "Id", "Created", "State"
})

public class ContainerBean {
    private String m_Id;
    private String m_Created;
    private ContainerStateBean m_State;

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

    public ContainerStateBean getState() {
        return m_State;
    }

    public void setState(ContainerStateBean State) {
        m_State = State;
    }
}

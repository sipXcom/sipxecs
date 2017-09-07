package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@JsonPropertyOrder({
        "Id", "Created", "Name", "State"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerBean {
    private String m_id;
    private String m_created;
    private String m_name;
    private ContainerStateBean m_state;

    @JsonProperty("Id")
    public String getId() {
        return m_id;
    }

    @JsonProperty("Id")
    public void setId(String id) {
        m_id = id;
    }

    @JsonProperty("Created")
    public String getCreated() {
        return m_created;
    }

    @JsonProperty("Created")
    public void setCreated(String created) {
        m_created = created;
    }        

    @JsonProperty("Name")
    public String getName() {
        return m_name;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        m_name = name;
    }

    @JsonProperty("State")
    public ContainerStateBean getState() {
        return m_state;
    }

    @JsonProperty("State")
    public void setState(ContainerStateBean state) {
        m_state = state;
    }
}

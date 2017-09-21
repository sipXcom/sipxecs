package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({
    "Hostname"
})
@JsonIgnoreProperties(ignoreUnknown = true)

public class ContainerConfigBean {
    
    private String m_hostname;

    @JsonProperty("Hostname")
    public String getHostname() {
        return m_hostname;
    }

    @JsonProperty("Hostname")
    public void setHostname(String hostname) {
        m_hostname = hostname;
    }
}

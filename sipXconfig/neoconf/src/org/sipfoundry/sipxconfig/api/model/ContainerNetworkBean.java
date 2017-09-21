package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({
    "IPAddress"
})

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerNetworkBean {

    private String ipAddress;

    @JsonProperty("IPAddress")
    public String getIpAddress() {
        return ipAddress;
    }

    @JsonProperty("IPAddress")
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}

package org.sipfoundry.sipxconfig.api.model;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({
    "Bridge", "SandboxID", "HairpinMode", "LinkLocalIPv6Address", "LinkLocalIPv6PrefixLen", "Networks"
})

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerNetworkSettingsBean {
    private String m_bridge;
    private String m_sandboxId;
    private String m_hairpinMode;
    private String m_linkLocalIPv6Address;
    private String m_linkLocalIPv6PrefixLen;
    private Map<String, ContainerNetworkBean> m_networks;
    
    @JsonProperty("Bridge")
    public String getBridge() {
        return m_bridge;
    }
    
    @JsonProperty("Bridge")
    public void setBridge(String bridge) {
        m_bridge = bridge;
    }
    
    @JsonProperty("SandboxID")
    public String getSandboxId() {
        return m_sandboxId;
    }
    
    @JsonProperty("SandboxID")
    public void setSandboxId(String sandboxId) {
        m_sandboxId = sandboxId;
    }
    
    @JsonProperty("HairpinMode")
    public String getHairpinMode() {
        return m_hairpinMode;
    }
    
    @JsonProperty("HairpinMode")
    public void setHairpinMode(String hairpinMode) {
        m_hairpinMode = hairpinMode;
    }
    
    @JsonProperty("LinkLocalIPv6Address")
    public String getLinkLocalIPv6Address() {
        return m_linkLocalIPv6Address;
    }
    
    @JsonProperty("LinkLocalIPv6Address")
    public void setLinkLocalIPv6Address(String linkLocalIPv6Address) {
        m_linkLocalIPv6Address = linkLocalIPv6Address;
    }
    
    @JsonProperty("LinkLocalIPv6PrefixLen")
    public String getLinkLocalIPv6PrefixLen() {
        return m_linkLocalIPv6PrefixLen;
    }
    
    @JsonProperty("LinkLocalIPv6PrefixLen")
    public void setLinkLocalIPv6PrefixLen(String linkLocalIPv6PrefixLen) {
        m_linkLocalIPv6PrefixLen = linkLocalIPv6PrefixLen;
    }
    
    @JsonProperty("Networks")
    public Map<String, ContainerNetworkBean> getNetworks() {
        return m_networks;
    }
    
    @JsonProperty("Networks")
    public void setNetworks(Map<String, ContainerNetworkBean> networks) {
        m_networks = networks;
    }
        
}

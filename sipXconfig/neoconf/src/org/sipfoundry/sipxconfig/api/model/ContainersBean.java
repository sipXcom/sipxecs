package org.sipfoundry.sipxconfig.api.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({
    "Id", "Names", "Image", "State", "Status"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainersBean {
    private String m_id;
    private List<String> m_names;
    private String m_image;
    private String m_state;
    private String m_status;
    
    @JsonProperty("Id")
    public String getId() {
        return m_id;
    }
    
    @JsonProperty("Id")
    public void setId(String id) {
        m_id = id;
    }
    
    @JsonProperty("Names")
    public List<String> getNames() {
        return m_names;
    }
    
    @JsonProperty("Names")
    public void setNames(List<String> names) {
        m_names = names;
    }
    
    @JsonProperty("Image")
    public String getImage() {
        return m_image;
    }
    
    @JsonProperty("Image")
    public void setImage(String image) {
        m_image = image;
    }
    
    @JsonProperty("State")
    public String getState() {
        return m_state;
    }
    
    @JsonProperty("State")
    public void setState(String state) {
        m_state = state;
    }
    
    @JsonProperty("Status")
    public String getStatus() {
        return m_status;
    }
    
    @JsonProperty("Status")
    public void setStatus(String status) {
        m_status = status;
    }        
}

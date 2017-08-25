package org.sipfoundry.sipxconfig.api.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;





import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.util.Date;

@JsonPropertyOrder({
        "Status", "Running", "Paused", "Restarting", "OOMKilled", "Dead", "Pid", "ExitCode", "Error", "StartedAt", "FinishedAt"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerStateBean {
    private String m_status;
    private Boolean m_running;
    private Boolean m_paused;
    private Boolean m_restarting;
    private Boolean m_oOMKilled;
    private Boolean m_dead;
    private Integer m_pid;
    private Integer m_exitCode;
    private String m_error;
    private Date m_startedAt;
    private Date m_finishedAt;

    @JsonProperty("Status")
    public String getStatus() {
        return m_status;
    }
    
    @JsonProperty("Status")
    public void setStatus(String status) {
        m_status = status;
    }

    @JsonProperty("Running")
    public Boolean getRunning() {
        return m_running;
    }
    
    @JsonProperty("Running")
    public void setRunning(Boolean running) {
        m_running = running;
    }

    @JsonProperty("Paused")
    public Boolean getPaused() {
        return m_paused;
    }

    @JsonProperty("Paused")
    public void setPaused(Boolean paused) {
        m_paused = paused;
    }

    @JsonProperty("Restarting")
    public Boolean getRestarting() {
        return m_restarting;
    }

    @JsonProperty("Restarting")
    public void setRestarting(Boolean restarting) {
        m_restarting = restarting;
    }

    @JsonProperty("OOMKilled")
    public Boolean getOOMKilled() {
        return m_oOMKilled;
    }

    @JsonProperty("OOMKilled")
    public void setOOMKilled(Boolean oOMKilled) {
        m_oOMKilled = oOMKilled;
    }

    @JsonProperty("Dead")
    public Boolean getDead() {
        return m_dead;
    }

    @JsonProperty("Dead")
    public void setDead(Boolean dead) {
        m_dead = dead;
    }

    @JsonProperty("Pid")
    public Integer getPid() {
        return m_pid;
    }

    @JsonProperty("Pid")
    public void setPid(Integer pid) {
        m_pid = pid;
    }

    @JsonProperty("ExitCode")
    public Integer getExitCode() {
        return m_exitCode;
    }

    @JsonProperty("ExitCode")
    public void setExitCode(Integer exitCode) {
        m_exitCode = exitCode;
    }

    @JsonProperty("Error")
    public String getError() {
        return m_error;
    }

    @JsonProperty("Error")
    public void setError(String error) {
        m_error = error;
    }

    @JsonProperty("StartedAt")
    public Date getStartedAt() {
        return m_startedAt;
    }

    @JsonProperty("StartedAt")
    public void setStartedAt(Date startedAt) {
        m_startedAt = startedAt;
    }

    @JsonProperty("FinishedAt")
    public Date getFinishedAt() {
        return m_finishedAt;
    }

    @JsonProperty("FinishedAt")
    public void setFinishedAt(Date finishedAt) {
        m_finishedAt = finishedAt;
    }
}

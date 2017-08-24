package org.sipfoundry.sipxconfig.api.model;

import java.util.Date;

public class ContainerStateBean {
    private String m_Status;
    private Boolean m_Running;
    private Boolean m_Paused;
    private Boolean m_Restarting;
    private Boolean m_OOMKilled;
    private Boolean m_Dead;
    private Integer m_Pid;
    private Integer m_ExitCode;
    private String m_Error;
    private Date m_StartedAt;
    private Date m_FinishedAt;

    public String getStatus() {
        return m_Status;
    }

    public void setStatus(String Status) {
        m_Status = Status;
    }

    public Boolean getRunning() {
        return m_Running;
    }

    public void setRunning(Boolean Running) {
        m_Running = Running;
    }

    public Boolean getPaused() {
        return m_Paused;
    }

    public void setPaused(Boolean Paused) {
        m_Paused = m_Paused;
    }

    public Boolean getRestarting() {
        return m_Restarting;
    }

    public void setRestarting(Boolean Restarting) {
        m_Restarting = Restarting;
    }

    public Boolean getOOMKilled() {
        return m_OOMKilled;
    }

    public void setOOMKilled(Boolean OOMKilled) {
        m_OOMKilled = OOMKilled;
    }

    public Boolean getDead() {
        return m_Dead;
    }

    public void setDead(Boolean Dead) {
        m_Dead = Dead;
    }

    public Integer getPid() {
        return m_Pid;
    }

    public void setPid(Integer Pid) {
        m_Pid = Pid;
    }

    public Integer getExitCode() {
        return m_ExitCode;
    }

    public void setExitCode(Integer ExitCode) {
        m_ExitCode = ExitCode;
    }

    public String getError() {
        return m_Error;
    }

    public void setError(String Error) {
        m_Error = Error;
    }

    public Date getStartedAt() {
        return m_StartedAt;
    }

    public void setStartedAt(Date StartedAt) {
        m_StartedAt = StartedAt;
    }

    public Date getFinishedAt() {
        return m_FinishedAt;
    }

    public void setFinishedAt(Date FinishedAt) {
        m_FinishedAt = FinishedAt;
    }
}

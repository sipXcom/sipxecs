package org.sipfoundry.sipxconfig.api.model;

public class AutoAttendantSettingsBean {
	private String m_dtmfInterTimeout;
	private String m_dtmfOverallTimeout;
	private String m_maxDtmfNumberTones;
	private String m_replayCount;
	private String m_invalidResponseCount;
	private boolean m_transferOnFailures;
	private String m_transferExtension;
	private String m_promptToPlay;
	private boolean m_playPromptWhenTransfer;
	
	public String getDtmfInterTimeout() {
		return m_dtmfInterTimeout;
	}
	public void setDtmfInterTimeout(String dtmfInterTimeout) {
		m_dtmfInterTimeout = dtmfInterTimeout;
	}
	public String getDtmfOverallTimeout() {
		return m_dtmfOverallTimeout;
	}
	public void setDtmfOverallTimeout(String dtmfOverallTimeout) {
		m_dtmfOverallTimeout = dtmfOverallTimeout;
	}
	public String getMaxDtmfNumberTones() {
		return m_maxDtmfNumberTones;
	}
	public void setMaxDtmfNumberTones(String maxDtmfNumberTones) {
		m_maxDtmfNumberTones = maxDtmfNumberTones;
	}
	public String getReplayCount() {
		return m_replayCount;
	}
	public void setReplayCount(String replayCount) {
		m_replayCount = replayCount;
	}
	public String getInvalidResponseCount() {
		return m_invalidResponseCount;
	}
	public void setInvalidResponseCount(String invalidResponseCount) {
		m_invalidResponseCount = invalidResponseCount;
	}
	public boolean isTransferOnFailures() {
		return m_transferOnFailures;
	}
	public void setTransferOnFailures(boolean transferOnFailures) {
		m_transferOnFailures = transferOnFailures;
	}
	public String getTransferExtension() {
		return m_transferExtension;
	}
	public void setTransferExtension(String transferExtension) {
		m_transferExtension = transferExtension;
	}
	public String getPromptToPlay() {
		return m_promptToPlay;
	}
	public void setPromptToPlay(String promptToPlay) {
		m_promptToPlay = promptToPlay;
	}
	public boolean isPlayPromptWhenTransfer() {
		return m_playPromptWhenTransfer;
	}
	public void setPlayPromptWhenTransfer(boolean playPromptWhenTransfer) {
		m_playPromptWhenTransfer = playPromptWhenTransfer;
	}
}

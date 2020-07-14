package org.sipfoundry.commons.extendedcdr;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

public class VideoStatistics {
	@Field("TimeStampUtc")
	@JsonProperty("TimeStampUtc")
	private String m_timeStampUtc;	
	
	@Field("PacketsSent")
	@JsonProperty("PacketsSent")
	private int m_packetsSent;
	
	@Field("PacketsReceived")
	@JsonProperty("PacketsReceived")
	private int m_packetsReceived;
	
	@Field("CumulativeLostPackets")
	@JsonProperty("CumulativeLostPackets")
	private int m_cumulativeLostPackets;
	
	@Field("RttMs")
	@JsonProperty("RttMs")
	private int m_rttMs;
	
	public String getTimeStampUtc() {
		return m_timeStampUtc;
	}
	public void setTimeStampUtc(String timeStampUtc) {
		m_timeStampUtc = timeStampUtc;
	}
	public int getPacketsSent() {
		return m_packetsSent;
	}
	public void setPacketsSent(int packetsSent) {
		m_packetsSent = packetsSent;
	}
	public int getPacketsReceived() {
		return m_packetsReceived;
	}
	public void setPacketsReceived(int packetsReceived) {
		m_packetsReceived = packetsReceived;
	}
	public int getCumulativeLostPackets() {
		return m_cumulativeLostPackets;
	}
	public void setCumulativeLostPackets(int cumulativeLostPackets) {
		m_cumulativeLostPackets = cumulativeLostPackets;
	}
	public int getRttMs() {
		return m_rttMs;
	}
	public void setRttMs(int rttMs) {
		m_rttMs = rttMs;
	}
}

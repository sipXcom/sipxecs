package org.sipfoundry.commons.extendedcdr;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

public class VideoStatistics implements Comparable {
	@Field("TimeStampUtc")	
	private String m_timeStampUtc;	
	
	@Field("PacketsSent")	
	private int m_packetsSent;
	
	@Field("PacketsReceived")	
	private int m_packetsReceived;
	
	@Field("CumulativeLostPackets")
	private int m_cumulativeLostPackets;
	
	@Field("RttMs")	
	private int m_rttMs;
	
	@JsonProperty("TimeStampUtc")
	public String getTimeStampUtc() {
		return m_timeStampUtc;
	}
	public void setTimeStampUtc(String timeStampUtc) {
		m_timeStampUtc = timeStampUtc;
	}
	
	@JsonProperty("PacketsSent")
	public int getPacketsSent() {
		return m_packetsSent;
	}
	public void setPacketsSent(int packetsSent) {
		m_packetsSent = packetsSent;
	}
	
	@JsonProperty("PacketsReceived")
	public int getPacketsReceived() {
		return m_packetsReceived;
	}
	public void setPacketsReceived(int packetsReceived) {
		m_packetsReceived = packetsReceived;
	}
	
	@JsonProperty("CumulativeLostPackets")
	public int getCumulativeLostPackets() {
		return m_cumulativeLostPackets;
	}
	public void setCumulativeLostPackets(int cumulativeLostPackets) {
		m_cumulativeLostPackets = cumulativeLostPackets;
	}
	
	@JsonProperty("RttMs")
	public int getRttMs() {
		return m_rttMs;
	}
	public void setRttMs(int rttMs) {
		m_rttMs = rttMs;
	}
	
	@Override
	public int compareTo(Object arg0) {		
		return m_timeStampUtc.compareTo((String)arg0);
	}	
}

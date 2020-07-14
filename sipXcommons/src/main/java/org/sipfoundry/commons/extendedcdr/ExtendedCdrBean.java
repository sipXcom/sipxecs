package org.sipfoundry.commons.extendedcdr;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "userProfile")
@XmlRootElement(name = "CallStatisticsData")
@CompoundIndexes({
    @CompoundIndex(name = "callStatistic_idx", def = "{'m_callId': 1, 'm_caller': 1}")
})

public class ExtendedCdrBean {
    
    @Id
    private String id;
    
    @Field("CallID")    
    @Indexed
    @JsonProperty("CallID")
	private String m_callId;
        
    @Field("ApplicationReferenceID")
    @JsonProperty("ApplicationReferenceID")
    private String m_applicationReferenceID;
    
    @Field("CallerYN")
    @JsonProperty("CallerYN")
    private String m_caller;

    @Field("AudioStatistics")
    @JsonProperty("AudioStatistics")
    private Set<AudioStatistics> m_audioStatistics;
    
    @Field("VideoStatistics")
    @JsonProperty("VideoStatistics")
    private Set<VideoStatistics> m_videoStatistics;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCallId() {
		return m_callId;
	}

	public void setCallId(String callId) {
		m_callId = callId;
	}
		
	public String getApplicationReferenceID() {
		return m_applicationReferenceID;
	}

	public void setApplicationReferenceID(String applicationReferenceID) {
		m_applicationReferenceID = applicationReferenceID;
	}

	public String getCaller() {
		return m_caller;
	}

	public void setCaller(String caller) {
		m_caller = caller;
	}

	public Set<AudioStatistics> getAudioStatistics() {
		return m_audioStatistics;
	}

	public void setAudioStatistics(Set<AudioStatistics> audioStatistics) {
		m_audioStatistics = audioStatistics;
	}

	public Set<VideoStatistics> getVideoStatistics() {
		return m_videoStatistics;
	}

	public void setVideoStatistics(Set<VideoStatistics> videoStatistics) {
		m_videoStatistics = videoStatistics;
	}
}

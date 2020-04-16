package org.sipfoundry.commons.extendedcdr;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "userProfile")
@XmlRootElement(name = "ExtendedCdr")
public class ExtendedCdrBean {
    
    @Id
    private String id;
    
    @Field("callId")
    @Indexed(unique = true)
	private String callId;
    
    @Field("mos")
    private int mos;
	
    @Field("jitter")
	private long jitter;
    
    @Field("latency")
    private long latency;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}
		
	public long getJitter() {
		return jitter;
	}

	public void setJitter(long jitter) {
		this.jitter = jitter;
	}

	public int getMos() {
		return mos;
	}

	public void setMos(int mos) {
		this.mos = mos;
	}

	public long getLatency() {
		return latency;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}	
}

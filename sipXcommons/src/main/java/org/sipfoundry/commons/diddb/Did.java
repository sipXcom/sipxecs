package org.sipfoundry.commons.diddb;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "entity")
public class Did implements Serializable {

    @Id
    private String id;
    
    @Field("type")
    private String type;
    
    @Indexed(unique = true)
    @Field("typeId")
    private String typeId;    
    
    @Field("value")
    private String value;
    
    @Field("pool_id")
    private String poolId;    
    
    public Did() {
    	
    }
    
    public Did(String type, String typeId, String value, String poolId) {
        this.type = type;
        this.typeId = typeId;
        this.value = value;
        this.poolId = poolId;
    }
            
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }
    
    @Override
    public boolean equals(Object o) {    
        if (o == this) { 
            return true; 
        } 
  
        if (!(o instanceof Did)) { 
            return false; 
        } 
        
        Did did = (Did) o;           
        return StringUtils.equals(this.getValue(), did.getValue());  
    }
    
    @Override
    public String toString() {
        return value;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }
}
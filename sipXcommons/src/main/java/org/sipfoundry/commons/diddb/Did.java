package org.sipfoundry.commons.diddb;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "entity")
public class Did {

    @Id
    private String id;
    
    @Field("type")
    private String type;
    
    @Indexed(unique = true)
    @Field("typeId")
    private String typeId;
    
    @Field("name")
    private String name;
    
    @Field("value")
    private String value;

    public Did() {
        super();
    }

    public Did(String type, String typeId, String name, String value) {
        super();
        this.type = type;
        this.typeId = typeId;
        this.name = name;
        this.value = value;        
    }
            
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
}
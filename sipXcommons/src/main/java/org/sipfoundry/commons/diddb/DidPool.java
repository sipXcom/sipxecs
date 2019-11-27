package org.sipfoundry.commons.diddb;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "userProfile")
public class DidPool implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;   
    
    @Field("name")
    private String name;
    
    @Field("start")
    private String start;
    
    @Field("end")
    private String end;
    
    @Field("next")
    private String next;
    
    @Field("description")
    private String description;
    
    public DidPool() {
    	
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
       
    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public DidPool(String name, String start, String end) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.next = start;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }        
}

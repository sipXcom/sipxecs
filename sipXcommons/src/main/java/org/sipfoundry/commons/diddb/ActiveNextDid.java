package org.sipfoundry.commons.diddb;

import org.springframework.data.mongodb.core.mapping.Field;

public class ActiveNextDid extends Did {
    
    @Field("activeNext")
    private boolean activeNext = true;
    
    public ActiveNextDid() {
    	
    }
    
    public ActiveNextDid(String type, String typeId, String value, String poolId) {
        super(type, typeId, value, poolId);
    }

    public boolean getActiveNext() {
        return activeNext;
    }

    public void setActiveNext(boolean activeNext) {
        this.activeNext = activeNext;
    }
}

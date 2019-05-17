package org.sipfoundry.commons.diddb;

import java.util.List;

public interface DidService {

    public static final String CONTEXT_BEAN_NAME = "didService";
    
    public static final String TYPE_USER = "USER";
    public static final String TYPE_HUNT_GROUP = "HUNT_GROUP";
    public static final String TYPE_LIVE_AUTO_ATTENDANT = "LIVE_AUTO_ATTENDANT";
    public static final String TYPE_AUTO_ATTENDANT_DIALING_RULE = "AUTO_ATTENDANT_DIALING_RULE";
    public static final String TYPE_VOICEMAIL_DIALING_RULE = "VOICEMAIL_DIALING_RULE";
        
    public Did getDid(String typeId);    

    public void saveDid(Did did);
    
    public void removeDid(String typeId);
    
    public List<Did> getAllDids();
    
    /**
     * get all saved dids except the did with the typeId parameter
     * @param typeId - the typeId did that needs to be filtered
     * @return
     */
    public List<Did> getDidsExceptOne(String typeId);
    
    public boolean isDidInUse(String typeId, String value);
    
    public boolean isDidInUse(String value);
    
    public List<Did> getDidsInUse(List<String> values);
    
    public boolean areDidsInUse(List<String> values);
}

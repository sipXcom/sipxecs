package org.sipfoundry.commons.diddb;

import java.util.List;

public interface DidService {

    public static final String CONTEXT_BEAN_NAME = "didService";

    public Did getDid(String typeId);
    
    public Did getActiveNextDid();

    public void saveDid(Did did);
    
    public void removeAllDids();
    
    public void insertDids(List<Did> dids);
    
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
    
    public List<Did> getDidsInUse(String typeId, List<String> values);
    
    public boolean areDidsInUse(String typeId, List<String> values);
    
    public DidPool getDidPool(String typeId);
}

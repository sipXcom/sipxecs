package org.sipfoundry.commons.diddb;

import java.util.List;

public interface DidPoolService {
    
    public DidPool getDidPool(String typeId);

    public DidPool getDidPoolById(String poolId);

    public void saveDidPool(DidPool didPool);
    
    public void removeAllDidPools();
    
    public void insertDidPools(List<DidPool> didPools);
    
    public void removeDidPool(DidPool didPool);
    
    public List<DidPool> getAllDidPools();
    
    public Long findNext(DidPool pool);
    
    public List<String> buildNextDids();

}

package org.sipfoundry.commons.diddb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class DidPoolServiceImpl implements DidPoolService {
    
    private MongoTemplate m_profiles;
    private DidService m_didService;

    @Override
    public DidPool getDidPool(String type) {
        DidPool did = m_profiles.findOne(
            new Query(Criteria.where("type").is(type)), DidPool.class);
        return did;
    }

    @Override
    public DidPool getDidPoolById(String poolId) {
        DidPool did = m_profiles.findOne(
            new Query(Criteria.where("_id").is(poolId)), DidPool.class);
        return did;
    }

    @Override
    public void saveDidPool(DidPool didPool) {
        m_profiles.save(didPool);
    }

    @Override
    public void removeAllDidPools() {
        m_profiles.remove(
            new Query(Criteria.where("_class").is("org.sipfoundry.commons.diddb.DidPool")), DidPool.class);
    }

    @Override
    public void insertDidPools(List<DidPool> didPools) {
        m_profiles.insertAll(didPools);        
    }

    @Override
    public void removeDidPool(DidPool didPool) {
        if (didPool != null) {
            m_profiles.remove(didPool);            
        }                
    }

    @Override
    public List<DidPool> getAllDidPools() {
        return m_profiles.find(
            new Query(Criteria.where("_class").is("org.sipfoundry.commons.diddb.DidPool")), DidPool.class);
    }
    
    @Required
    public void setProfiles(MongoTemplate profiles) {
        m_profiles = profiles;
    }

    @Override
    public Long findNext(DidPool pool) {
        List<Did> dids = m_didService.getAllDids();
        Long first = Long.parseLong(pool.getStart().replaceAll("[^\\d.]", ""));
        Long last = Long.parseLong(pool.getEnd().replaceAll("[^\\d.]", ""));
        List<Long> poolDids = new ArrayList<Long>();
        for (long i = first; i <= last; i++) {
            poolDids.add(i);
        }

        for (Did did : dids) {
            Long didValue = Long.parseLong(did.getValue().replaceAll("[^\\d.]", ""));
            if (poolDids.contains(didValue)) {
                poolDids.remove(didValue);
            }
        }
        return poolDids.size() > 0 ? poolDids.get(0) : null;
    }

    @Required
    public void setDidService(DidService didService) {
        m_didService = didService;
    }

    @Override
    public List<String> buildNextDids() {
        List<String> candidates = new ArrayList<String>();
        List<DidPool> pools = getAllDidPools();
        for (DidPool pool : pools) {
            String next = pool.getNext();
            if (StringUtils.isNotEmpty(next)) { 
                candidates.add(pool.getNext());
            }
        }
        return candidates;
    }
    
    @Override
    public boolean outsideRangeDidValue(DidPool pool, long value) {
        return Long.parseLong(pool.getStart().replaceAll("[^\\d.]", "")) > value ||
            Long.parseLong(pool.getEnd().replaceAll("[^\\d.]", "")) < value;
    }
}

package org.sipfoundry.commons.diddb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class DidPoolServiceImpl implements DidPoolService {
    
    private MongoTemplate m_imdb;
    private DidService m_didService;

    @Override
    public DidPool getDidPool(String type) {
        DidPool did = m_imdb.findOne(
            new Query(Criteria.where("type").is(type)), DidPool.class);
        return did;
    }

    @Override
    public void saveDidPool(DidPool didPool) {
        m_imdb.save(didPool);
    }

    @Override
    public void removeAllDidPools() {
        m_imdb.remove(
            new Query(Criteria.where("_class").is("org.sipfoundry.commons.diddb.DidPool")), DidPool.class);
    }

    @Override
    public void insertDidPools(List<DidPool> didPools) {
        m_imdb.insertAll(didPools);        
    }

    @Override
    public void removeDidPool(DidPool didPool) {
        if (didPool != null) {
            m_imdb.remove(didPool);            
        }                
    }

    @Override
    public List<DidPool> getAllDidPools() {
        return m_imdb.find(
            new Query(Criteria.where("_class").is("org.sipfoundry.commons.diddb.DidPool")), DidPool.class);
    }
    
    @Required
    public void setImdb(MongoTemplate imdb) {
        m_imdb = imdb;
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

}

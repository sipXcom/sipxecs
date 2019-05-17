package org.sipfoundry.commons.diddb;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class DidServiceImpl implements DidService {
    private MongoTemplate m_imdb;

    @Override
    public Did getDid(String typeId) {
        Did did = m_imdb.findOne(
            new Query(Criteria.where("typeId").is(typeId)), Did.class);
        return did;
    }

    @Override
    public void saveDid(Did did) {
        m_imdb.save(did);
    }
    
    @Required
    public void setImdb(MongoTemplate imdb) {
        m_imdb = imdb;
    }

    @Override
    public void removeDid(String typeId) {
        Did did = getDid(typeId);
        if (did != null) {
            m_imdb.remove(did);            
        }        
    }

    @Override
    public List<Did> getAllDids() {
        return m_imdb.findAll(Did.class);        
    }

    @Override
    public List<Did> getDidsExceptOne(String typeId) {
        return m_imdb.find(
            new Query(Criteria.where("typeId").ne(typeId)), Did.class);        
    }

    @Override
    public boolean isDidInUse(String typeId, String value) {
        return !m_imdb.find(
            new Query(Criteria.where("typeId").ne(typeId).and("value").is(value)), Did.class).isEmpty();        
    }
    
    @Override
    public boolean isDidInUse(String value) {
        return !m_imdb.find(
            new Query(Criteria.where("value").is(value)), Did.class).isEmpty();
    }
    
    @Override
    public boolean areDidsInUse(List<String> values) {
        return getDidsInUse(values).isEmpty();
    }
    
    @Override
    public List<Did> getDidsInUse(List<String> values) {
        return m_imdb.find(
            new Query(Criteria.where("value").in(values)), Did.class);
    }    
}

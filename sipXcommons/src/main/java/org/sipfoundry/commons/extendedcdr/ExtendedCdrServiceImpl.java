package org.sipfoundry.commons.extendedcdr;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class ExtendedCdrServiceImpl implements ExtendedCdrService {
    private MongoTemplate m_profiles;

    @Override
    public void saveExtendedCdr(ExtendedCdrBean extCdrBean) {
        m_profiles.save(extCdrBean);
    }
        
    @Required
	public void setProfiles(MongoTemplate profiles) {
		m_profiles = profiles;
	}

	@Override
	public ExtendedCdrBean getExtendedCdr(String callId) {
		ExtendedCdrBean extendedCdr = m_profiles.findOne(
                new Query(Criteria.where("callId").is(callId)), ExtendedCdrBean.class);
        return extendedCdr;		
	}
	
	@Override
	public List<ExtendedCdrBean> getExtendedCdrs(List<String> callId) {
		List<ExtendedCdrBean> extendedCdrs = m_profiles.find(
                new Query(Criteria.where("callId").in(callId)), ExtendedCdrBean.class);
        return extendedCdrs;		
	}  	
}

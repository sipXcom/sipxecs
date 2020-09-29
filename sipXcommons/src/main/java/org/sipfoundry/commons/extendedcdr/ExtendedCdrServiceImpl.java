package org.sipfoundry.commons.extendedcdr;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
/**
 * Extended cdr service
 * @author mirceac
 *
 */
public class ExtendedCdrServiceImpl implements ExtendedCdrService {
    private MongoTemplate m_profiles;
    private static final Logger log = Logger.getLogger(ExtendedCdrServiceImpl.class);

    @Override
    public void saveExtendedCdr(ExtendedCdrBean extCdrBean) {
    	ExtendedCdrBean extendedCdr = getExtendedCdrByCaller(extCdrBean.getCallId(), extCdrBean.getCaller());
    	if(extendedCdr != null) {
    		extendedCdr.setCaller(extCdrBean.getCaller());
    		extendedCdr.setCallId(extCdrBean.getCallId());
    		extendedCdr.setIp(extCdrBean.getIp());
    		extendedCdr.setApplicationReferenceID(extCdrBean.getApplicationReferenceID());
    		extendedCdr.setAudioStatistics(extCdrBean.getAudioStatistics());
    		extendedCdr.setVideoStatistics(extCdrBean.getVideoStatistics());
    		m_profiles.save(extendedCdr);
    	} else {
    		m_profiles.save(extCdrBean);
    	}
    }
        
    @Required
	public void setProfiles(MongoTemplate profiles) {
		m_profiles = profiles;
	}

	
	@Override
	public ExtendedCdrBean getExtendedCdrByCaller(String callId, String caller) {
		ExtendedCdrBean extendedCdr = null;
		try {
			extendedCdr = m_profiles.findOne(
                new Query(Criteria.where("CallID").is(callId).and("CallerYN").is(caller)), ExtendedCdrBean.class);
		} catch (Exception ex) {
			log.error("Exception ", ex);
		}
        return extendedCdr;		
	}
	
	@Override
	public List<ExtendedCdrBean> getExtendedCdrs(List<String> callId) {
		List<ExtendedCdrBean> extendedCdrs = m_profiles.find(
                new Query(Criteria.where("CallID").in(callId)), ExtendedCdrBean.class);
        return extendedCdrs;		
	}  	
}

package org.sipfoundry.sipxconfig.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.callgroup.AbstractRing;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;
import org.sipfoundry.sipxconfig.callgroup.UserRing;

@XmlRootElement(name = "CallGroup")
public class CallGroupBean {
    private static final Log LOG = LogFactory.getLog(CallGroupBean.class);
    private boolean m_enabled;
    private String m_name;
    private String m_extension;
    private String m_did;
    private String m_description;
    private String m_fallbackDestination;
    private boolean m_voicemailFallback = true;
    private boolean m_userForward = true;
    private boolean m_useFwdTimers;    
    private List<RingBean> m_ringBeans = new ArrayList<RingBean>();
    
    public static CallGroupBean convertCallGroup(CallGroup callGroup) throws Exception{
        if (callGroup == null) {
            return null;
        }
        
        CallGroupBean bean = new CallGroupBean();
        BeanUtils.copyProperties(bean, callGroup);
        List<AbstractRing> rings = callGroup.getRings();
        for (AbstractRing ring : rings) {
            UserRing userRing = (UserRing) ring;
            RingBean ringBean = new RingBean();
            ringBean.setEnabled(userRing.isEnabled());
            ringBean.setExpiration(userRing.getExpiration());
            if (!userRing.isFirst()) {
            	ringBean.setTypeStr(userRing.getType() == null ? UserRing.Type.DELAYED.getName() : userRing.getType().getName());
            }
            ringBean.setUserName(userRing.getUser().getName());
            bean.insertRingBean(ringBean);
        }
        return bean;
        
    }

    
	public List<RingBean> getRingBeans() {
		return m_ringBeans;
	}
    
	public void setRingBeans(List<RingBean> rings) {
		m_ringBeans = rings;
	}
    
    public void insertRingBean(RingBean ringBean) {
    	m_ringBeans.add(ringBean);
    }


	public boolean isEnabled() {
		return m_enabled;
	}


	public void setEnabled(boolean enabled) {
		m_enabled = enabled;
	}


	public String getName() {
		return m_name;
	}


	public void setName(String name) {
		m_name = name;
	}


	public String getExtension() {
		return m_extension;
	}


	public void setExtension(String extension) {
		m_extension = extension;
	}


	public String getDid() {
		return m_did;
	}


	public void setDid(String did) {
		m_did = did;
	}


	public String getDescription() {
		return m_description;
	}


	public void setDescription(String description) {
		m_description = description;
	}


	public String getFallbackDestination() {
		return m_fallbackDestination;
	}


	public void setFallbackDestination(String fallbackDestination) {
		m_fallbackDestination = fallbackDestination;
	}


	public boolean isVoicemailFallback() {
		return m_voicemailFallback;
	}


	public void setVoicemailFallback(boolean voicemailFallback) {
		m_voicemailFallback = voicemailFallback;
	}


	public boolean isUserForward() {
		return m_userForward;
	}


	public void setUserForward(boolean userForward) {
		m_userForward = userForward;
	}

	public boolean isUseFwdTimers() {
		return m_useFwdTimers;
	}


	public void setUseFwdTimers(boolean useFwdTimers) {
		m_useFwdTimers = useFwdTimers;
	}
}

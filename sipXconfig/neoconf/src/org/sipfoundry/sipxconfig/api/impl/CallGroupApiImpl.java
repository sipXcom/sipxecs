package org.sipfoundry.sipxconfig.api.impl;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.api.CallGroupApi;
import org.sipfoundry.sipxconfig.api.model.CallGroupBean;
import org.sipfoundry.sipxconfig.api.model.CallGroupList;
import org.sipfoundry.sipxconfig.api.model.RingBean;
import org.sipfoundry.sipxconfig.callgroup.AbstractRing;
import org.sipfoundry.sipxconfig.callgroup.AbstractRing.Type;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;
import org.sipfoundry.sipxconfig.callgroup.CallGroupContext;
import org.sipfoundry.sipxconfig.callgroup.UserRing;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.springframework.beans.factory.annotation.Required;

public class CallGroupApiImpl implements CallGroupApi {
	
	private static final Log LOG = LogFactory.getLog(CallGroupApiImpl.class);
	
	private CallGroupContext m_context;
	private CoreContext m_coreContext;
	
	@Override
	public Response getCallGroups() {
        List<CallGroup> callGroups = m_context.getCallGroups();
        if (callGroups != null) {
        	try {
        		return Response.ok().entity(CallGroupList.convertCallGroupList(callGroups)).build();
        	} catch (Exception ex) {
        		LOG.error("Exception building callgroup response ", ex);
        	}
        }
        return Response.status(Status.NOT_FOUND).build();
	}

	@Required
	public void setContext(CallGroupContext context) {
		m_context = context;
	}

	@Override
	public Response newCallGroup(CallGroupBean callGroupBean) {
		CallGroup callGroup = new CallGroup();
		convertToCallGroup(callGroupBean, callGroup);
		m_context.saveCallGroup(callGroup);
		return Response.ok().entity(callGroup.getId()).build();
	}
	
	public void convertToCallGroup(CallGroupBean callGroupBean, CallGroup callGroup) {        
        try {
            BeanUtils.copyProperties(callGroup, callGroupBean);
            callGroup.clear();            
            List<RingBean> rings = callGroupBean.getRingBeans();
            for (RingBean ring : rings) {
            	UserRing userRing = callGroup.insertRingForUser(m_coreContext.loadUserByUserName(ring.getUserName()));
            	userRing.setEnabled(ring.isEnabled());
            	userRing.setExpiration(ring.getExpiration());
            	userRing.setType(AbstractRing.Type.getEnum(ring.getTypeStr()));            	
            }
        } catch (Exception e) {
            LOG.error("Cannot marshal properties");
        }
	}

	@Required
	public void setCoreContext(CoreContext coreContext) {
		m_coreContext = coreContext;
	}

	@Override
	public Response updateCallGroup(String callGroupExtension, CallGroupBean callGroupBean) {
		int id = m_context.getCallGroupId(callGroupExtension);
		CallGroup callGroup = m_context.loadCallGroup(id);
		if (callGroup != null) {
			convertToCallGroup(callGroupBean, callGroup);
			m_context.saveCallGroup(callGroup);
			return Response.ok().entity(callGroup.getId()).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@Override
	public Response deleteCallGroup(String callGroupExtension) {
		m_context.removeCallGroupByAlias(callGroupExtension);
		return Response.ok().build();
	}

	@Override
	public Response getCallGroup(String callGroupExtension) {
        int callGroupId = m_context.getCallGroupId(callGroupExtension);
        CallGroup callGroup = m_context.loadCallGroup(callGroupId);
        return getCallGroup(callGroup);
	}
	
    public Response getCallGroup(CallGroup callGroup) {
        if (callGroup != null) {
            return Response.ok().entity(CallGroupBean.convertCallGroup(callGroup)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }	
}

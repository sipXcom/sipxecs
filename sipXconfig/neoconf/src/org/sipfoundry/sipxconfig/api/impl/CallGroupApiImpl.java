package org.sipfoundry.sipxconfig.api.impl;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sipfoundry.sipxconfig.api.CallGroupApi;
import org.sipfoundry.sipxconfig.api.model.CallGroupList;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;
import org.sipfoundry.sipxconfig.callgroup.CallGroupContext;
import org.springframework.beans.factory.annotation.Required;

public class CallGroupApiImpl implements CallGroupApi {
	
	private CallGroupContext m_context;
	
	@Override
	public Response getCallGroups() {
        List<CallGroup> callGroups = m_context.getCallGroups();
        if (callGroups != null) {
            return Response.ok().entity(CallGroupList.convertCallGroupList(callGroups)).build();
        }
        return Response.status(Status.NOT_FOUND).build();
	}

	@Required
	public void setContext(CallGroupContext context) {
		m_context = context;
	}

}

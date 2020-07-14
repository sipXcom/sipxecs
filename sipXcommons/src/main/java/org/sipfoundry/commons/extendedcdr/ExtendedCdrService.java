package org.sipfoundry.commons.extendedcdr;

import java.util.List;

public interface ExtendedCdrService {
	
	public void saveExtendedCdr(ExtendedCdrBean extCdrBean);
	
	public List<ExtendedCdrBean> getExtendedCdrs(List<String> callId);
	
	public ExtendedCdrBean getExtendedCdrByCaller(String callId, String caller);
}

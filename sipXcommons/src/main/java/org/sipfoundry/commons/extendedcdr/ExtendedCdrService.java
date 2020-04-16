package org.sipfoundry.commons.extendedcdr;

import java.util.List;

public interface ExtendedCdrService {
	
	public void saveExtendedCdr(ExtendedCdrBean extCdrBean);
	
	public ExtendedCdrBean getExtendedCdr(String callId);
	
	public List<ExtendedCdrBean> getExtendedCdrs(List<String> callId);
}

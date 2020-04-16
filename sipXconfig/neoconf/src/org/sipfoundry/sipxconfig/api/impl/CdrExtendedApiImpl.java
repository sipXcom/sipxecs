package org.sipfoundry.sipxconfig.api.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.elasticsearch.common.lang3.StringUtils;
import org.sipfoundry.commons.extendedcdr.ExtendedCdrBean;
import org.sipfoundry.commons.extendedcdr.ExtendedCdrService;
import org.sipfoundry.sipxconfig.api.CdrExtendedApi;
import org.sipfoundry.sipxconfig.api.model.CdrBean;
import org.sipfoundry.sipxconfig.api.model.CdrList;
import org.sipfoundry.sipxconfig.setting.Group;

public class CdrExtendedApiImpl extends BaseCdrApiImpl implements CdrExtendedApi {
	
	private ExtendedCdrService m_extendedCdrService;
	
	@Override
	public Response newCdr(ExtendedCdrBean extendedCdrBean) {
        Group group = new Group();
        m_extendedCdrService.saveExtendedCdr(extendedCdrBean);
        return Response.ok().entity(group.getId()).build();
	}

	@Override
    public Response getCdrHistory(String fromDate, String toDate, String from, String to, Integer limit,
            Integer offset, String orderBy, HttpServletRequest request) {
		CdrList list = CdrList.convertCdrList(getCdrs(fromDate, toDate, from, to, limit, offset, orderBy, null),
                request.getLocale());
		List<CdrBean> listCdrs = list.getCdrs();
		List<String> callIds = new ArrayList<String>();
		for (CdrBean bean : listCdrs) {
			callIds.add(bean.getCallId());
		}
		List<ExtendedCdrBean> extendedCdrs = callIds.isEmpty() ? 
				new ArrayList<ExtendedCdrBean>() : m_extendedCdrService.getExtendedCdrs(callIds);
		Collections.sort(listCdrs, CDR_CALL_ID);
		Collections.sort(extendedCdrs, EXTENDED_CDR_CALL_ID);
		for (CdrBean bean : listCdrs) {
			for (ExtendedCdrBean extBean : extendedCdrs) {
				if (StringUtils.equals(bean.getCallId(), extBean.getCallId())) {
					bean.setExtendedCdr(extBean);
					extendedCdrs.remove(extBean);
					break;
				}
			}
		}
        return Response
                .ok()
                .entity(list).build();
	}
	
	public void setExtendedCdrService(ExtendedCdrService extendedCdrService) {
		m_extendedCdrService = extendedCdrService;
	}	
	
    public static Comparator<CdrBean> CDR_CALL_ID = new Comparator<CdrBean>() {
        @Override
        public int compare(CdrBean o1, CdrBean o2) {
            return o1.getCallId().compareTo(o2.getCallId());
        }
    };
    
    public static Comparator<ExtendedCdrBean> EXTENDED_CDR_CALL_ID = new Comparator<ExtendedCdrBean>() {
        @Override
        public int compare(ExtendedCdrBean o1, ExtendedCdrBean o2) {
            return o1.getCallId().compareTo(o2.getCallId());
        }
    };
}

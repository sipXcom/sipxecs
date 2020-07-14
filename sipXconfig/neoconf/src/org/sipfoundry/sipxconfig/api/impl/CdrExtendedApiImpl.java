package org.sipfoundry.sipxconfig.api.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.extendedcdr.ExtendedCdrBean;
import org.sipfoundry.commons.extendedcdr.ExtendedCdrService;
import org.sipfoundry.sipxconfig.api.CdrExtendedApi;
import org.sipfoundry.sipxconfig.api.model.CdrBean;
import org.sipfoundry.sipxconfig.api.model.CdrList;
import org.sipfoundry.sipxconfig.cdr.CdrSearch;
import org.sipfoundry.sipxconfig.common.User;

public class CdrExtendedApiImpl extends BaseCdrApiImpl implements CdrExtendedApi {
	
	private ExtendedCdrService m_extendedCdrService;
	private static final Log LOG = LogFactory.getLog(CdrExtendedApiImpl.class);
	
	@Override
	public Response newCdr(ExtendedCdrBean extendedCdrBean) {        
        m_extendedCdrService.saveExtendedCdr(extendedCdrBean);
        return Response.ok().entity(extendedCdrBean.getId()).build();
	}

	@Override
    public Response getCdrHistory(String fromDate, String toDate, String from, String to, Integer limit,
            Integer offset, String orderBy, String orderDirection, HttpServletRequest request) {
		
		CdrList list = CdrList.convertCdrList(getCdrs(fromDate, toDate, from, to, limit, offset, orderBy, orderDirection, null, null, false),
                request.getLocale());
		addExtendedData(list);
		CdrSearch search = getSearch(from, to, orderBy, null, false);
        Date start = getDate(fromDate, RequestUtils.getDefaultStartTime());
        Date end = getDate(toDate, RequestUtils.getDefaultEndTime());
		list.setTotal(m_cdrManager.getCdrCount(start, end, search, null));
        return Response
                .ok()
                .entity(list).build();
	}
	
	
    @Override
    public Response getUserCdrHistory(String userId, String fromDate, String toDate, String from, String to,
            Integer limit, Integer offset, String orderBy, String orderDirection, HttpServletRequest request) {
        User user = getUserByIdOrUserName(userId);
        if (user != null) {        	
    		CdrList list = CdrList.convertCdrList(getCdrs(fromDate, toDate, from, to, limit, offset, orderBy, orderDirection, user, null, false),
                    request.getLocale());
    		addExtendedData(list);
    		CdrSearch search = getSearch(from, to, orderBy, null, false);
            Date start = getDate(fromDate, RequestUtils.getDefaultStartTime());
            Date end = getDate(toDate, RequestUtils.getDefaultEndTime());
    		list.setTotal(m_cdrManager.getCdrCount(start, end, search, user));
            return Response
                    .ok()
                    .entity(list).build();        	
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    @Override
    public Response getPrefixCdrHistory(String fromDate, String toDate, String from, String to,
            Integer limit, Integer offset, String orderBy, String orderDirection, String prefix, HttpServletRequest request) {

    	CdrList list = CdrList.convertCdrList(getCdrs(fromDate, toDate, from, to, limit, offset, orderBy, orderDirection, null, prefix, true), 
                request.getLocale());
    	addExtendedData(list);
		CdrSearch search = getSearch(from, to, orderBy, prefix, true);
        Date start = getDate(fromDate, RequestUtils.getDefaultStartTime());
        Date end = getDate(toDate, RequestUtils.getDefaultEndTime());
		list.setTotal(m_cdrManager.getCdrCount(start, end, search, null));
        return Response
                .ok()
                .entity(list).build();
    }
	
	private void addExtendedData(CdrList list) {
		List<CdrBean> listCdrs = list.getCdrs();
		for (CdrBean bean : listCdrs) {
			bean.setExtendedCdrY(m_extendedCdrService.getExtendedCdrByCaller(bean.getCallId(), "Y"));
			bean.setExtendedCdrN(m_extendedCdrService.getExtendedCdrByCaller(bean.getCallId(), "N"));
		}
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

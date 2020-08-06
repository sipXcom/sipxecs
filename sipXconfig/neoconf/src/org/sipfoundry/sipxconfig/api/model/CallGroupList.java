package org.sipfoundry.sipxconfig.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.sipfoundry.sipxconfig.callgroup.CallGroup;

@XmlRootElement(name = "CallGroups")
public class CallGroupList {

    private List<CallGroupBean> m_callGroups;

    public void setCallGroups(List<CallGroupBean> callGroups) {
    	m_callGroups = callGroups;
    }

    @XmlElement(name = "CallGroup")
    public List<CallGroupBean> getCallGroups() {
        if (m_callGroups == null) {
        	m_callGroups = new ArrayList<CallGroupBean>();
        }
        return m_callGroups;
    }

    public static CallGroupList convertCallGroupList(List<CallGroup> callGroups) throws Exception {
        List<CallGroupBean> callGroupBeans = new ArrayList<CallGroupBean>();
        for (CallGroup callGroup : callGroups) {
        	callGroupBeans.add(CallGroupBean.convertCallGroup(callGroup));
        }
        CallGroupList list = new CallGroupList();
        list.setCallGroups(callGroupBeans);
        return list;
    }
    
    public static CallGroupList convertCallGroupList(List<CallGroup> callGroups, String prefix) throws Exception {
        List<CallGroupBean> callGroupBeans = new ArrayList<CallGroupBean>();
        for (CallGroup callGroup : callGroups) {
        	if (callGroup.getExtension().startsWith(prefix)) {
        		callGroupBeans.add(CallGroupBean.convertCallGroup(callGroup));
        	}
        }
        CallGroupList list = new CallGroupList();
        list.setCallGroups(callGroupBeans);
        return list;
    }
}

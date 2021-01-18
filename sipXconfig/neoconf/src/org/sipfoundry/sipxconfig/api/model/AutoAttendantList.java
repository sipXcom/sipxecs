package org.sipfoundry.sipxconfig.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;

@XmlRootElement(name = "AutoAttendants")
public class AutoAttendantList {
    private List<AutoAttendantBean> m_autoAttendants;

    public void setAutoAttendants(List<AutoAttendantBean> autoAttendants) {
        m_autoAttendants = autoAttendants;
    }

    @XmlElement(name = "AutoAttendant")
    public List<AutoAttendantBean> getAutoAttendands() {
        if (m_autoAttendants == null) {
            m_autoAttendants = new ArrayList<AutoAttendantBean>();
        }
        return m_autoAttendants;
    }

    public static AutoAttendantList convertAutoAttendantList(List<AutoAttendant> autoAttendants) {
        List<AutoAttendantBean> autoAttendantList = new ArrayList<AutoAttendantBean>();
        for (AutoAttendant autoAttendant : autoAttendants) {
        	autoAttendantList.add(AutoAttendantBean.convertAutoAttendant(autoAttendant));
        }
        AutoAttendantList list = new AutoAttendantList();
        list.setAutoAttendants(autoAttendantList);
        return list;
    }
}

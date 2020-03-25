package org.sipfoundry.sipxconfig.api.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;

@XmlRootElement(name = "CallGroup")
public class CallGroupBean extends CallGroup {
    private static final Log LOG = LogFactory.getLog(CallGroupBean.class);

    public static CallGroupBean convertCallGroup(CallGroup callGroup) {
        if (callGroup == null) {
            return null;
        }
        try {
        	CallGroupBean bean = new CallGroupBean();
            BeanUtils.copyProperties(bean, callGroup);
            return bean;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void convertToCallGroup(CallGroupBean callGroupBean, CallGroup callGroup) {
        try {
            BeanUtils.copyProperties(callGroup, callGroupBean);
        } catch (Exception e) {
            LOG.error("Cannot marshal properties");
        }
    }
}

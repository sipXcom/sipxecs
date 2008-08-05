/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 * 
 */
package org.sipfoundry.sipxconfig.site.dialplan.sbc;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectComponent;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.html.BasePage;
import org.sipfoundry.sipxconfig.admin.dialplan.sbc.Sbc;
import org.sipfoundry.sipxconfig.admin.dialplan.sbc.SbcDevice;
import org.sipfoundry.sipxconfig.admin.dialplan.sbc.SbcManager;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.site.dialplan.ActivateDialPlan;

public abstract class InternetCalling extends BasePage implements PageBeginRenderListener {
    public static final String PAGE = "dialplan/sbc/InternetCalling";

    @InjectObject(value = "spring:sbcManager")
    public abstract SbcManager getSbcManager();

    @InjectComponent(value = "natTraversalComponent")
    public abstract NatTraversalPanel getNatTraversalPanel();

    public abstract Sbc getSbc();

    public abstract void setSbc(Sbc sbc);

    public abstract void setSelectedSbcDevice(SbcDevice selectedSbcDevice);

    public abstract SbcDevice getSelectedSbcDevice();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @Persist
    public abstract boolean isAdvanced();

    @Persist
    @InitialValue(value = "literal:internetCalling")
    public abstract String getTab();

    public void pageBeginRender(PageEvent event_) {
        Sbc sbc = getSbc();
        if (sbc == null) {
            sbc = getSbcManager().loadDefaultSbc();
            setSbc(sbc);
            SbcDevice sbcDevice = sbc.getSbcDevice();
            if (sbcDevice != null) {
                setSelectedSbcDevice(sbcDevice);
            }
        }
    }

    public IPage activate(IRequestCycle cycle) {
        if (!TapestryUtils.isValid(this)) {
            return null;
        }

        saveValid();
        getNatTraversalPanel().saveValid();

        ActivateDialPlan dialPlans = (ActivateDialPlan) cycle.getPage(ActivateDialPlan.PAGE);
        dialPlans.setReturnPage(this);
        return dialPlans;
    }

    private void saveValid() {
        Sbc sbc = getSbc();
        if (getSelectedSbcDevice() == null) {
            throw new UserException(getMessages().getMessage("error.requiredSbc"));
        }
        sbc.setSbcDevice(getSelectedSbcDevice());
        getSbcManager().saveSbc(sbc);
    }

}

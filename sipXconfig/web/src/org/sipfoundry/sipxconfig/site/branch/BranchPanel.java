/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */

package org.sipfoundry.sipxconfig.site.branch;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.form.StringPropertySelectionModel;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.time.NtpManager;

import com.davekoelle.AlphanumComparator;

public abstract class BranchPanel extends BaseComponent implements PageBeginRenderListener {

    @InjectObject("spring:branchManager")
    public abstract BranchManager getBranchManager();

    @InjectObject("spring:ntpManager")
    public abstract NtpManager getTimeManager();

    @Parameter(required = true)
    public abstract Branch getBranch();

    public abstract String getTimezoneType();

    public abstract void setTimezoneType(String type);

    public abstract IPropertySelectionModel getTimezoneTypeModel();

    public abstract void setTimezoneTypeModel(IPropertySelectionModel model);

    @Parameter
    public abstract ICallback getCallback();

    @Override
    public void pageBeginRender(PageEvent event_) {
        // Init. the timezone dropdown menu.
        List<String> timezoneList = getTimeManager().getAvailableTimezones();

        // Sort list alphanumerically.
        Collections.sort(timezoneList, new AlphanumComparator());
        StringPropertySelectionModel model = new StringPropertySelectionModel(
                timezoneList.toArray(new String[timezoneList.size()]));
        setTimezoneTypeModel(model);
    }

    public String getBranchTimezone() {
        if (getBranch().getTimeZone() == null) {
            return getTimeManager().getSystemTimezone();
        } else {
            return getBranch().getTimeZone();
        }
    }

    public void setBranchTimeZone(String timeZone) {
        getBranch().setTimeZone(timeZone);
    }

    /*
     * If the input is valid, then save changes to the branch.
     */
    public void apply() {
        if (!TapestryUtils.isValid(this)) {
            return;
        }
        Branch branch = getBranch();
        getBranchManager().saveBranch(branch);
    }
}

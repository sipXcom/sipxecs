/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */

package org.sipfoundry.sipxconfig.site.branch;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.components.ObjectSelectionModel;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

public abstract class CallRestrictionsPanel extends BaseComponent {

    @InjectObject("spring:branchManager")
    public abstract BranchManager getBranchManager();

    @Parameter(required = true)
    public abstract Branch getBranch();

    @Parameter
    public abstract ICallback getCallback();

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
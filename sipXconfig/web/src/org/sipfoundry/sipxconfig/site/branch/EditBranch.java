/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.site.branch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;

public abstract class EditBranch extends PageWithCallback implements PageBeginRenderListener {

    public static final String PAGE = "branch/EditBranch";

    private static final String BRANCH_TAB = "branch";

    private static final String ASSOCIATED_TAB = "associated";

    private static final String CALL_RESTRICTIONS_TAB = "callRestrictions";

    private static final String CALL_PERMISSIONS_TAB = "callPermissions";
    
    private static final String CALL_FALLBACK_PERMISSIONS_TAB = "callFallbackPermissions";

    @Persist
    public abstract Integer getBranchId();

    public abstract void setBranchId(Integer branchId);

    public abstract Branch getBranch();

    public abstract void setBranch(Branch branch);

    @Persist
    @InitialValue("literal:branch")
    public abstract String getTab();

    public abstract void setTab(String tab);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    @InjectObject("spring:branchManager")
    public abstract BranchManager getBranchManager();

    public Collection<String> getAvailableTabNames() {
        Collection<String> tabNames = new ArrayList<String>();
        tabNames.addAll(Arrays.asList(
            BRANCH_TAB, ASSOCIATED_TAB, CALL_RESTRICTIONS_TAB, CALL_PERMISSIONS_TAB, CALL_FALLBACK_PERMISSIONS_TAB));
        return tabNames;
    }

    public void pageBeginRender(PageEvent event_) {
        Branch branch = getBranch();
        if (branch != null) {
            // make sure we have correct bean ID persisted
            if (!branch.isNew()) {
                setBranchId(branch.getId());
            }
            return;
        }
        if (getBranchId() != null) {
            branch = getBranchManager().getBranch(getBranchId());
        } else {
            branch = new Branch();
            setTab(BRANCH_TAB);
        }
        setBranch(branch);
    }

    @Override
    public String getBreadCrumbTitle() {
        return null == getBranchId() ? "&crumb.new.branch"
            : getBranchManager().getBranch(getBranchId()).getName();
    }
}

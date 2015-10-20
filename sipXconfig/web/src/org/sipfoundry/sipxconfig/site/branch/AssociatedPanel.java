/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.site.branch;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.ListUtils;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.sipfoundry.sipxconfig.acccode.AuthCode;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.branch.BranchManager;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.dialplan.AttendantRule;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendant;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchExtension;
import org.sipfoundry.sipxconfig.gateway.Gateway;
import org.sipfoundry.sipxconfig.paging.PagingGroup;
import org.sipfoundry.sipxconfig.parkorbit.ParkOrbit;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.site.admin.EditAuthCode;
import org.sipfoundry.sipxconfig.site.admin.EditCallGroup;
import org.sipfoundry.sipxconfig.site.conference.EditConference;
import org.sipfoundry.sipxconfig.site.dialplan.EditAutoAttendant;
import org.sipfoundry.sipxconfig.site.dialplan.EditDialRule;
import org.sipfoundry.sipxconfig.site.gateway.EditGateway;
import org.sipfoundry.sipxconfig.site.paging.EditPagingGroupPage;
import org.sipfoundry.sipxconfig.site.park.EditParkOrbit;
import org.sipfoundry.sipxconfig.site.user.UserGroupSettings;

public abstract class AssociatedPanel extends BaseComponent implements PageBeginRenderListener {

    @InjectObject("spring:branchManager")
    public abstract BranchManager getBranchManager();

    @InjectObject("spring:coreContext")
    public abstract CoreContext getCoreContext();

    @Parameter(required = true)
    public abstract Branch getBranch();

    @Parameter
    public abstract ICallback getCallback();


    public abstract List<?> getBranchGroups();
    public abstract void setBranchGroups(List<?> branchGroups);
    public abstract Group getBranchGroup();

    public abstract List<?> getBranchConferences();
    public abstract void setBranchConferences(List<?> branchConferences);
    public abstract Conference getBranchConference();

    public abstract List<?> getBranchGateways();
    public abstract void setBranchGateways(List<?> branchGateways);
    public abstract Gateway getBranchGateway();

    public abstract List<?> getBranchAuthCodes();
    public abstract void setBranchAuthCodes(List<?> branchAuthCodes);
    public abstract AuthCode getBranchAuthCode();

    public abstract List<?> getBranchAutoAttendants();
    public abstract void setBranchAutoAttendants(List<?> branchAutoAttendants);
    public abstract AutoAttendant getBranchAutoAttendant();

    public abstract List<?> getBranchParkOrbits();
    public abstract void setBranchParkOrbits(List<?> branchParkOrbits);
    public abstract ParkOrbit getBranchParkOrbit();

    public abstract List<?> getBranchCallGroups();
    public abstract void setBranchCallGroups(List<?> branchCallGroups);
    public abstract CallGroup getBranchCallGroup();

    public abstract List<?> getBranchPagingGroups();
    public abstract void setBranchPagingGroups(List<?> branchPagingGroups);
    public abstract PagingGroup getBranchPagingGroup();

    public abstract List<?> getBranchFreeswitchExtensions();
    public abstract void setBranchFreeswitchExtensions(List<?> branchFreeswitchExtensions);
    public abstract FreeswitchExtension getBranchFreeswitchExtension();

    public abstract List<?> getBranchAttendantRules();
    public abstract void setBranchAttendantRules(List<?> branchAttendantRules);
    public abstract DialingRule getBranchDialingRule();

    public void pageBeginRender(PageEvent event) {
        List<?> groups = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.GROUP_BY_BRANCH, Group.class);
        setBranchGroups(groups);

        List<?> conferences = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.CONFERENCES_BY_BRANCH, Conference.class);
        setBranchConferences(conferences);

        List<?> branchGateways = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.GATEWAYS_BY_BRANCH, Gateway.class);
        List<?> sharedGateways = getBranchManager().getFeatureNames(BranchManager.SHARED_GATEWAYS, Gateway.class);
        branchGateways.removeAll(sharedGateways);
        List<?> gateways = ListUtils.union(branchGateways, sharedGateways);
        setBranchGateways(gateways);

        List<?> authCodes = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.AUTH_CODES_BY_BRANCH, AuthCode.class);
        setBranchAuthCodes(authCodes);

        List<?> autoAttendants = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.AUTO_ATTENDANDS_BY_BRANCH, AutoAttendant.class);
        setBranchAutoAttendants(autoAttendants);

        List<?> parkOrbits = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.PARKS_BY_BRANCH, ParkOrbit.class);
        setBranchParkOrbits(parkOrbits);

        List<?> callGroups = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.CALL_GROUP_BY_BRANCH, CallGroup.class);
        setBranchCallGroups(callGroups);

        List<?> pagingGroups = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.PAGING_GROUP_BY_BRANCH, PagingGroup.class);
        setBranchPagingGroups(pagingGroups);

        List<?> freeswitchExtensions = getBranchManager().getFeatureNames(getBranch().getId(),
                BranchManager.CALL_QUEUE_BY_BRANCH, FreeswitchExtension.class);
        setBranchFreeswitchExtensions(freeswitchExtensions);

        List<?> branchAttendantRules = getBranchManager().getFeatureNames(getBranch().getId(),
            BranchManager.AUTO_ATTENDANT_DIALING_RULES_BY_BRANCH, AttendantRule.class);
        setBranchAttendantRules(branchAttendantRules);
    }

    public IPage editGroup(IRequestCycle cycle, Integer branchGroupId) {
        UserGroupSettings page = (UserGroupSettings) cycle.getPage(UserGroupSettings.PAGE);
        page.setGroupId(branchGroupId);
        User user = getCoreContext().newUser();
        page.setBean(user);
        return page;
    }

    public IPage editConference(IRequestCycle cycle, Integer branchConferenceId) {
        EditConference page = (EditConference) cycle.getPage(EditConference.PAGE);
        page.setConferenceId(branchConferenceId);
        return page;
    }

    public IPage editGateway(IRequestCycle cycle, Integer branchGatewayId) {
        EditGateway page = (EditGateway) cycle.getPage(EditGateway.PAGE);
        page.setGatewayId(branchGatewayId);
        return page;
    }

    public IPage editAuthCode(IRequestCycle cycle, Integer authCodeId) {
        EditAuthCode page = (EditAuthCode) cycle.getPage(EditAuthCode.PAGE);
        page.setAuthCodeId(authCodeId);
        return page;
    }

    public IPage editAutoAttendant(IRequestCycle cycle, AutoAttendant autoAttendant) {
        EditAutoAttendant page = (EditAutoAttendant) cycle.getPage(EditAutoAttendant.PAGE);
        page.setAttendant(autoAttendant);
        return page;
    }

    public IPage editParkOrbit(IRequestCycle cycle, Integer parkOrbitId) {
        EditParkOrbit page = (EditParkOrbit) cycle.getPage(EditParkOrbit.PAGE);
        page.setParkOrbitId(parkOrbitId);
        return page;
    }

    public IPage editCallGroup(IRequestCycle cycle, Integer callGroupId) {
        EditCallGroup page = (EditCallGroup) cycle.getPage(EditCallGroup.PAGE);
        page.setCallGroupId(callGroupId);
        return page;
    }

    public IPage editPagingGroup(IRequestCycle cycle, Integer pagingGroupId) {
        EditPagingGroupPage page = (EditPagingGroupPage) cycle.getPage(EditPagingGroupPage.PAGE);
        page.editPagingGroup(pagingGroupId, EditBranch.PAGE);
        return page;
    }

    public IPage editFreeswitchExtensions(IRequestCycle cycle, Integer freeswitchExtensionId) {
        LocationsAware page = (LocationsAware) cycle.getPage("plugin/CallQueueEditQueue");
        page.setFeatureId(freeswitchExtensionId);
        return page;
    }

    public IPage editAutoAttendantDialingRule(IRequestCycle cycle, Integer ruleId) {
        EditDialRule page = (EditDialRule) cycle.getPage(EditDialRule.ATTENDANT);
        page.setRuleId(ruleId);
        return page;
    }

}

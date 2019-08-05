/*
 *
 *
 * Copyright (C) 2019 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.didpool;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.hivemind.util.PropertyUtils;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.callback.PageCallback;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEndRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.elasticsearch.common.lang3.ArrayUtils;
import org.elasticsearch.common.lang3.StringUtils;
import org.sipfoundry.commons.diddb.AbstractDecoratedIterator;
import org.sipfoundry.commons.diddb.Did;
import org.sipfoundry.commons.diddb.DidPool;
import org.sipfoundry.commons.diddb.DidPoolService;
import org.sipfoundry.commons.diddb.DidService;
import org.sipfoundry.commons.diddb.DidType;
import org.sipfoundry.commons.diddb.LabeledDid;
import org.sipfoundry.sipxconfig.callgroup.CallGroup;
import org.sipfoundry.sipxconfig.callgroup.CallGroupContext;
import org.sipfoundry.sipxconfig.common.BeanId;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.conference.ConferenceBridgeContext;
import org.sipfoundry.sipxconfig.dialplan.AttendantRule;
import org.sipfoundry.sipxconfig.dialplan.AutoAttendantManager;
import org.sipfoundry.sipxconfig.dialplan.DialPlanContext;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
import org.sipfoundry.sipxconfig.dialplan.DialingRuleType;
import org.sipfoundry.sipxconfig.dialplan.InternalRule;
import org.sipfoundry.sipxconfig.dialplan.attendant.AutoAttendantSettings;
import org.sipfoundry.sipxconfig.site.admin.EditCallGroup;
import org.sipfoundry.sipxconfig.site.conference.EditConference;
import org.sipfoundry.sipxconfig.site.dialplan.EditDialRule;
import org.sipfoundry.sipxconfig.site.dialplan.ManageAttendants;
import org.sipfoundry.sipxconfig.site.user.EditUser;
import org.sipfoundry.sipxconfig.site.user.SelectUsers;
import org.sipfoundry.sipxconfig.site.user.SelectUsersCallback;

public abstract class DiDPoolsPage extends SipxBasePage implements PageBeginRenderListener, PageEndRenderListener {

    public static final String PAGE = "didpool/DiDPools";

    private static final String CALLBACK_PROPERTY_NAME = "selectedUsers";

    @InjectObject(value = "spring:didService")
    public abstract DidService getDidService();

    @InjectObject(value = "spring:didPoolService")
    public abstract DidPoolService getDidPoolService();

    @InjectObject(value = "spring:coreContext")
    public abstract CoreContext getCoreContext();
    
    @InjectObject(value = "spring:dialPlanContext")
    public abstract DialPlanContext getDialPlanContext();
    
    @InjectObject(value = "spring:callGroupContext")
    public abstract CallGroupContext getCallGroupContext();

    @InjectObject("spring:conferenceBridgeContext")
    public abstract ConferenceBridgeContext getConferenceBridgeContext();
    
    @InjectObject("spring:autoAttendantManager")
    public abstract AutoAttendantManager getAutoAttendantManager();
    
    public abstract List<Did> getDids();

    public abstract List<Did> getLabeledDids();

    public abstract void setDids(List<Did> dids);

    public abstract void setLabeledDids(List<Did> dids);

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public abstract int getIndex();

    public abstract String getTypeLabel();

    public abstract void setTypeLabel(String type);

    public abstract Did getCurrentRow();

    public abstract List<DidPool> getPools();

    public abstract void setPools(List<DidPool> pools);

    public abstract void setSelectedUsers(Collection<Integer> selectedUsers);

    public abstract Collection<Integer> getSelectedUsers();

    public abstract String getNext();

    public abstract void setNext(String next);

    public abstract String getUserNext();

    public abstract void setUserNext(String userNext);

    @Persist
    public abstract DidPoolSearch getDidPoolSearch();

    public abstract void setDidPoolSearch(DidPoolSearch didPoolSearch);

    @Persist
    public abstract String getNextToUse();

    public abstract void setNextToUse(String nextToUse);

    @InitialValue("false")
    @Persist
    public abstract boolean getSearchMode();

    @Persist
    public abstract Integer getGroupId();

    public abstract void setGroupId(Integer groupId);
    
    @Bean
    public abstract SelectMap getSelections();
    
    public abstract Collection getRowsToDelete();

    @Override
    public void pageBeginRender(PageEvent event) {
        ManageAttendants page = (ManageAttendants) event.getRequestCycle().getPage(ManageAttendants.PAGE);
        page.setReturnPage(ManageAttendants.PAGE);
        if (getDidPoolSearch() == null) {
            setDidPoolSearch(new DidPoolSearch());
        }
        List<Did> dids = null;
        String [] terms = getDidPoolSearch().getTerm();
        if (ArrayUtils.isEmpty(terms) || StringUtils.isEmpty(terms[0])) {
            dids = getDidService().getAllDids();
        } else if (getDidPoolSearch().getMode().equals(DidPoolSearch.Mode.DIDEXTENSION)) {
            dids = getDidService().searchDidsByValue(terms[0]);
        } else if (getDidPoolSearch().getMode().equals(DidPoolSearch.Mode.EXTENSION)) {
            dids = getDidService().searchDidsByExtension(terms[0]);
        } else {
            dids = getDidService().getAllDids();
        }
        setDids(dids);
        setLabeledDids(IteratorUtils.toList(new Decorated(dids).iterator()));
        List<DidPool> pools = getPools();
        if (pools == null) {
            pools = getDidPoolService().getAllDidPools();
            setPools(pools);
        }
        if (getNext() == null) {
            Did activeDid = getDidService().getActiveNextDid();
            setNext(activeDid == null ? null : activeDid.getValue());
        }
    }

    public void pageEndRender(PageEvent event) {
        String nextValue = getNextToUse();

        try {
            Collection<Integer> selectedUsers = (Collection<Integer>) PropertyUtils.read(getPage(), CALLBACK_PROPERTY_NAME);
            if (selectedUsers != null && !selectedUsers.isEmpty()) {
                Integer newOwnerId = selectedUsers.iterator().next();
                User selectedUser = getCoreContext().loadUser(newOwnerId);

                if (nextValue != null) {
                    selectedUser.getUserProfile().setDidNumber(nextValue);
                    getCoreContext().saveUser(selectedUser);
                }
            }
        } catch (UserException e) {
            getValidator().record(e, getMessages());
        }
    }

    public Did getValue() {
        Did value = getDids().get(getIndex());
        setTypeLabel(getMessages().getMessage(value.getType()));
        return value;
    }

    public void setValue(Did value) {
        getDids().set(getIndex(), value);
    }

    public int getSize() {
        return getDids().size();
    }

    private class LabelIterator extends AbstractDecoratedIterator {
        List<DidPool> m_pools = null;
        public LabelIterator(Iterator<Did> source) {
            super(source);
            m_pools = getDidPoolService().getAllDidPools();
        }

        @Override
        public Did next() {
            Did next = getSource().next();
            DidPool myPool = getDidPoolService().getDidPoolById(next.getPoolId());
            String type = next.getType();
            LabeledDid labelDid = new LabeledDid(
                type, next.getTypeId(), next.getValue(), null);
            if (type != null) {
                labelDid.setTypeLabel(getMessages().getMessage(type));
            }
            labelDid.setDescription(myPool != null ? myPool.getDescription() : null);
            return labelDid;
        }
    }

    private class Decorated implements Iterable<Did> {
        private Iterable<Did> source;

        public Decorated(Iterable<Did> iterator) {
            this.source = iterator;
        }

        @Override
        public Iterator<Did> iterator() {
            return new LabelIterator(source.iterator());
        }
    }

    public IPage assignDid(IRequestCycle cycle) {
        if (getDidService().isDidInUse(getUserNext())) {
            throw new UserException("&next.did.used", getUserNext());
        }
        if (getDidPoolService().getAllDidPools().isEmpty()) {
        	throw new UserException("&no.did.pool");
        }
        boolean outsideRange = true;
        for (DidPool pool : getDidPoolService().getAllDidPools()) {
            if (getDidPoolService().outsideRangeDidValue(pool, Long.parseLong(getUserNext()))) {
                continue;
            } else {
                outsideRange = false;
                break;
            }
        }
        if (outsideRange) {
            throw new UserException("&err.notInRange");
        }
        SelectUsers selectUsersPage = (SelectUsers) cycle.getPage(SelectUsers.PAGE);
        SelectUsersCallback callback = new SelectUsersCallback(getPage());
        callback.setIdsPropertyName(CALLBACK_PROPERTY_NAME);
        selectUsersPage.setCallback(callback);
        selectUsersPage.setTitle(getMessages().getMessage("label.assign.to.user"));
        selectUsersPage.setPrompt(getMessages().getMessage("prompt.selectUser"));
        setNextToUse(getUserNext());
        return selectUsersPage;
    }
    
    public IPage entityListener(IRequestCycle cycle, String typeId, String type) {        
        if (StringUtils.equals(DidType.TYPE_USER.getName(), type)) {
            EditUser page = (EditUser) cycle.getPage(EditUser.PAGE);
            User user = getCoreContext().loadUserByUserName(typeId);
            page.setUser(user);
            page.setUserId(user.getId());
            page.setReturnPage(PAGE);
            return page; 
        } 
        else if (StringUtils.equals(DidType.TYPE_FAX_USER.getName(), type)) {
            EditUser page = (EditUser) cycle.getPage(EditUser.PAGE);
            BeanId beanId = (BeanId)CollectionUtils.get(getCoreContext().getBeanIdsOfObjectsWithAlias(typeId),0);
            User user = getCoreContext().getUser(beanId.getId());
            page.setUser(user);
            page.setUserId(user.getId());
            page.setReturnPage(PAGE);
            return page;
        }
        else if (StringUtils.equals(DidType.TYPE_AUTO_ATTENDANT_DIALING_RULE.getName(), type)) {
            EditDialRule page = (EditDialRule) cycle.getPage(EditDialRule.ATTENDANT);
            page.setRuleType(DialingRuleType.ATTENDANT);
            DialingRule rule = null;
            Iterator rules = getDialPlanContext().getAttendantRulesWithExtensionOrDid(typeId).iterator();
            if (rules.hasNext()) {
                Integer id = (Integer)rules.next();
                rule = getDialPlanContext().getRule(id);
                page.setRule(rule);
                page.setRuleId(id);
            }
            page.setReturnPage(PAGE);
            return page;
        } 
        else if (StringUtils.equals(DidType.TYPE_VOICEMAIL_DIALING_RULE.getName(), type)) {
            EditDialRule page = (EditDialRule) cycle.getPage(EditDialRule.INTERNAL);
            page.setRuleType(DialingRuleType.INTERNAL);
            DialingRule rule = null;
            Iterator rules = getDialPlanContext().getInternalRulesWithVoiceMailExtension(typeId).iterator();
            if (rules.hasNext()) {
                Integer id = (Integer)rules.next();
                rule = getDialPlanContext().getRule(id);
                page.setRule(rule);
                page.setRuleId(id);
            }
            page.setReturnPage(PAGE);
            return page;
        } 
        else if (StringUtils.equals(DidType.TYPE_HUNT_GROUP.getName(), type)) {
            EditCallGroup page = (EditCallGroup) cycle.getPage(EditCallGroup.PAGE);            
            page.setCallGroupId(getCallGroupContext().getCallGroupId(typeId));
            page.setReturnPage(PAGE);
            return page;
        } 
        else if (StringUtils.equals(DidType.TYPE_CONFERENCE.getName(), type)) {
            EditConference page = (EditConference) cycle.getPage(EditConference.PAGE);
            Conference conf = getConferenceBridgeContext().findConferenceByExtension(typeId);
            page.setConference(conf);
            page.setConferenceId(conf.getId());
            page.setBridge(conf.getBridge());
            page.setBridgeId(conf.getBridge().getId());
            page.setReturnPage(PAGE);
            return page;
        } 
        else if (StringUtils.equals(DidType.TYPE_LIVE_AUTO_ATTENDANT.getName(), type)) {
            ManageAttendants page = (ManageAttendants) cycle.getPage(ManageAttendants.PAGE);
            page.setReturnPage(PAGE);
            return page;
        }
        return null;               
    }
    
    public void deleteDids() {
        Collection<Did> rowsToDelete = getRowsToDelete();
        if (rowsToDelete != null) {
            Iterator iterator = rowsToDelete.iterator(); 
            while(iterator.hasNext()) {
                Did did = (Did)iterator.next();
                if (StringUtils.equals(DidType.TYPE_AUTO_ATTENDANT_DIALING_RULE.getName(), did.getType())) {
                    Iterator rules = getDialPlanContext().getAttendantRulesWithExtensionOrDid(did.getTypeId()).iterator();
                    if (rules.hasNext()) {
                        Integer id = (Integer)rules.next();
                        AttendantRule rule = (AttendantRule)getDialPlanContext().getRule(id);
                        rule.setDid(null);
                        getDialPlanContext().storeRule(rule);
                    }
                } 
                else if (StringUtils.equals(DidType.TYPE_VOICEMAIL_DIALING_RULE.getName(), did.getType())) {
                    Iterator rules = getDialPlanContext().getInternalRulesWithVoiceMailExtension(did.getTypeId()).iterator();
                    if (rules.hasNext()) {
                        Integer id = (Integer)rules.next();
                        InternalRule rule = (InternalRule)getDialPlanContext().getRule(id);
                        rule.setDid(null);
                        getDialPlanContext().storeRule(rule);
                    }
                } 
                else if (StringUtils.equals(DidType.TYPE_CONFERENCE.getName(), did.getType())) {
                    Conference conf = getConferenceBridgeContext().findConferenceByExtension(did.getTypeId());
                    conf.setDid(null);
                    getConferenceBridgeContext().saveConference(conf);
                } 
                else if (StringUtils.equals(DidType.TYPE_LIVE_AUTO_ATTENDANT.getName(), did.getType())) {
                    AutoAttendantSettings settings = getAutoAttendantManager().getSettings();
                    settings.setSettingValue(AutoAttendantSettings.LIVE_DID, null);
                    getAutoAttendantManager().saveSettings(settings);
                } 
                else if (StringUtils.equals(DidType.TYPE_HUNT_GROUP.getName(), did.getType())) {
                    CallGroup cg = getCallGroupContext().loadCallGroup(getCallGroupContext().getCallGroupId(did.getTypeId()));
                    cg.setDid(null);
                    getCallGroupContext().saveCallGroup(cg);
                } 
                else if (StringUtils.equals(DidType.TYPE_USER.getName(), did.getType())) {
                    User user = getCoreContext().loadUserByUserName(did.getTypeId());
                    user.getUserProfile().setDidNumber(null);
                    getCoreContext().saveUser(user);
                } 
                else if (StringUtils.equals(DidType.TYPE_FAX_USER.getName(), did.getType())) {
                    BeanId beanId = (BeanId)CollectionUtils.get(getCoreContext().getBeanIdsOfObjectsWithAlias(did.getTypeId()), 0);
                    User user = getCoreContext().getUser(beanId.getId());
                    user.setSaveFaxDid(true);
                    user.setFaxDid(null);
                    getCoreContext().saveUser(user);
                }
            }
        }
    }
}

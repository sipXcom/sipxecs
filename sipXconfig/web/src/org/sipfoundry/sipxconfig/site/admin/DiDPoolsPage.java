/*
 *
 *
 * Copyright (C) 2019 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.hivemind.util.PropertyUtils;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEndRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.elasticsearch.common.lang3.StringUtils;
import org.sipfoundry.commons.diddb.AbstractDecoratedIterator;
import org.sipfoundry.commons.diddb.ActiveNextDid;
import org.sipfoundry.commons.diddb.Did;
import org.sipfoundry.commons.diddb.DidPool;
import org.sipfoundry.commons.diddb.DidPoolService;
import org.sipfoundry.commons.diddb.DidService;
import org.sipfoundry.commons.diddb.LabeledDid;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SipxBasePage;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.site.common.ListStartEndPanel;
import org.sipfoundry.sipxconfig.site.user.SelectUsers;
import org.sipfoundry.sipxconfig.site.user.SelectUsersCallback;

public abstract class DiDPoolsPage extends SipxBasePage implements PageBeginRenderListener, PageEndRenderListener {

    public static final String PAGE = "admin/DiDPools";
    
    private static final String CALLBACK_PROPERTY_NAME = "selectedUsers";    

    @InjectObject(value = "spring:didService")
    public abstract DidService getDidService();
    
    @InjectObject(value = "spring:didPoolService")
    public abstract DidPoolService getDidPoolService();
    
    @InjectObject(value = "spring:coreContext")
    public abstract CoreContext getCoreContext();    

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
    public abstract String getNextToUse();
    
    public abstract void setNextToUse(String nextToUse);
    
    @Persist
    public abstract String getQueryText();

    @InitialValue("false")
    @Persist
    public abstract boolean getSearchMode();

    @Persist
    public abstract Integer getGroupId();

    public abstract void setGroupId(Integer groupId);    
    
    @Override
    public void pageBeginRender(PageEvent event) {
        List<Did> dids = getDids();
        if (dids == null) {
            dids = getDidService().getAllDids();
            setDids(dids);
            setLabeledDids(IteratorUtils.toList(new Decorated(dids).iterator()));
        }
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
            DidPool myPool = null;
            for (DidPool pool : m_pools) {
                if (StringUtils.equals(pool.getId(), next.getPoolId())) {
                    myPool = pool;
                    break;
                }
            }
            LabeledDid labelDid = new LabeledDid(
                next.getType(), next.getTypeId(), next.getValue(), null);
            labelDid.setTypeLabel(getMessages().getMessage(next.getType()));
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
    
    public void commit() {
        for (DidPool pool : getPools()) {
        	pool.setNext(getDidPoolService().findNext(pool).toString());
            getDidPoolService().saveDidPool(pool);
        }
        setDids(null);
    }

    public IPage assignDid(IRequestCycle cycle) {
        if (getDidService().isDidInUse(getUserNext())) {
            throw new UserException("&next.did.used", getUserNext());
        }
        if (getDidPoolService().getAllDidPools().isEmpty()) {
        	throw new UserException("&no.did.pool");
        }
        SelectUsers selectUsersPage = (SelectUsers) cycle.getPage(SelectUsers.PAGE);
        SelectUsersCallback callback = new SelectUsersCallback(getPage());
        callback.setIdsPropertyName(CALLBACK_PROPERTY_NAME);
        selectUsersPage.setCallback(callback);
        selectUsersPage.setTitle(getMessages().getMessage("label.assign.to.user"));
        selectUsersPage.setPrompt(getMessages().getMessage("prompt.selectRings"));
        setNextToUse(getUserNext());
        return selectUsersPage;
    }
}

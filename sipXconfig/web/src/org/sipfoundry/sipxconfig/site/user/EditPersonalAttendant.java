/*
 * 
 * 
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 * $
 */
package org.sipfoundry.sipxconfig.site.user;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.admin.localization.LocalizationContext;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.site.admin.ModelWithDefaults;
import org.sipfoundry.sipxconfig.site.common.LanguageSupport;
import org.sipfoundry.sipxconfig.vm.MailboxManager;
import org.sipfoundry.sipxconfig.vm.attendant.PersonalAttendant;

public abstract class EditPersonalAttendant extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "user/EditPersonalAttendant";
    
    @Bean
    public abstract SipxValidationDelegate getValidator();
    
    @InjectObject(value = "spring:localizationContext")
    public abstract LocalizationContext getLocalizationContext();
    
    @InjectObject(value = "spring:coreContext")
    public abstract CoreContext getCoreContext();
    
    @InjectObject(value = "spring:mailboxManager")
    public abstract MailboxManager getMailboxManager();
    
    @InjectObject(value = "spring:jarMessagesSource")
    public abstract LanguageSupport getLanguageSupport();
    
    @Persist
    public abstract Integer getUserId();
    public abstract void setUserId(Integer userId);
    
    public abstract Setting getParentSetting();
    public abstract void setParentSetting(Setting setting);
    
    public abstract User getUser();
    public abstract void setUser(User user);
    
    public abstract PersonalAttendant getPersonalAttendant();
    public abstract void setPersonalAttendant(PersonalAttendant personalAttendant);
    
    public abstract IPropertySelectionModel getLanguageList();
    public abstract void setLanguageList(IPropertySelectionModel languageList);
    
    public void pageBeginRender(PageEvent event) {
        if (getLanguageList() == null) {
            initLanguageList();
        }
        if (getUser() == null) {
            User user = getCoreContext().loadUser(getUserId());
            setUser(user);
            
            Setting personalAttendantSetting = user.getSettings().getSetting("personal-attendant");
            setParentSetting(personalAttendantSetting);
            
            PersonalAttendant attendant = getMailboxManager().loadPersonalAttendantForUser(user);
            setPersonalAttendant(attendant);
        }
    }
    
    public void commit() {
        getMailboxManager().storePersonalAttendant(getPersonalAttendant());
        getCoreContext().saveUser(getUser());
    }
    
    protected void initLanguageList() {
        String[] languages = getLocalizationContext().getInstalledLanguages();
        String[] localizedLocales = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            localizedLocales[i] = getLanguageSupport().resolveLocaleName(languages[i]);
        }
        IPropertySelectionModel model = new ModelWithDefaults(null, localizedLocales);
        setLanguageList(model);    
    }
}

/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.vm;

import static org.sipfoundry.sipxconfig.vm.MailboxPreferences.ACTIVE_GREETING;
import static org.sipfoundry.sipxconfig.vm.MailboxPreferences.BUSY_PROMPT;
import static org.sipfoundry.sipxconfig.vm.MailboxPreferences.UNIFIED_MESSAGING_LANGUAGE;
import static org.sipfoundry.sipxconfig.vm.MailboxPreferences.VOICEMAIL_TUI;

import org.custommonkey.xmlunit.XMLTestCase;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.permission.PermissionManagerImpl;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences.ActiveGreeting;
import org.sipfoundry.sipxconfig.vm.MailboxPreferences.VoicemailTuiType;

public class MailboxPreferencesTest extends XMLTestCase {

    public void testGetValueOfById() {
        MailboxPreferences.ActiveGreeting actual = MailboxPreferences.ActiveGreeting.fromId("none");
        assertSame(MailboxPreferences.ActiveGreeting.NONE, actual);
    }

    public void testFromUser() {
        PermissionManagerImpl pm = new PermissionManagerImpl();
        pm.setModelFilesContext(TestHelper.getModelFilesContext());

        User user = new User();
        user.setPermissionManager(pm);
        user.setEmailAddress("first@example.com");
        user.setAlternateEmailAddress("second@example.com");
        user.setPrimaryEmailNotification(MailboxPreferences.AttachType.YES.getValue());
        user.setPrimaryEmailFormat(MailboxPreferences.MailFormat.MEDIUM.name());
        user.setPrimaryEmailAttachAudio(true);
        user.setAlternateEmailNotification(MailboxPreferences.AttachType.NO.getValue());
        user.setAlternateEmailFormat(MailboxPreferences.MailFormat.FULL.name());
        user.setAlternateEmailAttachAudio(false);
        user.setSettingValue(UNIFIED_MESSAGING_LANGUAGE, "en");

        MailboxPreferences mailboxPrefs = new MailboxPreferences(user);

        assertEquals("first@example.com", mailboxPrefs.getEmailAddress());
        assertEquals("second@example.com", mailboxPrefs.getAlternateEmailAddress());
        assertEquals(MailboxPreferences.AttachType.YES, mailboxPrefs.getAttachVoicemailToEmail());
        assertEquals(MailboxPreferences.MailFormat.MEDIUM, mailboxPrefs.getEmailFormat());
        assertEquals(true, mailboxPrefs.isIncludeAudioAttachment());
        assertEquals(MailboxPreferences.AttachType.NO, mailboxPrefs.getVoicemailToAlternateEmailNotification());
        assertEquals(MailboxPreferences.MailFormat.FULL, mailboxPrefs.getAlternateEmailFormat());
        assertEquals(false, mailboxPrefs.isIncludeAudioAttachmentAlternateEmail());
        assertEquals("en", mailboxPrefs.getLanguage());
    }

    public void testUpdateUser() {
        MailboxPreferences mailboxPrefs = new MailboxPreferences();
        mailboxPrefs.setEmailAddress("first@example.com");
        mailboxPrefs.setAlternateEmailAddress("second@example.com");
        mailboxPrefs.setActiveGreeting(ActiveGreeting.EXTENDED_ABSENCE);
        mailboxPrefs.setLanguage("en");
        mailboxPrefs.setAttachVoicemailToEmail(MailboxPreferences.AttachType.YES);
        mailboxPrefs.setEmailFormat(MailboxPreferences.MailFormat.MEDIUM);
        mailboxPrefs.setIncludeAudioAttachment(true);
        mailboxPrefs.setVoicemailToAlternateEmailNotification(MailboxPreferences.AttachType.NO);
        mailboxPrefs.setAlternateEmailFormat(MailboxPreferences.MailFormat.FULL);
        mailboxPrefs.setIncludeAudioAttachmentAlternateEmail(false);

        PermissionManagerImpl pm = new PermissionManagerImpl();
        pm.setModelFilesContext(TestHelper.getModelFilesContext());
        User user = new User();
        user.setPermissionManager(pm);
        mailboxPrefs.updateUser(user);

        assertEquals("first@example.com", user.getEmailAddress());
        assertEquals("second@example.com", user.getAlternateEmailAddress());
        assertEquals(ActiveGreeting.EXTENDED_ABSENCE.getId(), user.getSettingValue(ACTIVE_GREETING));
        assertEquals("en", user.getSettingValue(UNIFIED_MESSAGING_LANGUAGE));
        assertEquals(MailboxPreferences.AttachType.YES.getValue(), user.getPrimaryEmailNotification());
        assertEquals(MailboxPreferences.MailFormat.MEDIUM.name(), user.getPrimaryEmailFormat());
        assertEquals(new Boolean(true), user.isPrimaryEmailAttachAudio());
        assertEquals(MailboxPreferences.AttachType.NO.getValue(), user.getAlternateEmailNotification());
        assertEquals(MailboxPreferences.MailFormat.FULL.name(), user.getAlternateEmailFormat());
        assertEquals(new Boolean(false), user.isAlternateEmailAttachAudio());
    }

    public void testUserTui() {
        MailboxPreferences mailboxPrefs = new MailboxPreferences();
        PermissionManagerImpl pm = new PermissionManagerImpl();
        pm.setModelFilesContext(TestHelper.getModelFilesContext());
        User user = new User();
        user.setPermissionManager(pm);

        mailboxPrefs.setBusyPrompt("true");
        mailboxPrefs.updateUser(user);
        assertEquals(VoicemailTuiType.STANDARD.getValue(), user.getSettingValue(VOICEMAIL_TUI));
        assertEquals("true", user.getSettingValue(BUSY_PROMPT));

        mailboxPrefs.setBusyPrompt("false");
        mailboxPrefs.setVoicemailTui(VoicemailTuiType.CALLPILOT);
        mailboxPrefs.updateUser(user);
        assertEquals(VoicemailTuiType.CALLPILOT.getValue(), user.getSettingValue(VOICEMAIL_TUI));
        assertEquals("false", user.getSettingValue(BUSY_PROMPT));
    }

}

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sipfoundry.sipxconfig.common.User;

public class MailboxPreferences {
    public static final String EMPTY = "";

    public static final String ACTIVE_GREETING = "voicemail/mailbox/active-greeting";
    public static final String UNIFIED_MESSAGING_LANGUAGE = "voicemail/mailbox/language";
    public static final String BUSY_PROMPT = "voicemail/mailbox/user-busy-prompt";
    public static final String VOICEMAIL_TUI = "voicemail/mailbox/voicemail-tui";
    public static final String EXTERNAL_MWI = "voicemail/mailbox/external-mwi";
    public static final String FORWARD_DELETE_VOICEMAIL = "voicemail/mailbox/forward-delete-voicemail";

    public static final String PRIMARY_EMAIL_NOTIFICATION = "voicemail/mailbox/primary-email-voicemail-notification";
    public static final String PRIMARY_EMAIL_FORMAT = "voicemail/mailbox/primary-email-format";
    public static final String PRIMARY_EMAIL_ATTACH_AUDIO = "voicemail/mailbox/primary-email-attach-audio";
    public static final String ALT_EMAIL_NOTIFICATION = "voicemail/mailbox/alternate-email-voicemail-notification";
    public static final String ALT_EMAIL_FORMAT = "voicemail/mailbox/alternate-email-format";
    public static final String ALT_EMAIL_ATTACH_AUDIO = "voicemail/mailbox/alternate-email-attach-audio";

    public enum ActiveGreeting {
        NONE("none"), STANDARD("standard"), OUT_OF_OFFICE("outofoffice"), EXTENDED_ABSENCE("extendedabsence");

        private static final Set<String> IDS = new HashSet<String>();

        private final String m_id;

        ActiveGreeting(String id) {
            m_id = id;
        }

        public String getId() {
            return m_id;
        }

        static {
            for (ActiveGreeting e : ActiveGreeting.values()) {
                IDS.add(e.getId());
            }
            IDS.add(EMPTY);
        }

        public static ActiveGreeting fromId(String id) {
            for (ActiveGreeting greeting : ActiveGreeting.values()) {
                if (greeting.getId().equals(id)) {
                    return greeting;
                }
            }
            return NONE;
        }

        public static boolean isValid(String s) {
            return IDS.contains(s);
        }
    }

    public enum AttachType {
        NO("0"), YES("1");

        private static final Set<String> VALUES = new HashSet<String>();

        private String m_value;

        AttachType(String value) {
            m_value = value;
        }

        public String getValue() {
            return m_value;
        }

        static {
            for (AttachType e : AttachType.values()) {
                VALUES.add(e.getValue());
            }
            VALUES.add(EMPTY);
        }

        public static AttachType fromValue(String value) {
            for (AttachType e : values()) {
                if (e.m_value.equals(value)) {
                    return e;
                }
            }
            return NO;
        }

        public static boolean isValid(String s) {
            return VALUES.contains(s);
        }
    }

    public enum VoicemailTuiType {
        STANDARD("stdui"), CALLPILOT("cpui");

        private String m_value;

        VoicemailTuiType(String value) {
            m_value = value;
        }

        public String getValue() {
            return m_value;
        }

        public static VoicemailTuiType fromValue(String value) {
            for (VoicemailTuiType e : values()) {
                if (e.m_value.equals(value)) {
                    return e;
                }
            }
            return STANDARD;
        }
    }

    public enum MailFormat {
        FULL, MEDIUM, BRIEF;

        private static final Set<String> VALUES = new HashSet<String>();

        static {
            for (MailFormat e : MailFormat.values()) {
                VALUES.add(e.toString());
            }
            VALUES.add(EMPTY);
        }

        public static boolean isValid(String s) {
            return VALUES.contains(s);
        }
    }

    private ActiveGreeting m_activeGreeting = ActiveGreeting.NONE;
    private String m_language;
    private String m_busyPrompt;
    private VoicemailTuiType m_voicemailTui = VoicemailTuiType.STANDARD;
    private String m_externalMwi;
    private boolean m_forwardDeleteVoicemail;

    private String m_emailAddress;
    private MailFormat m_emailFormat = MailFormat.FULL;
    private AttachType m_attachVoicemailToEmail = AttachType.NO;
    private boolean m_includeAudioAttachment;

    private String m_alternateEmailAddress;
    private MailFormat m_alternateEmailFormat = MailFormat.FULL;
    private AttachType m_voicemailToAlternateEmailNotification = AttachType.NO;
    private boolean m_includeAudioAttachmentAlternateEmail;

    public MailboxPreferences() {
        // empty
    }

    public MailboxPreferences(User user) {
        m_emailAddress = user.getEmailAddress();
        m_alternateEmailAddress = user.getAlternateEmailAddress();
        m_activeGreeting = ActiveGreeting.fromId(user.getActiveGreeting());
        m_language = user.getSettingValue(UNIFIED_MESSAGING_LANGUAGE);
        m_busyPrompt = user.getSettingValue(BUSY_PROMPT);
        m_voicemailTui = VoicemailTuiType.fromValue(user.getSettingValue(VOICEMAIL_TUI));
        m_externalMwi = user.getSettingValue(EXTERNAL_MWI);
        m_forwardDeleteVoicemail = (Boolean) user.getSettingTypedValue(FORWARD_DELETE_VOICEMAIL);
        m_attachVoicemailToEmail = AttachType.fromValue(user.getPrimaryEmailNotification());
        m_emailFormat = MailFormat.valueOf(user.getPrimaryEmailFormat());
        m_includeAudioAttachment = (Boolean) user.isPrimaryEmailAttachAudio();
        m_voicemailToAlternateEmailNotification = AttachType.fromValue(user.getAlternateEmailNotification());
        m_alternateEmailFormat = MailFormat.valueOf(user.getAlternateEmailFormat());
        m_includeAudioAttachmentAlternateEmail = (Boolean) user.isAlternateEmailAttachAudio();
    }

    public void updateUser(User user) {
        user.setEmailAddress(m_emailAddress);
        user.setAlternateEmailAddress(m_alternateEmailAddress);
        user.setActiveGreeting(m_activeGreeting.getId());
        user.setSettingValue(UNIFIED_MESSAGING_LANGUAGE, m_language);
        user.setSettingValue(BUSY_PROMPT, m_busyPrompt);
        user.setSettingValue(VOICEMAIL_TUI, m_voicemailTui.getValue());
        user.setSettingValue(EXTERNAL_MWI, m_externalMwi);
        user.setSettingTypedValue(FORWARD_DELETE_VOICEMAIL, m_forwardDeleteVoicemail);
        user.setPrimaryEmailNotification(m_attachVoicemailToEmail.getValue());
        user.setPrimaryEmailFormat(m_emailFormat.name());
        user.setPrimaryEmailAttachAudio(m_includeAudioAttachment);
        user.setAlternateEmailNotification(m_voicemailToAlternateEmailNotification.getValue());
        user.setAlternateEmailFormat(m_alternateEmailFormat.name());
        user.setAlternateEmailAttachAudio(m_includeAudioAttachmentAlternateEmail);
    }

    public ActiveGreeting getActiveGreeting() {
        return m_activeGreeting;
    }

    public void setActiveGreeting(ActiveGreeting activeGreeting) {
        m_activeGreeting = activeGreeting;
    }

    public String getLanguage() {
        return m_language;
    }

    public void setLanguage(String language) {
        m_language = language;
    }

    public String getBusyPrompt() {
        return m_busyPrompt;
    }

    public void setBusyPrompt(String busyPrompt) {
        m_busyPrompt = busyPrompt;
    }

    public VoicemailTuiType getVoicemailTui() {
        return m_voicemailTui;
    }

    public void setVoicemailTui(VoicemailTuiType voicemailTui) {
        m_voicemailTui = voicemailTui;
    }

    public String getExternalMwi() {
        return m_externalMwi;
    }

    public void setExternalMwi(String externalMwi) {
        m_externalMwi = externalMwi;
    }

    public AttachType getAttachVoicemailToEmail() {
        return m_attachVoicemailToEmail;
    }

    public void setAttachVoicemailToEmail(AttachType attachVoicemailToEmail) {
        m_attachVoicemailToEmail = attachVoicemailToEmail;
    }

    public MailFormat getEmailFormat() {
        return m_emailFormat;
    }

    public void setEmailFormat(MailFormat emailFormat) {
        m_emailFormat = emailFormat;
    }

    public String getEmailAddress() {
        return m_emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        m_emailAddress = emailAddress;
    }

    public String getAlternateEmailAddress() {
        return m_alternateEmailAddress;
    }

    public void setAlternateEmailAddress(String alternateEmailAddress) {
        m_alternateEmailAddress = alternateEmailAddress;
    }

    public AttachType getVoicemailToAlternateEmailNotification() {
        return m_voicemailToAlternateEmailNotification;
    }

    public void setVoicemailToAlternateEmailNotification(AttachType voicemailToAlternateEmailNotification) {
        m_voicemailToAlternateEmailNotification = voicemailToAlternateEmailNotification;
    }

    public boolean isSynchronizeWithImapServer() {
        // return m_attachVoicemailToEmail == AttachType.IMAP;
        return false;
    }

    public boolean isEmailNotificationEnabled() {
        return m_attachVoicemailToEmail == AttachType.YES;
    }

    public boolean isEmailNotificationAlternateEnabled() {
        return m_voicemailToAlternateEmailNotification == AttachType.YES;
    }

    public boolean isIncludeAudioAttachment() {
        return m_includeAudioAttachment;
    }

    public void setIncludeAudioAttachment(boolean includeAudioAttachment) {
        m_includeAudioAttachment = includeAudioAttachment;
    }

    public MailFormat getAlternateEmailFormat() {
        return m_alternateEmailFormat;
    }

    public void setAlternateEmailFormat(MailFormat emailFormat) {
        m_alternateEmailFormat = emailFormat;
    }

    public boolean isIncludeAudioAttachmentAlternateEmail() {
        return m_includeAudioAttachmentAlternateEmail;
    }

    public void setIncludeAudioAttachmentAlternateEmail(boolean audioAttachmentAlternateEmail) {
        m_includeAudioAttachmentAlternateEmail = audioAttachmentAlternateEmail;
    }

    public boolean isForwardDeleteVoicemail() {
        return m_forwardDeleteVoicemail;
    }

    public void setForwardDeleteVoicemail(boolean forwardDeleteVoicemail) {
        m_forwardDeleteVoicemail = forwardDeleteVoicemail;
    }

    public boolean isImapServerConfigured() {
        // return StringUtils.isNotEmpty(getImapHost()) && getImapPort() != null;
        return false;
    }

    public ActiveGreeting[] getOptionsForActiveGreeting(boolean isStandardTui) {
        List list = new ArrayList();
        if (isStandardTui) {
            list.add(ActiveGreeting.NONE);
            list.add(ActiveGreeting.STANDARD);
            list.add(ActiveGreeting.OUT_OF_OFFICE);
            list.add(ActiveGreeting.EXTENDED_ABSENCE);
        } else {
            list.add(ActiveGreeting.STANDARD);
            list.add(ActiveGreeting.OUT_OF_OFFICE);
        }
        return (ActiveGreeting[]) list.toArray(new ActiveGreeting[0]);
    }

    public AttachType[] getAttachOptions(boolean isAdmin) {
        if (isImapServerConfigured() || isAdmin) {
            return AttachType.values();
        }
        return new AttachType[] {
            AttachType.NO, AttachType.YES
        };

    }

    public AttachType[] getAttachOptionsForAlternateEmail() {
        return new AttachType[] {
            AttachType.NO, AttachType.YES
        };
    }

    public VoicemailTuiType[] getOptionsForVoicemailTui(String promptDir) {
        List list = new ArrayList();
        list.add(VoicemailTuiType.STANDARD);
        // Check that the voicemail stdprompts directory is available
        if (promptDir != null) {
            // Check if the optional scs-callpilot-prompts package is installed
            String cpPromptDir = promptDir + "/cpui";
            if ((new File(cpPromptDir)).exists()) {
                list.add(VoicemailTuiType.CALLPILOT);
            }
        }
        // Add any additional voicemail prompts packages here
        return (VoicemailTuiType[]) list.toArray(new VoicemailTuiType[0]);
    }
}

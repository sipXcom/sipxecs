/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxivr.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.Collect;
import org.sipfoundry.commons.freeswitch.Localization;
import org.sipfoundry.commons.freeswitch.PromptList;
import org.sipfoundry.commons.freeswitch.Set;
import org.sipfoundry.commons.freeswitch.Speak;
import org.sipfoundry.commons.userdb.User;
import org.sipfoundry.commons.userdb.ValidUsers;
import org.sipfoundry.sipxivr.ApplicationConfiguraton;
import org.sipfoundry.sipxivr.common.IvrChoice.IvrChoiceReason;
import org.sipfoundry.voicemail.mailbox.MailboxManager;
import org.springframework.beans.factory.annotation.Required;

public class DialByName {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");

    // Global store for DialByNamePrompts resource bundles keyed by locale
    private static final String RESOURCE_NAME = "org.sipfoundry.sipxivr.common.DialByNamePrompts";

    private ValidUsers m_validUsers;
    private Localization m_loc;
    private ApplicationConfiguraton m_config;
    private boolean m_OnlyVoicemailUsers;   // Limit users to those who have voicemail permssions
    private MailboxManager m_mailboxManager;
    private boolean m_tts;

    /**
     * The Dial by Name dialog.
     * 
     * Prompts the caller to enter digits that "spell" the name of the user.
     * Collects the digits, then calls selectChoice() to see if it matches
     * any of the valid user names.
     * 
     * @return User selected by dialog, else null
     */
    public DialByNameChoice dialByName() {
        return dialByName(null);
    }

    public DialByNameChoice dialByName(String groups) {
        int timeoutCount = 0;
        for (;;) {
            
            if (timeoutCount > m_config.getNoInputCount()) {
                return new DialByNameChoice(new IvrChoice("", IvrChoiceReason.TIMEOUT));

            }

            // Probably should be:
            // "Please spell the first or last name of the person using as many letters as you know.
            // "You'll be prompted when the list of names is narrowed enough.
            // "Press seven for 'q' and nine for 'z' To cancel, press star"
            
            // "Please spell the name of the person.
            // "Press seven for 'q' and nine for 'z' To cancel, press star"
            m_loc.play("dial_by_name", "0123456789*");
            String digits = "";
            for(;;) {
                // Collect the digits from the caller.  Use the "*" and "#" keys as terminators
                // There are a LONG (10 second) digit timers here, as spelling on the phone
                // is difficult!  The "#" key will terminate any input if the caller is finished.
                Collect c = new Collect(m_loc.getFreeSwitchEventSocketInterface(), 3, 10000, 1000, 1000);
                c.setTermChars("*#");
                c.go();
                String digit = c.getDigits();
                LOG.info("DialByName::dialByName Collected digits=" + digit);
                if (digit.length() == 0) {
                    break ; // Timeout
                }
                if (digit.contentEquals("*")) {
                    digits = "*";
                    break; // Cancel
                } 
                if (digit.contentEquals("#")) {
                    break ; // "enter" key
                }
                digits += digit;
                List<User> matches = m_validUsers.lookupDTMF(digits, m_OnlyVoicemailUsers, groups);
                LOG.info(String.format("DialByName::dialByName %s matchs %d users", digits, matches.size()));
                if (matches.size() < 3) {
                    break ; // Less than 3 (including none!) time to leave
                }
                if (matches.size() < 10 && digits.length() > 6) {
                    break ; // Less than 10 after 7 or more digits?  time to leave.
                }
            }

            // Timed out.  (No digits)
            if (digits.length() == 0) {
                ++timeoutCount;
                continue;
            }
            
            // Reset the timeout counter, they entered something
            timeoutCount = 0 ;

            if (digits.contentEquals("*")) {
                // "Canceled."
                m_loc.play("canceled", "");
                return new DialByNameChoice(new IvrChoice("*", IvrChoiceReason.CANCELED));

            }

            if (digits.contentEquals("0") || digits.contentEquals("1")) {
                // "That extension is not valid"
                m_loc.play("invalid_try_again", "");
                continue;
            }
                        
            // See if the digits they dialed matched anyone, and let them select among
            // all the possibilities
            DialByNameChoice choice = selectChoice(digits, groups);

            if (choice.getIvrChoiceReason() == IvrChoiceReason.CANCELED) {
                // They canceled the selectChoice menu, back to dialByName
                continue ;
            }
            
            if (choice.getIvrChoiceReason() == IvrChoiceReason.SUCCESS && 
                    choice.getUsers() == null) {
                // Matched no names
                continue;
            }
            return choice;
        }
    }

    /**
     * Given the dialed digits "spelling" a user name, create a menu of matching users (up to 9)
     * and have the caller enter one of the selections.
     * 
     * @param digits
     * @return The selected user, null if none
     */
    private DialByNameChoice selectChoice(String digits, String groups){
        // Lookup the list of validUsers that match the DTMF digits
        List<User> matches = m_validUsers.lookupDTMF(digits, false, groups);
        if (matches.size() != 0) {
            matches = m_validUsers.lookupDTMF(digits, m_OnlyVoicemailUsers, groups);
            if (matches.size() == 0) {
                // Indicate dose match but user has no voice mail permission
                m_loc.play("invalid_extension", "");
                return new DialByNameChoice(new IvrChoice(digits, IvrChoiceReason.SUCCESS));
            }
        } else {
            // Indicate no match
            // "No name in the directory matches the name you entered."
            m_loc.play("dial_by_name_nomatch", "");
            // create a SUCCESS return as they didn't timeout or enter bad data
            // but the user is null
            return new DialByNameChoice(new IvrChoice(digits, IvrChoiceReason.SUCCESS));
        }

        /*
         * This is an enhancement over the original vxml which will prompt even if only one
         * matches. if (matches.size() == 1) { User u = matches.firstElement() ; transfer(u.uri);
         * return true ; }
         */

        // Build a menu of the matched user's names.
        // Limit the choices to the first 9 (or it gets too long)
        List<PlayStructure> playStructureList = new ArrayList<PlayStructure>();
        StringBuilder digitMask = new StringBuilder();
        int choices = matches.size();
        if (choices > 9) {
            choices = 9;
        }
        for (int i = 0; i < choices; i++) {
            String digit = Integer.toString(i+1);
            PromptList pl = new PromptList(m_loc);
            String name = null;
            User u = matches.get(i);
            // Try to speak the user's recorded name
            File nameFile = m_mailboxManager.getRecordedName(u.getUserName());
            String namePrompts = null;
            if (nameFile.exists()) {
                namePrompts = nameFile.getPath();
            } else {
                if ((StringUtils.isEmpty(u.getFirstName()) && StringUtils.isEmpty(u.getLastName())) || !m_tts) {
                    PromptList ext = new PromptList(m_loc);
                    // "Extension {extension}"
                    ext.addFragment("extension", u.getUserName());
                    namePrompts = ext.toString();
                } else {
                    name = new StringBuilder(u.getFirstName()).append(",").append(u.getLastName()).toString();
                }
            }
            LOG.debug(String.format("DialByName::selectChoice menu %s for %s", digit, u.getUserName()));           
            // "Press {number} for {name}"
            if (namePrompts != null) {
                pl.addFragment("press_n_for", digit, namePrompts);
            } else {
                pl.addFragment("press_n_for", digit);
            }
            digitMask.append(digit);
            PlayStructure playStructure = new PlayStructure();
            playStructure.setPromptList(pl);
            playStructure.setName(name);
            playStructureList.add(playStructure);
        }

        // Dialog for the caller to enter one of the choices
        int timeoutCount = 0;
        for (;;) {
            if (timeoutCount > m_config.getNoInputCount()) {
                return new DialByNameChoice(new IvrChoice("", IvrChoiceReason.FAILURE));
            }
            
            // Play the menu
            for (PlayStructure playStructure : playStructureList) {
                m_loc.play(playStructure.getPromptList(), digitMask+"*");
                if (!StringUtils.isEmpty(playStructure.getName())) {
                    new Set(m_loc.getFreeSwitchEventSocketInterface(), "tts_engine", "flite").go();
                    new Set(m_loc.getFreeSwitchEventSocketInterface(), "tts_voice", "slt").go();
                    new Speak(m_loc.getFreeSwitchEventSocketInterface(), playStructure.getName()).go();
                    new Set(m_loc.getFreeSwitchEventSocketInterface(), "playback_terminators", "#").go();
                }
            }
            
            // "To cancel and enter a different name, press *."
            PromptList pl = new PromptList(m_loc);
            pl.addFragment("dial_by_name_enter_different_name");
            m_loc.play(pl, digitMask+"*");
            
            // Wait for the caller to enter a digit
            Collect c = new Collect(m_loc.getFreeSwitchEventSocketInterface(), 1, 
                    m_config.getInitialTimeout(), 0, 0);
            c.setTermChars("*#");
            c.go();
            String choice = c.getDigits();
            LOG.info("DialByName::selectChoice Collected digits=" + choice);

            // See what they entered
            if (choice.length() == 0) {
                timeoutCount++;
                continue;
            }

            // Reset timeout counter, they entered something
            timeoutCount = 0;

            if (choice.contentEquals("*")) {
                // "Canceled."
                m_loc.play("canceled", "");
                return new DialByNameChoice(new IvrChoice("*", IvrChoiceReason.CANCELED));

            }

            if (choice.contentEquals("#")) {
                // "No entry matches that selection."
                m_loc.play("no_entry_matches", "");
                continue;
            }

            if ("123456789".contains(choice)) {
                int selected = Integer.parseInt(choice);
                if (selected <= choices) {
                    User u = matches.get(selected - 1);
                    LOG.info(String.format("DialByName::selectChoice returns extension %s (%s)", u.getUserName(), u.getUri()));
                    return new DialByNameChoice(u, choice, IvrChoiceReason.SUCCESS);
                }
            }

            // "No entry matches that selection."
            m_loc.play("no_entry_matches", "");
            continue;
        }
    }

    /**
     * If true, allow only users who have voicemail permission and are in the directory.
     * If false, allow only users who are in the directory.
     * @param onlyVoicemailUsers
     */
    public void setOnlyVoicemailUsers(boolean onlyVoicemailUsers) {
        m_OnlyVoicemailUsers = true;
    }

    public void setValidUsers(ValidUsers validUsers) {
        m_validUsers = validUsers;
    }

    public void setLocalization(Localization parentLocalization) {
        m_loc = new Localization(RESOURCE_NAME, parentLocalization);
    }

    public void changeLocale(String localeString) {
        m_loc.changeLocale(localeString);
    }

    public void setApplicationConfiguration(ApplicationConfiguraton config) {
        m_config = config;
    }

    public void setMailboxManager(MailboxManager mgr) {
        m_mailboxManager = mgr;
    }

    @Required
    public void setTts(boolean tts) {
        m_tts = tts;
    }

    private class PlayStructure {
        private PromptList m_promptList;
        private String m_name;
        
        public PromptList getPromptList() {
            return m_promptList;
        }
        public void setPromptList(PromptList promptList) {
            m_promptList = promptList;
        }
        public String getName() {
            return m_name;
        }
        public void setName(String name) {
            m_name = name;
        }
    }
}

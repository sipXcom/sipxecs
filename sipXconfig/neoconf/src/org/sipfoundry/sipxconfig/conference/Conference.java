/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.conference;

import static org.sipfoundry.commons.mongo.MongoConstants.CONF_AUTORECORD;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_DESCRIPTION;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_ENABLED;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_EXT;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_MEMBERS_ONLY;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_MODERATED;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_NAME;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_OWNER;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_PIN;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_PUBLIC;
import static org.sipfoundry.commons.mongo.MongoConstants.CONF_URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.commserver.imdb.AliasMapping;
import org.sipfoundry.sipxconfig.commserver.imdb.DataSet;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettings;
import org.sipfoundry.sipxconfig.setting.ProfileNameHandler;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.sipxconfig.setting.SettingValue;
import org.sipfoundry.sipxconfig.setting.SettingValueImpl;

public class Conference extends BeanWithSettings implements Replicable, DeployConfigOnEdit {
    public static final String BEAN_NAME = "conferenceConference";

    /**
     * default lengths of autogenerated access code
     */
    public static final int CODE_LEN = 4;
    public static final int SECRET_LEN = 9;

    // settings names
    public static final String ORGANIZER_CODE = "fs-conf-conference/organizer-code";
    public static final String PARTICIPANT_CODE = "fs-conf-conference/participant-code";
    public static final String REMOTE_ADMIT_SECRET = "fs-conf-conference/remote-admin-secret";
    public static final String AUTO_RECORDING = "fs-conf-conference/autorecord";
    public static final String AOR_RECORD = "fs-conf-conference/AOR";
    public static final String MAX_LEGS = "fs-conf-conference/MAX_LEGS";
    public static final String MOH = "fs-conf-conference/MOH";
    public static final String MOH_SOUNDCARD_SOURCE = "SOUNDCARD_SRC";
    public static final String MOH_FILES_SOURCE = "FILES_SRC";
    public static final String MODERATOR_CODE = "fs-conf-conference/moderator-code";
    public static final String TERMINATE_ON_MODERATOR_EXIT = "fs-conf-conference/terminate-on-moderator-exit";
    public static final String QUICKSTART = "fs-conf-conference/quickstart";
    public static final String VIDEO = "fs-conf-conference/video";
    public static final String VIDEO_TOGGLE_FLOOR = "fs-conf-conference/video-toogle-floor";
    public static final String MODERATED_ROOM = "chat-meeting/moderated";
    public static final String PUBLIC_ROOM = "chat-meeting/public";

    private static final String ALIAS_RELATION = "conference";
    private boolean m_enabled;
    private String m_name;
    private String m_description;
    private String m_extension;
    private String m_did;
    private Bridge m_bridge;
    private User m_owner;
    private String m_organizerCode;
    private String m_remoteSecretAgent;
    private String m_participantCode;
    private AddressManager m_addressManager;
    private final boolean m_aloneSound = true;

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(this);
        getSettingModel2().setDefaultProfileNameHandler(new ConferenceProfileName(this));
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxconference/conference.xml");
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public boolean isEnabled() {
        return m_enabled;
    }

    public void setEnabled(boolean enabled) {
        m_enabled = enabled;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public void setName(String name) {
        m_name = name;
    }

    public Bridge getBridge() {
        return m_bridge;
    }

    public void setBridge(Bridge bridge) {
        m_bridge = bridge;
    }

    public User getOwner() {
        return m_owner;
    }

    public void setOwner(User owner) {
        m_owner = owner;
    }

    public boolean isAutorecorded() {
        if (m_owner == null) {
            // Can't auto-record if we don't have a conference owner.
            return false;
        }
        return (Boolean) getSettingTypedValue(AUTO_RECORDING);
    }

    public void setAutorecorded(Boolean autorecord) {
        setSettingTypedValue(AUTO_RECORDING, autorecord);
    }

    public String getExtension() {
        return m_extension;
    }

    public void setExtension(String extension) {
        m_extension = extension;
    }

    public String getOrganizerAccessCode() {
        return getSettingValue(ORGANIZER_CODE);
    }

    public String getParticipantAccessCode() {
        return getSettingValue(PARTICIPANT_CODE);
    }

    public void setParticipantAccessCode(String code) {
        setSettingTypedValue(PARTICIPANT_CODE, code);
    }

    public String getModeratorAccessCode() {
        return getSettingValue(MODERATOR_CODE);
    }

    public void setModeratorAccessCode(String code) {
        setSettingTypedValue(MODERATOR_CODE, code);
    }

    public boolean isQuickstart() {
        return (Boolean) getSettingTypedValue(QUICKSTART);
    }

    public void setQuickstart(Boolean quickstart) {
        setSettingTypedValue(QUICKSTART, quickstart);
    }

    public String getMohSource() {
        return (String) getSettingTypedValue(MOH);
    }

    public void setMohSource(String source) {
        setSettingTypedValue(MOH, source);
    }

    public boolean isMohPortAudioEnabled() {
        return getSettingValue(MOH).equals(MOH_SOUNDCARD_SOURCE);
    }

    public boolean isVideoConference() {
        return (Boolean) getSettingTypedValue(VIDEO);
    }

    public void setVideoConference(Boolean video) {
        setSettingTypedValue(VIDEO, video);
    }

    public boolean isVideoToggleFloor() {
        return (Boolean) getSettingTypedValue(VIDEO_TOGGLE_FLOOR);
    }

    public void setVideoToggleFloor(Boolean videoToggleFloor) {
        setSettingTypedValue(VIDEO_TOGGLE_FLOOR, videoToggleFloor);
    }

    public boolean isMohFilesSrcEnabled() {
        return getSettingValue(MOH).equals(MOH_FILES_SOURCE);
    }

    public boolean isModeratedRoom() {
        return (Boolean) getSettingTypedValue(MODERATED_ROOM);
    }

    public void setModeratedRoom(Boolean moderatedRoom) {
        setSettingTypedValue(MODERATED_ROOM, moderatedRoom);
    }

    public boolean isPublicRoom() {
        return (Boolean) getSettingTypedValue(PUBLIC_ROOM);
    }

    public void setPublicRoom(Boolean publicRoom) {
        setSettingTypedValue(PUBLIC_ROOM, publicRoom);
    }

    public String getUri() {
        return getSettingValue(AOR_RECORD);
    }

    public boolean hasOwner() {
        return m_owner != null;
    }

    public Setting getConfigSettings() {
        return getSettings().getSetting("fs-conf-conference");
    }

    public String getConferenceFlags() {
        StringBuilder flags = new StringBuilder();
        flags.append("waste-bandwidth");
        if (isVideoConference()) {
            flags.append(" | video-bridge");
        }
        if ((Boolean) getSettingTypedValue("fs-conf-conference/video-toogle-floor")) {
            flags.append(" | video-floor-only");
        }
        if (!(Boolean) getSettingTypedValue(QUICKSTART)) {
            flags.append(" | wait-mod");
        }
        return flags.toString();
    }

    /**
     * builds the dial string from java rather than vm
     */
    public String getDialString() {
        StringBuilder dialString = new StringBuilder(getName());
        dialString.append("@");
        dialString.append(getExtension());
        if (StringUtils.isNotBlank(getParticipantAccessCode())) {
            dialString.append("+").append(getParticipantAccessCode());
        }
        return dialString.toString();
    }

    @Override
    public void setSettingValue(String path, String value) {
        if (AOR_RECORD.equals(path)) {
            throw new UnsupportedOperationException("cannot change AOR_RECORD");
        }
        super.setSettingValue(path, value);
    }

    void generateRemoteAdmitSecret() {
        m_remoteSecretAgent = RandomStringUtils.randomAlphanumeric(SECRET_LEN);
    }

    void generateAccessCodes() {
        m_organizerCode = RandomStringUtils.randomNumeric(CODE_LEN);
        m_participantCode = RandomStringUtils.randomNumeric(CODE_LEN);
    }

    @SettingEntry(path = AOR_RECORD)
    public String getAorRecord() {
        Address fs = m_addressManager.getSingleAddress(FreeswitchFeature.SIP_ADDRESS);
        String host;
        if (m_bridge != null) {
            host = m_bridge.getHost();
        } else {
            host = fs.getAddress();
        }
        return SipUri.format(StringUtils.defaultString(m_name), host, fs.getPort());
    }

    @SettingEntry(path = PARTICIPANT_CODE)
    public String getParticipantCode() {
        return m_participantCode;
    }

    @SettingEntry(path = ORGANIZER_CODE)
    public String getOrganizerCode() {
        return m_organizerCode;
    }

    @SettingEntry(path = REMOTE_ADMIT_SECRET)
    public String getRemoteAdmitSecret() {
        return m_remoteSecretAgent;
    }

    public boolean isAloneSound() {
        return m_aloneSound;
    }

    public Integer getConfMaxMembers() {
        return (Integer) getSettingTypedValue(MAX_LEGS);
    }

    public void setConfMaxMembers(Integer maxMembers) {
        setSettingTypedValue(MAX_LEGS, maxMembers);
    }

    public static class ConferenceProfileName implements ProfileNameHandler {
        private static final char SEPARATOR = '.';
        private final Conference m_conference;

        ConferenceProfileName(Conference conference) {
            m_conference = conference;
        }

        @Override
        public SettingValue getProfileName(Setting setting) {
            String nameToken = SEPARATOR + m_conference.getName();
            String profileName = setting.getProfileName();
            StringBuffer buffer = new StringBuffer(profileName);
            int dotIndex = profileName.indexOf(SEPARATOR);
            if (dotIndex > 0) {
                buffer.insert(dotIndex, nameToken);
            } else {
                buffer.append(nameToken);
            }

            return new SettingValueImpl(buffer.toString());
        }
    }

    /**
     * Generates two alias mappings for a conference:
     *
     * extension@domain ==> name@domainm and name@domain ==>> media server
     *
     * @param domainName
     *
     * @return list of aliase mappings, empty list if conference is disabled
     */
    private Collection<AliasMapping> generateAliases(String domainName) {
        if (!isEnabled()) {
            return Collections.emptyList();
        }
        Collection<AliasMapping> aliases = new ArrayList<AliasMapping>();
        if (StringUtils.isNotBlank(m_extension) && !m_extension.equals(m_name)) {
            // add extension mapping
            String identityUri = SipUri.format(m_name, domainName, false);
            AliasMapping extensionAlias = new AliasMapping(m_extension, identityUri, ALIAS_RELATION);
            aliases.add(extensionAlias);
        }
        if (StringUtils.isNotBlank(m_did) && !m_did.equals(m_name)) {
            // add extension mapping
            String identityUri = SipUri.format(m_name, domainName, false);
            AliasMapping didAlias = new AliasMapping(m_did, identityUri, ALIAS_RELATION);
            aliases.add(didAlias);
        }
        aliases.add(createFreeSwitchAlias());
        return aliases;
    }

    private AliasMapping createFreeSwitchAlias() {
        String freeswitchUri = getUri();
        return new AliasMapping(m_name, freeswitchUri, ALIAS_RELATION);
    }

    public String getDid() {
        return m_did;
    }

    public void setDid(String did) {
        m_did = did;
    }

    @Override
    public Collection<AliasMapping> getAliasMappings(String domain) {
        Collection<AliasMapping> mappings = generateAliases(domain);
        return mappings;
    }

    @Override
    public Set<DataSet> getDataSets() {
        Set<DataSet> ds = new HashSet<DataSet>();
        ds.add(DataSet.ALIAS);
        return ds;
    }

    @Override
    public String getIdentity(String domain) {
        return SipUri.stripSipPrefix(SipUri.format(null, getExtension(), domain));
    }

    @Override
    public boolean isValidUser() {
        return true;
    }

    @Override
    public Map<String, Object> getMongoProperties(String domain) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(CONF_ENABLED, isEnabled());
        props.put(CONF_EXT, getExtension());
        props.put(CONF_NAME, getName());
        props.put(CONF_DESCRIPTION, getDescription());
        if (getOwner() != null) {
            props.put(CONF_OWNER, getOwner().getUserName());
        } else {
            props.put(CONF_OWNER, StringUtils.EMPTY);
        }
        props.put(CONF_PIN, getParticipantAccessCode());
        props.put(CONF_MODERATED, getSettingValue("chat-meeting/moderated"));
        props.put(CONF_PUBLIC, getSettingValue("chat-meeting/public"));
        props.put(CONF_MEMBERS_ONLY, getSettingValue("chat-meeting/members-only"));
        props.put(CONF_AUTORECORD, isAutorecorded());
        props.put(CONF_URI, getUri());
        return props;
    }

    public void setAddressManager(AddressManager addressManager) {
        m_addressManager = addressManager;
    }

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) FreeswitchFeature.FEATURE, (Feature) ConferenceBridgeContext.FEATURE);
    }

    @Override
    public String getEntityName() {
        return getClass().getSimpleName();
    }

    /**
     * Conference entity must be replicated only when m_enabled is set to true
     */
    @Override
    public boolean isReplicationEnabled() {
        return isEnabled();
    }
}

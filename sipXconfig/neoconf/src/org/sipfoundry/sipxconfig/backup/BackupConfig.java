/**
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.backup;


import static java.lang.String.format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.commons.security.Util;
import org.sipfoundry.sipxconfig.cfgmgt.CfengineModuleConfiguration;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.YamlConfiguration;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureListener;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.ivr.Ivr;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.springframework.beans.factory.annotation.Required;

public class BackupConfig implements ConfigProvider, FeatureListener {
    private static final Log LOG = LogFactory.getLog(BackupConfig.class);

    private static final String RESTORE = "restore";
    private BackupManager m_backupManager;
    private ConfigManager m_configManager;
    private Ivr m_ivr;
    private boolean m_dirty;

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(BackupManager.FEATURE, Ivr.FEATURE) && !m_dirty) {
            return;
        }

        BackupSettings settings = m_backupManager.getSettings();
        Collection<BackupPlan> plans = m_backupManager.getBackupPlans();
        List<Location> hosts = manager.getLocationManager().getLocationsList();

        //save new default backup host if necessary
        m_ivr.saveDefaultIvrBackupHost();

        writeCfConfig(manager.getGlobalDataDirectory(), hosts, settings);

        for (BackupPlan plan : plans) {
            File f = m_backupManager.getPlanFile(plan);
            Writer fout = new FileWriter(f);
            try {
                writeConfig(fout, plan, hosts, settings);
            } finally {
                IOUtils.closeQuietly(fout);
            }

            String datname = format("1/archive-%s.cfdat", plan.getType());
            File dat = new File(m_configManager.getGlobalDataDirectory(), datname);
            Writer datout = new FileWriter(dat);
            try {
                writeBackupSchedules(datout, plan.getType(), plan.getSchedules());
            } finally {
                IOUtils.closeQuietly(datout);
            }

        }
        m_dirty = false;
    }

    void writeCfConfig(File dir, List<Location> hosts, BackupSettings settings) throws IOException {
        Writer cfdat = new FileWriter(new File(dir, "archive.cfdat"));
        try {
            CfengineModuleConfiguration cfg = new CfengineModuleConfiguration(cfdat);
            cfg.writeClass("archive", true);
            for (Location host : hosts) {
                Collection<ArchiveDefinition> possibleDefIds = m_backupManager.getArchiveDefinitions(
                    host, null, settings);
                for (ArchiveDefinition definition : possibleDefIds) {
                    //CFengine does not accept "." characters in variable names
                    //Write definition ids where backup should take place (we can have restore to take place on one node
                    //and backup on other node, therefore we need to check if backup command is not null,
                    //otherwise we might end up with duplicates)
                    //Host might be specified where the backup to run, if service is redundant (voicemail for example)
                    String backupHost = definition.getBackupHost();
                    LOG.debug("BACKUP CONFIG cf - backup host: " + backupHost);
                    if (!StringUtils.isEmpty(definition.getBackupCommand())
                        && (backupHost == null || backupHost.equals(host.getAddress()))) {
                        cfg.write(StringUtils.replace(definition.getId(), ".", "_"),
                            backupHost == null ? host.getAddress() : backupHost);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(cfdat);
        }
    }

    @SuppressWarnings("unchecked")
    void writeConfig(Writer w, BackupPlan plan, Collection<Location> hosts, BackupSettings settings)
        throws IOException {
        final Collection<String> selectedDefIds = plan.getDefinitionIds();
        YamlConfiguration config = new YamlConfiguration(w);
        config.startStruct("hosts");
        final Set<ArchiveDefinition> configuredBackupArchives = new TreeSet<ArchiveDefinition>();
        final Set<ArchiveDefinition> configuredRestoreArchives = new TreeSet<ArchiveDefinition>();
        for (final Location host : hosts) {
            Collection<ArchiveDefinition> possibleDefIds = m_backupManager.getArchiveDefinitions(host, plan, settings);
            final BackupRestore execBackupRestore = new BackupRestore();
            LOG.debug("BACKUP CONFIG - host: " + host.getFqdn() + " possible definitions: " + possibleDefIds);
            Collection<ArchiveDefinition> defIds = CollectionUtils.select(possibleDefIds, new Predicate() {
                @Override
                public boolean evaluate(Object arg0) {
                    ArchiveDefinition def = (ArchiveDefinition) arg0;
                    String defId = def.getId();
                    String backupHost = def.getBackupHost();
                    LOG.debug("BACKUP CONFIG yaml - backup host: " + backupHost);
                    boolean selected = selectedDefIds.contains(defId);
                    LOG.debug("BACKUP CONFIG - is definition selected: " + def.getId() + " " + selected
                        + " already marked for backup " + configuredBackupArchives.contains(def)
                        + " already marked for restore " + configuredRestoreArchives.contains(def));
                    //at least one backup command
                    if (!execBackupRestore.isBackup(defId) && !StringUtils.isEmpty(def.getBackupCommand()) && selected
                        && (!def.isSingleNodeBackup() || !configuredBackupArchives.contains(def)
                        && (backupHost == null || backupHost.equals(host.getAddress())))) {
                        execBackupRestore.setBackup(true, defId);
                        LOG.debug("BACKUP CONFIG - definition is selected for backup");
                    }
                    //at least one restore command
                    if (!execBackupRestore.isRestore(defId) && !StringUtils.isEmpty(def.getRestoreCommand()) && selected
                        && (!def.isSingleNodeRestore() || !configuredRestoreArchives.contains(def))) {
                        execBackupRestore.setRestore(true, defId);
                        LOG.debug("BACKUP CONFIG - definition is selected for restore");
                    }
                    return selected;
                }
            });
            //write host definitions if we have:
            //at least one definition id and at least one backup/restore command to execute
            if (!defIds.isEmpty() && (execBackupRestore.isBackup() || execBackupRestore.isRestore())) {
                writeHostDefinitions(config, host, defIds, execBackupRestore,
                    configuredBackupArchives, configuredRestoreArchives);
            }
        }
        config.endStruct(); //hosts
        writeBackupDetails(w, plan, hosts, settings);
    }

    void writeBackupSchedules(Writer w, BackupType type, Collection<DailyBackupSchedule> schedules) throws IOException {
        CfengineModuleConfiguration config = new CfengineModuleConfiguration(w);
        List<String> crons = new ArrayList<String>();
        for (DailyBackupSchedule schedule : schedules) {
            if (schedule.isEnabled()) {
                crons.add(schedule.toCronString());
            }
        }
        config.writeList(type.toString() + "_backup_schedule", crons);
    }

    void writeHostDefinitions(YamlConfiguration config, Location host, Collection<ArchiveDefinition> defs,
        BackupRestore execBackupRestore,
        Set<ArchiveDefinition> backupDefs, Set<ArchiveDefinition> restoreDefs) throws IOException {

        config.startStruct(host.getId().toString());
        config.write("host", host.getAddress());
        //write backup struct if at least one backup command is available
        if (execBackupRestore.isBackup()) {
            config.startStruct("backup");
            for (ArchiveDefinition def : defs) {
                if (execBackupRestore.isBackup(def.getId())) {
                    if (!def.isSingleNodeBackup() || !backupDefs.contains(def)) {
                        writeCommand(config, def, def.getBackupCommand());
                        backupDefs.add(def);
                    }
                }
            }
            config.endStruct();
        }

        //write restore struct if at least one restore command is available
        if (execBackupRestore.isRestore()) {
            config.startStruct(RESTORE);
            for (ArchiveDefinition def : defs) {
                if (execBackupRestore.isRestore(def.getId())) {
                    if (!def.isSingleNodeRestore() || !restoreDefs.contains(def)) {
                        writeCommand(config, def, def.getRestoreCommand());
                        restoreDefs.add(def);
                    }
                }
            }
            config.endStruct();
        }
        config.endStruct();
    }

    void writeBackupDetails(Writer w, BackupPlan plan, Collection<Location> hosts, BackupSettings settings)
        throws IOException {
        YamlConfiguration config = new YamlConfiguration(w);
        config.write("plan", plan.getType());
        config.write("max", plan.getLimitedCount());
        config.startStruct("settings");
        if (settings != null) {
            if (plan.getType() == BackupType.ftp) {
                String ftp = "ftp";
                String password = "ftp/password";
                Setting passwSetting = settings.getSettings().getSetting(password);
                String[] passwHexValue  = Util.hexUnicodeEscape((String) passwSetting.getTypedValue());
                //YAML accepts only plain ASCII characters - therefore we need to convert to unicode
                if (passwHexValue != null) {
                    passwSetting.setTypedValue(StringUtils.join(passwHexValue, '.'));
                }
                config.startStruct(ftp);
                config.writeSettings(settings.getSettings().getSetting(ftp));
                config.endStruct();
            }
        }

        config.endStruct();
    }

    void writeCommand(YamlConfiguration config, ArchiveDefinition def, String command) throws IOException {
        if (StringUtils.isNotBlank(command)) {
            config.write(def.getId(), command);
        }
    }

    public void setBackupManager(BackupManager backupManager) {
        m_backupManager = backupManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        m_configManager = configManager;
    }

    @Required
    public void setIvr(Ivr ivr) {
        m_ivr = ivr;
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        // enabling/disabling features can impact backup metadata
        m_dirty = true;
    }
    public static class BackupRestore {
        private Map<String, Boolean> m_backup = new HashMap<String, Boolean>();
        private Map<String, Boolean> m_restore = new HashMap<String, Boolean>();

        public boolean isBackup(String defId) {
            Boolean value = m_backup.get(defId);
            return value != null && value;
        }
        public void setBackup(boolean backup, String defId) {
            m_backup.put(defId, backup);
        }
        public boolean isRestore(String defId) {
            Boolean value = m_restore.get(defId);
            return value != null && value;
        }
        public void setRestore(boolean restore, String defId) {
            m_restore.put(defId, restore);
        }
        public boolean isBackup() {
            return !m_backup.isEmpty();
        }
        public boolean isRestore() {
            return !m_restore.isEmpty();
        }
    }
}

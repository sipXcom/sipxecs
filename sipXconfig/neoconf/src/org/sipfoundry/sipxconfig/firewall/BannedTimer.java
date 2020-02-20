package org.sipfoundry.sipxconfig.firewall;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.lang3.StringUtils;
import org.sipfoundry.sipxconfig.apiban.BannedApi;
import org.springframework.beans.factory.annotation.Required;

public class BannedTimer {
    private BannedApi m_bannedApi;
    private FirewallManager m_firewallManager;
    private Integer m_counter = 0;
    private String m_key;

    private static final Log LOG = LogFactory.getLog(BannedTimer.class);

    @Required
    public void setBannedApi(BannedApi bannedApi) {
        m_bannedApi = bannedApi;
    }

    public void saveBannedIps() {
        LOG.debug("APIBAN Key : " + m_key);
        if (m_key == null) {
            return;
        }
        FirewallSettings settings = m_firewallManager.getSettings();
        Integer poolingPeriod = settings.getBannedPoolingPeriod();        
        if (poolingPeriod != 0) {
            if (m_counter < poolingPeriod) {
                m_counter ++;
                LOG.debug("Banned Timer counter " + m_counter);
            } else {
                LOG.debug("Banned Timer counter " + m_counter + " ready to save apiban banned list");
                try {
                    String bannedIps = StringUtils.join(m_bannedApi.getBanned().getIpaddress(), ',');
                    settings.setApibanIps(bannedIps);
                    m_firewallManager.saveSettings(settings);
                } catch (Exception ex) {
                    LOG.error("APIBAN Call failed with: ", ex);
                }
                m_counter = 0;
            }
        }
    }

    @Required
    public void setFirewallManager(FirewallManager firewallManager) {
        m_firewallManager = firewallManager;
    }

    @Required
    public void setKey(String key) {
        m_key = key;
    }        
}

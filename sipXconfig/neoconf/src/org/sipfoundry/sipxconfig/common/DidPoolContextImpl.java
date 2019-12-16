package org.sipfoundry.sipxconfig.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.lang3.StringUtils;
import org.sipfoundry.commons.diddb.DidPool;
import org.sipfoundry.commons.diddb.DidPoolService;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.dialplan.DialingRule;
import org.sipfoundry.sipxconfig.dialplan.UnassignedDidRule;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.springframework.beans.factory.annotation.Required;

public class DidPoolContextImpl implements DidPoolContext {
    
    private FeatureManager m_featureManager;
    private DidPoolService m_didPoolService;
    
    private static final Log LOG = LogFactory.getLog(DidPoolContextImpl.class);

    @Override
    public List< ? extends DialingRule> getDialingRules(Location location) {
        List<Location> locations = m_featureManager.getLocationsForEnabledFeature(FreeswitchFeature.FEATURE);
        List<DialingRule> dialingRules = new ArrayList<DialingRule>();
        if(CollectionUtils.isEmpty(locations)) {
            return dialingRules;
        }
        
        List<DidPool> pools = m_didPoolService.getAllDidPools();
        String start = null;
        String end = null;
        StringBuffer prefix = new StringBuffer();
        Character prefixDigit = null;
        int noDigits = 0;
        for (DidPool pool : pools) {
            start = pool.getStart().replaceAll("[^\\d.]", "");
            end = pool.getEnd().replaceAll("[^\\d.]", "");
            for (int i = 0; i < start.length() && i< end.length(); i++) {
                prefixDigit = start.charAt(i);
                if (prefixDigit.equals(end.charAt(i)) && noDigits == 0) {
                    prefix.append(start.charAt(i));
                } else {
                    noDigits++;
                }
            }
            for (int i = 0; i < end.length() - start.length(); i++) {
                noDigits++;
            }
            UnassignedDidRule rule = new UnassignedDidRule(prefix.toString(), noDigits, locations.get(0).getFqdn(), pool.getRedirectExtension());
            rule.appendToGenerationRules(dialingRules);
            prefix = new StringBuffer();
            prefixDigit = Character.MIN_VALUE;
            noDigits = 0;
        }
        return dialingRules;        
    }    

    @Required
    public void setFeatureManager(FeatureManager manager) {
        m_featureManager = manager;
    }

    @Required
    public void setDidPoolService(DidPoolService didPoolService) {
        m_didPoolService = didPoolService;
    }
}

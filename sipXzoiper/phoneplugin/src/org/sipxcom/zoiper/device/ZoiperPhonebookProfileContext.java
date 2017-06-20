package org.sipxcom.zoiper.device;

import java.util.Collection;
import java.util.Map;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;

public class ZoiperPhonebookProfileContext extends ProfileContext<ZoiperPhone> {
    private final Collection<PhonebookEntry> m_phonebookEntries;

    public ZoiperPhonebookProfileContext(ZoiperPhone device, String profileTemplate, Collection<PhonebookEntry> phonebookEntries) {
        super(device, profileTemplate);
        m_phonebookEntries = phonebookEntries;
    }

    @Override
    public Map<String, Object> getContext() {
        Map<String, Object> context = super.getContext();
        mapDataInContext(context);
        return context;
    }

    public void mapDataInContext(Map<String, Object> context) {
        context.put("phonebookEntries", m_phonebookEntries);
    }

}

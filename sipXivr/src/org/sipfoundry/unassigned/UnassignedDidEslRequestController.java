package org.sipfoundry.unassigned;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.freeswitch.Set;
import org.sipfoundry.commons.freeswitch.Speak;
import org.sipfoundry.commons.freeswitch.eslrequest.AbstractEslRequestController;
import org.springframework.beans.factory.annotation.Required;

public class UnassignedDidEslRequestController extends AbstractEslRequestController {
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");
    
    String m_ttsVoice;
    String m_dialed;
    
    @Override
    public void extractParameters(Hashtable<String, String> parameters) {
        m_dialed = parameters.get("dialed");
    }

    @Override
    public void loadConfig() {
        
    }
    
    public void speak(String text) {                
        new Set(getFsEventSocket(), "tts_engine", "flite").go();
        new Set(getFsEventSocket(), "tts_voice", m_ttsVoice).go();
        new Speak(getFsEventSocket(), text).go();
        new Set(getFsEventSocket(), "playback_terminators", "#").go();        
    }

    @Required
    public void setTtsVoice(String ttsVoice) {
        m_ttsVoice = ttsVoice;
    }

    public String getDialed() {
        return m_dialed;
    }        
}

package org.sipfoundry.unassigned;

import org.apache.log4j.Logger;
import org.sipfoundry.commons.diddb.DidPool;
import org.sipfoundry.commons.diddb.DidPoolService;
import org.sipfoundry.sipxivr.SipxIvrApp;

public class UnassignedDid extends SipxIvrApp {

    private DidPoolService m_didPoolService;
    
    static final Logger LOG = Logger.getLogger("org.sipfoundry.sipxivr");
    
    @Override
    public void run() {
        LOG.info("SipXivr::run Check DID");
        UnassignedDidEslRequestController controller = (UnassignedDidEslRequestController) getEslRequestController();
        String didExt = controller.getDialed();
        
        boolean outsideDid = true;
        for (DidPool pool : m_didPoolService.getAllDidPools()) {
            outsideDid = m_didPoolService.outsideRangeDidValue(pool, Long.valueOf(didExt));
            if (!outsideDid) {                
                break;
            }
        }
        if (outsideDid) {
            controller.speak("Dialed extension " + didExt + " is not part of any DID pool. Extension is unassigned");
        } else {
            controller.speak("Dialed extension " + didExt + " is not active yet");
        }
    }

    public void setDidPoolService(DidPoolService didPoolService) {
        m_didPoolService = didPoolService;
    }
}

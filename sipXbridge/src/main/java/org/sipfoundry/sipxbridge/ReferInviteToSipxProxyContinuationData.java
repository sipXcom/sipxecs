package org.sipfoundry.sipxbridge;

import javax.sip.Dialog;
import javax.sip.message.Request;

/**
 * A private class that stores continuation data when we re-invite the referred party to 
 * determine the SDP codec.
 * 
 * @author M. Ranganathan
 *
 */
final class ReferInviteToSipxProxyContinuationData {

    Request request;
    Dialog dialog;

    public ReferInviteToSipxProxyContinuationData(Request request, Dialog dialog) {
        this.request = request;
        this.dialog = dialog;
    }

}

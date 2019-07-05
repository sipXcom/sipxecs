package org.sipfoundry.commons.diddb;

import org.apache.commons.lang.enums.Enum;

public class DidType extends Enum {
    private static final long serialVersionUID = 1L;
    public static final DidType TYPE_USER = new DidType("USER");
    public static final DidType TYPE_FAX_USER = new DidType("FAX_USER");
    public static final DidType TYPE_HUNT_GROUP = new DidType("HUNT_GROUP");    
    public static final DidType TYPE_LIVE_AUTO_ATTENDANT = new DidType("LIVE_AA");
    public static final DidType TYPE_AUTO_ATTENDANT_DIALING_RULE = new DidType("AA_DIALING_RULE");
    public static final DidType TYPE_VOICEMAIL_DIALING_RULE = new DidType("VM_DIALING_RULE");
    public static final DidType TYPE_CONFERENCE = new DidType("CONFERENCE");
    
    public DidType(String name) {
        super(name);
    }

    public static DidType getEnum(String type) {
        return (DidType) getEnum(DidType.class, type);
    }
}

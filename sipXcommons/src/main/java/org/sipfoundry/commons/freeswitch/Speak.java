package org.sipfoundry.commons.freeswitch;

public class Speak extends CallCommand {
    String m_uuid;

    public boolean start() {
        if(m_uuid == null) {
            return super.start();
        }
        m_finished = false;
        // Send the command to the socket
        m_fses.cmd("sendmsg " + m_uuid +
                "\ncall-command: execute\nexecute-app-name: " + m_command);
        return false;
    }


    public boolean handleEvent(FreeSwitchEvent event) {

        if(m_uuid == null) {
            return super.handleEvent(event);
        }

        return true;
    }
   
    public Speak(FreeSwitchEventSocketInterface fses, String value) {
        super(fses);
        m_uuid = null;
        m_command = String.format("speak\nexecute-app-arg: %s", value);
    }

    public Speak(FreeSwitchEventSocketInterface fses, String chan_uuid, String value) {
        super(fses);
        m_uuid = chan_uuid;
        m_command = "speak\nexecute-app-arg: " + value;
    }
}

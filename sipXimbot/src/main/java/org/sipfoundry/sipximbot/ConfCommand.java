package org.sipfoundry.sipximbot;

import org.sipfoundry.commons.freeswitch.CallCommand;
import org.sipfoundry.commons.freeswitch.FreeSwitchEvent;
import org.sipfoundry.commons.freeswitch.FreeSwitchEventSocketInterface;

public class ConfCommand extends CallCommand {
   String  m_command; // eg. list, lock, mute, deaf, undeaf etc.
   String  m_confName;
   boolean m_success;
   String  m_errString;
    
   public  boolean start() {
       m_finished = false;
       // Send the command to the socket
       m_fses.cmd("api conference " + m_confName + " " + m_command);
       return false;
   }
        
   public ConfCommand(FreeSwitchEventSocketInterface fses, String confName, String confCommand) {         
       super(fses);
       m_confName = confName;
       m_command = confCommand;  
       m_success = false;
   }
   
   public boolean isSucessful() {
       return m_success;
   }
   
   public String GetErrString() {
       return m_errString;
   }
   
   public boolean handleEvent(FreeSwitchEvent event) {

       String content = event.getContent();
       
       if(content != null) {
           if(content.startsWith("OK")) {
               m_finished = true;
               m_success = true;
           }
           
           if(content.startsWith("Non-Existant")) {
               m_errString = "That person does not exist.";
               m_finished = true;
           }
       
           if(content.startsWith("Conference") && content.endsWith("not found\n")) {
               m_errString = "Your conference is not in use.";
               m_finished = true;
           }
           
           if(content.startsWith("-ERR")) {
               m_finished = true;
           }
       }       
       return isFinished();
   }  
}

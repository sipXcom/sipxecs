package org.sipfoundry.voicemail.mailbox;

public interface MailboxManagerMigrator extends MailboxManager {
    
    void migrateFromFlatToMongo();
    
    void migrateFromFlatToMongo(boolean remove);
    
    void removeFromFlat();
    
    void migrateFromMongoToFlat();
    
    void migrateFromMongoToFlat(boolean remove);
    
    void removeFromMongo();
    
}

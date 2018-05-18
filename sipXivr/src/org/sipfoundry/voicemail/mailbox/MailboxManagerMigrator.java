package org.sipfoundry.voicemail.mailbox;

public interface MailboxManagerMigrator extends MailboxManager {
    
    void migrateFromFlatToMongo(String path);
    
    void migrateFromFlatToMongo(String path, boolean remove);
    
    void removeFromFlat();
    
    void migrateFromMongoToFlat(String path);
    
    void migrateFromMongoToFlat(String path, boolean remove);
    
    void removeFromMongo();
    
}

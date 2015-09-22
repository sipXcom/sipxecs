/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.provision.dao;

import org.sipxcom.sipxconfig.pojo.Entity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<Entity, String> {
    public Entity findByUid(String uid);
    public Entity findOneByEntAndModelAndPhLinesContaining(String ent, String model, String uid);
}

/**
 * Copyright (C) 2017 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.commons.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EntityRepository extends MongoRepository<Entity, String> {
    public Entity findByUid(String uid);
    public Entity findOneByEntAndModelAndPhLinesContaining(String ent, String model, String uid);
}

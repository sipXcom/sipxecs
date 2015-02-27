/**
 *
 * Copyright (c) 2015 eZuce, Inc. All rights reserved.
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.dialplan.config;

public interface ForwardingRulesPlugin {
    public final String NOTIFY = "NOTIFY";
    public final String SUBSCRIBE = "SUBSCRIBE";

    String getMethodPattern();

    String getFieldMatch();

    String getFieldPattern();

    String getRouteTo();

    String getFeatureId();

    String getRuriParams();
}

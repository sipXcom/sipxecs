/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.sipxconfig.branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BranchUtils {
    private BranchUtils() {

    }

    public static List<String> getLocationsToReplicate(Set<Branch> allLocations) {
        List<String> locations = new ArrayList<String>();
        for (Branch branch : allLocations) {
            locations.add(String.format("Location%s", branch.getId()));
        }
        return locations;
    }

}

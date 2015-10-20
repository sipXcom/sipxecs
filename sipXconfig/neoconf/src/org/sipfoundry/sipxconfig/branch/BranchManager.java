/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */

package org.sipfoundry.sipxconfig.branch;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.common.DataObjectSource;
import org.sipfoundry.sipxconfig.common.ReplicableProvider;

public interface BranchManager extends DataObjectSource<Branch>, ReplicableProvider {

    public static final String CONFERENCES_BY_BRANCH = "SELECT * from meetme_conference mc JOIN branch_conference bc "
        + "ON mc.meetme_conference_id = bc.meetme_conference_id where bc.branch_id = :branchId";

    public static final String GATEWAYS_BY_BRANCH = "SELECT * from gateway gw where gw.branch_id = :branchId";

    public static final String SHARED_GATEWAYS = "SELECT * from gateway gw where gw.shared='TRUE'";

    public static final String AUTH_CODES_BY_BRANCH = "SELECT * from auth_code ac JOIN branch_auth_code bac "
        + "ON ac.auth_code_id = bac.auth_code_id where bac.branch_id = :branchId";

    public static final String AUTO_ATTENDANDS_BY_BRANCH =
        "SELECT * from auto_attendant aa JOIN branch_auto_attendant baa "
        + "ON aa.auto_attendant_id = baa.auto_attendant_id where baa.branch_id = :branchId";

    public static final String PARKS_BY_BRANCH = "SELECT * from park_orbit po JOIN branch_park_orbit bpo "
        + "ON po.park_orbit_id = bpo.park_orbit_id where bpo.branch_id = :branchId";

    public static final String CALL_GROUP_BY_BRANCH = "SELECT * from call_group cg JOIN branch_call_group bcg "
        + "ON cg.call_group_id = bcg.call_group_id where bcg.branch_id = :branchId";

    public static final String PAGING_GROUP_BY_BRANCH = "SELECT * from paging_group pg JOIN branch_paging_group bpg "
        + "ON pg.paging_group_id = bpg.paging_group_id where bpg.branch_id = :branchId";

    public static final String CALL_QUEUE_BY_BRANCH =
        "SELECT * from freeswitch_extension fe JOIN branch_call_queue bcq "
        + "ON fe.freeswitch_ext_id = bcq.freeswitch_ext_id where bcq.branch_id = :branchId";

    public static final String GROUP_BY_BRANCH =
        "SELECT * from group_storage where resource = 'user' and branch_id = :branchId ";

    public static final String AUTO_ATTENDANT_DIALING_RULES_BY_BRANCH =
        "SELECT * from attendant_dialing_rule adr "
        + "LEFT OUTER JOIN dialing_rule dr "
        + "ON adr.attendant_dialing_rule_id=dr.dialing_rule_id "
        + "JOIN branch_aa_dialing_rule badr "
        + "ON adr.attendant_dialing_rule_id = badr.attendant_dialing_rule_id where badr.branch_id = :branchId";

    Branch getBranch(String name);

    Branch getBranch(Integer branchId);

    Branch retrieveBranch(Integer branchId);

    void saveBranch(Branch branch);

    void deleteBranches(Collection<Integer> allSelected);

    List<Branch> getBranches();

    List<Branch> loadBranchesByPage(final int firstRow, final int pageSize, final String[] orderBy,
            final boolean orderAscending);

    /**
     * returns features that apply to given branchId
     * */
    List<?> getFeatureNames(Integer branchId, String sqlQuery, Class<?> c);

    /**
     * returns features that apply to all branches
     * */
    List<?> getFeatureNames(String sqlQuery, Class<?> c);

    void clear();
}

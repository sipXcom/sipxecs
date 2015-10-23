/*
 * Copyright (c) 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

#ifndef ENTITYRECORD_H
#define	ENTITYRECORD_H

#include <string>
#include <vector>
#include <set>
#include <boost/shared_ptr.hpp>
#include "sipdb/MongoDB.h"

class EntityRecord
{
  // The unit test classes needs access to private data.
  friend class EntityRecordTest;
  friend class EntityDBTest;
  friend class DbHelperTest;
public:

    //
    // The Caller Alias Structure
    //
    struct CallerAlias
    {
        std::string targetDomain;
        std::string alias;
    };

    //
    // Alias Structure
    //
    struct Alias
    {
        std::string id;
        std::string contact;
        std::string relation;
    };

    //
    // Static User Location
    //
    struct StaticUserLoc
    {
        std::string event;
        std::string contact;
        std::string fromUri;
        std::string toUri;
        std::string callId;
    };

    //
    // Caller-ID parameters
    //
    struct CallerId
    {
      std::string id;
      std::string type;
      bool enforcePrivacy;
      bool ignoreUserCalleId;
      bool transformExtension;
      int extensionLength;
      std::string extensionPrefix;

      CallerId()
      {
        enforcePrivacy = false;
        ignoreUserCalleId = false;
        transformExtension = false;
        extensionLength = 0;
      }
    };
    
    typedef std::vector<std::string> LocationSubnet;
    typedef std::vector<std::string> LocationDomain;

    EntityRecord();

    EntityRecord(const EntityRecord& entity);

    ~EntityRecord();

    EntityRecord& operator=(const EntityRecord& entity);

    void swap(EntityRecord& entity);

    EntityRecord& operator=(const mongo::BSONObj& bsonObj);

    //
    // The unique record object-id
    //
    std::string& oid();
    static const char* oid_fld();

    //
    // The sip user-id
    //
    std::string& userId();
    static const char* userId_fld();

    //
    // Identity of the a user designated by a uri with an optional port
    //
    std::string& identity();
    static const char* identity_fld();

    //
    // Realm used for authenticating users
    //
    std::string& realm();
    static const char* realm_fld();

    //
    // The SIP password
    //
    std::string& password();
    static const char* password_fld();

    //
    // The PIN for the user interface access
    //
    std::string& pin();
    static const char* pin_fld();

    //
    // The type of authentication used for this user
    //
    std::string& authType();
    static const char* authType_fld();

    //
    // User Location
    //
    std::string& location();
    static const char* location_fld();
    
    //
    // Call Forward Time
    //
    int& callForwardTime();
    static const char* callForwardTime_fld();

    //
    // Permission array to which the user has access to
    //
    std::set<std::string>& permissions();
    static const char* permission_fld();
    
    std::set<std::string>& allowedLocations();
    static const char* allowed_locations_fld();
    
    std::set<std::string>& associatedLocations();
    static const char* associated_locations_fld();
    
    const std::string& associatedLocationFallback();
    static const char* associated_location_fallback_fld();

    std::set<std::string>& inboundAssociatedLocations();
    static const char* inbound_associated_locations_fld();
    //
    // Permission array to which the user has access to
    //
    std::string& entity();
    static const char* entity_fld();
    
    //
    // Authentication Code if present
    //
    std::string& authc();
    static const char* authc_fld();
    
    //
    // Inbound location filter by domain
    //
    LocationDomain& loc_restr_dom();
    static const char* loc_restr_dom_fld();
    
    //
    // Inbound location filter by IP
    //
    LocationSubnet& loc_restr_sbnet();
    static const char* loc_restr_sbnet_fld();
    
    static const char* entity_branch_str();

    //
    // Caller alias to be sent to certain target domains
    //
    CallerId& callerId();
    static const char* callerId_fld();
    static const char*  callerIdEnforcePrivacy_fld();
    static const char*  callerIdIgnoreUserCalleId_fld();
    static const char*  callerIdTransformExtension_fld();
    static const char*  callerIdExtensionLength_fld();
    static const char*  callerIdExtensionPrefix_fld();

    //
    // Aliases that points back toa real user
    //
    std::vector<Alias>& aliases();
    static const char* aliases_fld();
    static const char* aliasesId_fld();
    static const char* aliasesContact_fld();
    static const char* aliasesRelation_fld();

    //
    // Static user locations
    //
    std::vector<StaticUserLoc>& staticUserLoc();
    static const char* staticUserLoc_fld();
    static const char* staticUserLocEvent_fld();
    static const char* staticUserLocContact_fld();
    static const char* staticUserLocFromUri_fld();
    static const char* staticUserLocToUri_fld();
    static const char* staticUserLocCallId_fld();

    //
    // Presence routing
    //
    static const char* vmOnDnd_fld();
    bool& vmOnDnd();
private:
    void fillStaticUserLoc(StaticUserLoc& userLoc, const mongo::BSONObj& innerObj);

private:
    std::string _oid;
    std::string _userId;
    std::string _identity;
    std::string _realm;
    std::string _password;
    std::string _pin;
    std::string _authType;
    std::string _location;
    std::string _authc;
    CallerId _callerId;
    LocationDomain _locRestrDom;
    LocationSubnet _locRestrSbnet;
    //bool _ignoreUserCallerId;
    //bool _transformCallerExtension;
    int _callForwardTime;
    std::set<std::string> _permissions;
    std::set<std::string> _allowedLocations;
    std::set<std::string> _associatedLocations;
    std::set<std::string> _inboundAssociatedLocations;
    std::string _associatedLocationFallback;
    std::string _entity;
    std::vector<Alias> _aliases;
    std::vector<StaticUserLoc> _staticUserLoc;
    bool _vmOnDnd;
};

//
// Inlines
//

inline std::string& EntityRecord::oid()
{
    return _oid;
}

inline std::string& EntityRecord::userId()
{
    return _userId;
}

inline std::string& EntityRecord::identity()
{
    return _identity;
}

inline std::string& EntityRecord::realm()
{
    return _realm;
}

inline std::string& EntityRecord::password()
{
    return _password;
}

inline std::string& EntityRecord::pin()
{
    return _pin;
}

inline std::string& EntityRecord::authType()
{
    return _authType;
}

inline std::string& EntityRecord::location()
{
    return _location;
}

inline int& EntityRecord::callForwardTime()
{
    return _callForwardTime;
}


inline std::set<std::string>& EntityRecord::permissions()
{
    return _permissions;
}

inline std::set<std::string>& EntityRecord::allowedLocations()
{
  return _allowedLocations;
}

inline std::set<std::string>& EntityRecord::associatedLocations()
{
  return _associatedLocations;
}

inline const std::string& EntityRecord::associatedLocationFallback()
{
  return _associatedLocationFallback;
}

inline std::set<std::string>& EntityRecord::inboundAssociatedLocations()
{
  return _inboundAssociatedLocations;
}

inline std::string& EntityRecord::entity()
{
    return _entity;
}

inline EntityRecord::CallerId& EntityRecord::callerId()
{
    return _callerId;
}

inline std::string& EntityRecord::authc()
{
  return _authc;
}
    
inline std::vector<EntityRecord::Alias>& EntityRecord::aliases()
{
    return _aliases;
}

inline std::vector<EntityRecord::StaticUserLoc>& EntityRecord::staticUserLoc()
{
    return _staticUserLoc;
}

inline bool& EntityRecord::vmOnDnd()
{
  return _vmOnDnd;
}

inline EntityRecord::LocationDomain& EntityRecord::loc_restr_dom()
{
  return _locRestrDom;
}

inline EntityRecord::LocationSubnet& EntityRecord::loc_restr_sbnet()
{
  return _locRestrSbnet;
}

#endif	/* ENTITYRECORD_H */


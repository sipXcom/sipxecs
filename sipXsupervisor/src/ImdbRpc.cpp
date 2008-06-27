//
// Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
//////////////////////////////////////////////////////////////////////////////

// SYSTEM INCLUDES
// APPLICATION INCLUDES
#include "utl/UtlBool.h"
#include "utl/UtlHashBag.h"
#include "utl/UtlHashBagIterator.h"
#include "utl/UtlHashMapIterator.h" 
#include "utl/UtlSListIterator.h" 
#include "os/OsProcessMgr.h"
#include "os/OsSysLog.h"
#include "os/OsFileSystem.h"
#include "net/XmlRpcDispatch.h"
#include "net/XmlRpcMethod.h"
#include "net/XmlRpcRequest.h"
#include "WatchDog.h"

#include "sipdb/ResultSet.h"
#include "sipdb/SIPDBManager.h"
#include "sipdb/AliasDB.h"
#include "sipdb/CallerAliasDB.h"
#include "sipdb/PermissionDB.h"
#include "sipdb/CredentialDB.h"
#include "sipdb/RegistrationDB.h"
#include "sipdb/ExtensionDB.h"
#include "sipdb/AuthexceptionDB.h"

#include "ImdbRpc.h"


// DEFINES
#define  URI            "uri"
#define  CONTACT        "contact"
#define  REALM          "realm"
#define  USERID         "userid"
#define  PASSTOKEN      "passtoken"
#define  PINTOKEN       "pintoken"
#define  AUTHTYPE       "authtype"
#define  IDENTITY       "identity"
#define  USER           "user"
#define  DOMAIN         "domain"

#define  CREDENTIAL     "credential"
#define  ALIAS          "alias"
#define  CALLER_ALIAS   "caller-alias"
#define  PERMISSION     "permission"
#define  EXTENSION      "extension"
#define  REGISTRATION   "registration"
#define  AUTHEXCEPTION  "authexception"

// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STATIC VARIABLE INITIALIZATIONS

/*************************************************************************
 **** ImdbRpcMethod contains common code for ImdbRpc methods
 ************************************************************************/
const char* ImdbRpcMethod::METHOD_NAME = "ImdbRpc.BASE";
const char* ImdbRpcMethod::PARAM_NAME_CALLING_HOST = "callingHostname";
const char* ImdbRpcMethod::PARAM_NAME_IMDB_TABLE = "IMDBTableName";
const char* ImdbRpcMethod::PARAM_NAME_IMDB_TABLE_DATA = "IMDBTableRecords";


XmlRpcMethod* ImdbRpcMethod::get()
{
   assert(false);  // this should have been overridden in the subclass

   return NULL;
}

void ImdbRpcMethod::registerSelf(WatchDog & watchdog)
{
   assert(false);  // this should have been overridden in the subclass
}

ImdbRpcMethod::ImdbRpcMethod()
{
}

void ImdbRpcMethod::registerMethod(const char*       methodName,
                                       XmlRpcMethod::Get getMethod,
                                       WatchDog &           watchdog 
                                       )
{
   watchdog.getXmlRpcDispatch()->addMethod(methodName, getMethod, &watchdog );
}

bool ImdbRpcMethod::execute(const HttpRequestContext& requestContext,
                                UtlSList& params,
                                void* userData,
                                XmlRpcResponse& response,
                                ExecutionStatus& status
                                )
{
   assert(false); // this should have been overridden in the subclass

   return false;
}

bool ImdbRpcMethod::validCaller(const HttpRequestContext& requestContext,
                                    const UtlString&          peerName,
                                    XmlRpcResponse&           response,
                                    const WatchDog&           watchdog,
                                    const char*               callingMethod
                                    )
{
   bool result = false;

   if (!peerName.isNull() && requestContext.isTrustedPeer(peerName))
   {
      // ssl says the connection is from the named host
      if (watchdog.isAllowedPeer(peerName))
      {
         // sipXsupervisor says it is one of the allowed peers.
         result = true;
         OsSysLog::add(FAC_WATCHDOG, PRI_DEBUG,
                       "ImdbMethod::validCaller '%s' peer authenticated for %s",
                       peerName.data(), callingMethod
                       );
      }
      else
      {
         // this peer is authenticated, but not configured, so provide a good error response
         UtlString faultMsg;
         faultMsg.append("Unconfigured calling host '");
         faultMsg.append(peerName);
         faultMsg.append("'");
         response.setFault(ImdbRpcMethod::UnconfiguredPeer, faultMsg.data());
            
         OsSysLog::add(FAC_WATCHDOG, PRI_ERR,
                       "%s failed - '%s' not a configured peer",
                       callingMethod, peerName.data()
                       );
      }
   }
   else
   {
      // ssl says not authenticated - provide only a generic error
      response.setFault(XmlRpcResponse::AuthenticationRequired, "TLS Peer Authentication Failure");
            
      OsSysLog::add(FAC_WATCHDOG, PRI_ERR,
                    "%s failed: '%s' failed SSL authentication",
                    callingMethod, peerName.data()
                    );
   }

   return result;
}

void ImdbRpcMethod::handleMissingExecuteParam(const char* methodName,
                                                  const char* paramName,
                                                  XmlRpcResponse& response,
                                                  ExecutionStatus& status)
{
   UtlString faultMsg;
   faultMsg += methodName;
   faultMsg += " '";
   faultMsg += paramName;
   faultMsg += "' parameter is missing or invalid type";
   status = XmlRpcMethod::FAILED;
   response.setFault(ImdbRpcMethod::InvalidParameter, faultMsg);
   OsSysLog::add(FAC_WATCHDOG, PRI_ERR, faultMsg);
}

void ImdbRpcMethod::handleExtraExecuteParam(const char* methodName,
                                                XmlRpcResponse& response,
                                                ExecutionStatus& status)
{
   UtlString faultMsg;
   faultMsg += methodName;
   faultMsg += " has incorrect number of parameters";
   status = XmlRpcMethod::FAILED;
   response.setFault(ImdbRpcMethod::InvalidParameter, faultMsg);
   OsSysLog::add(FAC_WATCHDOG, PRI_ERR, faultMsg);
}


/*write the database's content to xml file
*/
void ImdbRpcMethod::storeTable(const UtlString& tableName){

    if ( tableName == CREDENTIAL ){
        if ( CredentialDB::getInstance()->store() != OS_SUCCESS ) {
        }
    }

    else if ( tableName == ALIAS ){
        if ( AliasDB::getInstance()->store() != OS_SUCCESS ) {
        }
    }

    else if ( tableName == PERMISSION ){
        if ( PermissionDB::getInstance()->store() != OS_SUCCESS ){
        }
    }

    else if ( tableName == EXTENSION ){
        if ( ExtensionDB::getInstance()->store() != OS_SUCCESS ){
        }
    }

    else if ( tableName == AUTHEXCEPTION ){
        if ( AuthexceptionDB::getInstance()->store() != OS_SUCCESS ){
        }
    }

    else if ( tableName == CALLER_ALIAS ){
       if ( CallerAliasDB::getInstance()->store() != OS_SUCCESS ){
       }
    }
}


UtlBoolean ImdbRpcMethod::insertTableRecord(UtlString& tableName, const UtlHashMap& tableRecord)
{
    UtlBoolean   result;

    static UtlString identityKey      (IDENTITY);
    static UtlString domainKey        (DOMAIN);
    static UtlString aliasKey         (ALIAS);

    result = FALSE;

    if ( tableName == CREDENTIAL ){
        result = CredentialDB::getInstance()->insertRow(tableRecord);
    }

    else if ( tableName == ALIAS ){
        result = AliasDB::getInstance()->insertRow(tableRecord);
    }

    else if ( tableName == PERMISSION ){
        result = PermissionDB::getInstance()->insertRow(tableRecord);
    }

    else if ( tableName == EXTENSION ){
       result = ExtensionDB::getInstance()->insertRow(tableRecord);
    }

    else if ( tableName == AUTHEXCEPTION ){
       result = AuthexceptionDB::getInstance()->insertRow(tableRecord);
    }

    else if ( tableName == CALLER_ALIAS ){
       UtlString* optionalIdentity = dynamic_cast<UtlString*>(tableRecord.findValue(&identityKey));
       UtlString nullIdentity;
 
       result = TRUE;
       CallerAliasDB::getInstance()->
          insertRow(optionalIdentity ? *optionalIdentity : nullIdentity,
                    *dynamic_cast<UtlString*>(tableRecord.findValue(&domainKey)),
                    *dynamic_cast<UtlString*>(tableRecord.findValue(&aliasKey))
                    );
    }

    return result;
}



/*****************************************************************
 **** ImdbRpcReplaceTable
 *****************************************************************/

const char* ImdbRpcReplaceTable::METHOD_NAME = "ImdbTable.replace";

const char* ImdbRpcReplaceTable::name()
{
   return METHOD_NAME;
}

ImdbRpcReplaceTable::ImdbRpcReplaceTable()
{
}

XmlRpcMethod* ImdbRpcReplaceTable::get()
{
   return new ImdbRpcReplaceTable();
}

void ImdbRpcReplaceTable::registerSelf(WatchDog & watchdog)
{
   registerMethod(METHOD_NAME, ImdbRpcReplaceTable::get, watchdog);
}

bool ImdbRpcReplaceTable::execute(const HttpRequestContext& requestContext,
                                     UtlSList& params, 
                                     void* userData,
                                     XmlRpcResponse& response,
                                     ExecutionStatus& status)
{
   bool result = false;
   status = XmlRpcMethod::FAILED;

   // Verify that the number of parameters expected is correct.
   if (3 != params.entries())
   {
      handleExtraExecuteParam(name(), response, status);
   }
   else
   {
      // Extract the paramaters and verify them.  This method expects
      // the calling host name, IMDB Table name and the IMDB Table data records.
      if (!params.at(0) || !params.at(0)->isInstanceOf(UtlString::TYPE))
      {
         handleMissingExecuteParam(name(), PARAM_NAME_CALLING_HOST, response, status);
      }
      else
      {
         UtlString* pCallingHostname = dynamic_cast<UtlString*>(params.at(0));

         if (!params.at(1) || !params.at(1)->isInstanceOf(UtlString::TYPE))
         {
            handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE, response, status);
         }
         else
         {
            UtlString* pIMDBTable = dynamic_cast<UtlString*>(params.at(1));

            if (!params.at(2) || !params.at(2)->isInstanceOf(UtlSList::TYPE))
            {
               handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE_DATA, response, status);
            }
            else
            {
               UtlSList* pIMDBTableData = dynamic_cast<UtlSList*>(params.at(2));

               // Verify that at least the first row of table data is of the correct type.
               if (   !pIMDBTableData->isEmpty() /* an empty table is allowed */
                   && !pIMDBTableData->at(0)->isInstanceOf(UtlHashMap::TYPE)
                   )
               {
                  // the table data list was non-empty but the first item was not a UtlHashMap
                  handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE_DATA, response, status);
               }
               else
               {
                  WatchDog* pWatchDog = ((WatchDog *)userData);

                  if(validCaller(requestContext, *pCallingHostname, response, *pWatchDog, name()))
                  {
                     // we've validated the parm types and the caller.
                     // steps are to clear the existing table
                     // and then insert each record individually.
                     clearTable( *pIMDBTable );         

                     UtlSListIterator tableRecordItor(*pIMDBTableData);
                     UtlHashMap*      tableRecord;
                     UtlBool method_result(true);
                     UtlBoolean insert_result;
                     while ( (tableRecord = dynamic_cast <UtlHashMap*> (tableRecordItor())))
                     {
                        // records are committed as soon as they're added.
                        insert_result = insertTableRecord(*pIMDBTable, *tableRecord);
                        if ( ( FALSE == insert_result ) && ( true == method_result.getValue() ) )
                        {
                           method_result = false;
                        }
                     }

                     if ( method_result.getValue() == true )
                     {
                        storeTable( *pIMDBTable );         
                     }

                     // Construct and set the response.
                     response.setResponse(&method_result);
                     status = XmlRpcMethod::OK;
                     result = true;
                  }
               }
            }
         }
      }
   }

   return result;
}

void ImdbRpcReplaceTable::clearTable(UtlString& tableName)
{

    if ( tableName == CREDENTIAL ){
        CredentialDB::getInstance()->removeAllRows();
    }

    else if ( tableName == ALIAS ){
        AliasDB::getInstance()->removeAllRows();
    }

    else if ( tableName == PERMISSION ){
        PermissionDB::getInstance()->removeAllRows();
    }

    else if ( tableName == EXTENSION ){
       ExtensionDB::getInstance()->removeAllRows();
    }

    else if ( tableName == AUTHEXCEPTION ){
       AuthexceptionDB::getInstance()->removeAllRows();
    }

    else if ( tableName == CALLER_ALIAS ){
       CallerAliasDB::getInstance()->removeAllRows();
    }

}


/*****************************************************************
 **** ImdbRpcRetrieveTable
 *****************************************************************/

const char* ImdbRpcRetrieveTable::METHOD_NAME = "ImdbTable.read";

const char* ImdbRpcRetrieveTable::name()
{
   return METHOD_NAME;
}

ImdbRpcRetrieveTable::ImdbRpcRetrieveTable()
{
}

XmlRpcMethod* ImdbRpcRetrieveTable::get()
{
   return new ImdbRpcRetrieveTable();
}

void ImdbRpcRetrieveTable::registerSelf(WatchDog & watchdog)
{
   registerMethod(METHOD_NAME, ImdbRpcRetrieveTable::get, watchdog);
}

bool ImdbRpcRetrieveTable::execute(const HttpRequestContext& requestContext,
                                     UtlSList& params, 
                                     void* userData,
                                     XmlRpcResponse& response,
                                     ExecutionStatus& status)
{
   bool result = false;
   status = XmlRpcMethod::FAILED;

   // Verify that the number of parameters expected is correct.
   if (2 != params.entries())
   {
      handleExtraExecuteParam(name(), response, status);
   }
   else
   {
      // Extract the paramaters and verify them.  This method expects
      // the calling host name and IMDB Table name.
      if (!params.at(0) || !params.at(0)->isInstanceOf(UtlString::TYPE))
      {
         handleMissingExecuteParam(name(), PARAM_NAME_CALLING_HOST, response, status);
      }
      else
      {
         UtlString* pCallingHostname = dynamic_cast<UtlString*>(params.at(0));

         if (!params.at(1) || !params.at(1)->isInstanceOf(UtlString::TYPE))
         {
            handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE, response, status);
         }
         else
         {
            UtlBool method_result(true);
            UtlString* pIMDBTable = dynamic_cast<UtlString*>(params.at(1));

            WatchDog* pWatchDog = ((WatchDog *)userData);

            if(validCaller(requestContext, *pCallingHostname, response, *pWatchDog, name()))
            {
                 OsSysLog::add(FAC_WATCHDOG, PRI_INFO,
                             "ImdbRpc::retrieveTable"
                             " host %s requested IMDB Table retrieval",
                             pCallingHostname->data()
                            );

                  // Check the resource permissions. To be added when available.

                  // Get the records from the appropriate IMDB table.  (This dynamically allocates memory.)
                  ResultSet   imdb_tabledata;

                  readTable( *pIMDBTable, &imdb_tabledata );

                  // Construct and set the response.
                  response.setResponse(&imdb_tabledata);
                  status = XmlRpcMethod::OK;
                  result = true;

                  // Delete the new'd objects 
                  imdb_tabledata.destroyAll();

            }
         }
      }
   }

   return result;
}

void ImdbRpcRetrieveTable::readTable(UtlString& tableName, ResultSet* imdb_records)
{
    if ( tableName == CREDENTIAL ){
        CredentialDB::getInstance()->getAllRows( *imdb_records );
    }

    else if ( tableName == REGISTRATION ){
        RegistrationDB::getInstance()->getAllRows( *imdb_records );
    }

    else if ( tableName == ALIAS ){
        AliasDB::getInstance()->getAllRows( *imdb_records );
    }

    else if ( tableName == PERMISSION ){
        PermissionDB::getInstance()->getAllRows( *imdb_records );
    }

    else if ( tableName == EXTENSION ){
       ExtensionDB::getInstance()->getAllRows( *imdb_records );
    }

    else if ( tableName == AUTHEXCEPTION ){
       AuthexceptionDB::getInstance()->getAllRows( *imdb_records );
    }

    else if ( tableName == CALLER_ALIAS ){
       // CallerAliasDB::getInstance()->getAllRows( *imdb_records );
    }

}



/*****************************************************************
 **** ImdbRpcAddTableRecords
 *****************************************************************/

const char* ImdbRpcAddTableRecords::METHOD_NAME = "ImdbTable.insertRows";

const char* ImdbRpcAddTableRecords::name()
{
   return METHOD_NAME;
}

ImdbRpcAddTableRecords::ImdbRpcAddTableRecords()
{
}

XmlRpcMethod* ImdbRpcAddTableRecords::get()
{
   return new ImdbRpcAddTableRecords();
}

void ImdbRpcAddTableRecords::registerSelf(WatchDog & watchdog)
{
   registerMethod(METHOD_NAME, ImdbRpcAddTableRecords::get, watchdog);
}

bool ImdbRpcAddTableRecords::execute(const HttpRequestContext& requestContext,
                                     UtlSList& params, 
                                     void* userData,
                                     XmlRpcResponse& response,
                                     ExecutionStatus& status)
{
   bool result = false;
   status = XmlRpcMethod::FAILED;

   // Verify that the number of parameters expected is correct.
   if (3 != params.entries())
   {
      handleExtraExecuteParam(name(), response, status);
   }
   else
   {
      // Extract the paramaters and verify them.  This method expects
      // the calling host name, IMDB Table name and the IMDB Table data records.
      if (!params.at(0) || !params.at(0)->isInstanceOf(UtlString::TYPE))
      {
         handleMissingExecuteParam(name(), PARAM_NAME_CALLING_HOST, response, status);
      }
      else
      {
         UtlString* pCallingHostname = dynamic_cast<UtlString*>(params.at(0));

         if (!params.at(1) || !params.at(1)->isInstanceOf(UtlString::TYPE))
         {
            handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE, response, status);
         }
         else
         {
            UtlString* pIMDBTable = dynamic_cast<UtlString*>(params.at(1));

            if (!params.at(2) || !params.at(2)->isInstanceOf(UtlSList::TYPE))
            {
               handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE_DATA, response, status);
            }
            else
            {
               UtlSList* pIMDBTableData = dynamic_cast<UtlSList*>(params.at(2));

               // Verify that the table data is of the correct type.
               if (!pIMDBTableData->at(0) || !pIMDBTableData->at(0)->isInstanceOf(UtlHashMap::TYPE))
               {
                  handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE_DATA, response, status);
               }

               WatchDog* pWatchDog = ((WatchDog *)userData);

               if(validCaller(requestContext, *pCallingHostname, response, *pWatchDog, name()))
               {
                  // we've validated the parm types and the caller.

                  UtlSListIterator tableRecordItor(*pIMDBTableData);
                  UtlHashMap*      tableRecord;
                  UtlBool method_result(true);
                  UtlBoolean insert_result;
                  while ( (tableRecord = dynamic_cast <UtlHashMap*> (tableRecordItor())))
                  {
                     // records are committed as soon as they're added.
                     insert_result = insertTableRecord(*pIMDBTable, *tableRecord);
                     if ( ( FALSE == insert_result ) && ( true == method_result.getValue() ) ) {
                        method_result = false;
                     }
                  }

                  // If we successfully added the new rows, write out the table to an XML file.
                  if ( method_result.getValue() == true ) {
                     storeTable( *pIMDBTable );
                  }

                  // Construct and set the response.
                  response.setResponse(&method_result);
                  status = XmlRpcMethod::OK;
                  result = true;

                  
               }
            }
         }
      }
   }

   return result;
}


/*****************************************************************
 **** ImdbRpcDeleteTableRecords
 *****************************************************************/

const char* ImdbRpcDeleteTableRecords::METHOD_NAME = "ImdbTable.deleteRows";

const char* ImdbRpcDeleteTableRecords::name()
{
   return METHOD_NAME;
}

ImdbRpcDeleteTableRecords::ImdbRpcDeleteTableRecords()
{
}

XmlRpcMethod* ImdbRpcDeleteTableRecords::get()
{
   return new ImdbRpcDeleteTableRecords();
}

void ImdbRpcDeleteTableRecords::registerSelf(WatchDog & watchdog)
{
   registerMethod(METHOD_NAME, ImdbRpcDeleteTableRecords::get, watchdog);
}

bool ImdbRpcDeleteTableRecords::execute(const HttpRequestContext& requestContext,
                                     UtlSList& params, 
                                     void* userData,
                                     XmlRpcResponse& response,
                                     ExecutionStatus& status)
{
   bool result = false;
   status = XmlRpcMethod::FAILED;

   // Verify that the number of parameters expected is correct.
   if (3 != params.entries())
   {
      handleExtraExecuteParam(name(), response, status);
   }
   else
   {
      // Extract the paramaters and verify them.  This method expects
      // the calling host name, IMDB Table name and the IMDB Table data record keys.
      if (!params.at(0) || !params.at(0)->isInstanceOf(UtlString::TYPE))
      {
         handleMissingExecuteParam(name(), PARAM_NAME_CALLING_HOST, response, status);
      }
      else
      {
         UtlString* pCallingHostname = dynamic_cast<UtlString*>(params.at(0));

         if (!params.at(1) || !params.at(1)->isInstanceOf(UtlString::TYPE))
         {
            handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE, response, status);
         }
         else
         {
            UtlString* pIMDBTable = dynamic_cast<UtlString*>(params.at(1));

            if (!params.at(2) || !params.at(2)->isInstanceOf(UtlSList::TYPE))
            {
               handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE_DATA, response, status);
            }
            else
            {
               UtlSList* pIMDBTableDataKeys = dynamic_cast<UtlSList*>(params.at(2));

               // Verify that the table data is of the correct type.
               if (!pIMDBTableDataKeys->at(0) || !pIMDBTableDataKeys->at(0)->isInstanceOf(UtlHashMap::TYPE))
               {
                  handleMissingExecuteParam(name(), PARAM_NAME_IMDB_TABLE_DATA, response, status);
               }

               WatchDog* pWatchDog = ((WatchDog *)userData);

               if(validCaller(requestContext, *pCallingHostname, response, *pWatchDog, name()))
               {
                  // we've validated the parm types and the caller.

                  UtlSListIterator tableRecordKeysItor(*pIMDBTableDataKeys);
                  UtlHashMap*      tableRecordKeys;
                  UtlBool method_result(true);
                  UtlBoolean delete_result;
                  while ( (tableRecordKeys = dynamic_cast <UtlHashMap*> (tableRecordKeysItor())))
                  {
                     // record deletion is immediate.
                     delete_result = deleteTableRecord(*pIMDBTable, *tableRecordKeys);
                     if ( ( FALSE == delete_result ) && ( true == method_result.getValue() ) ) {
                        method_result = false;
                     }
                  }

                  // If we successfully deleted the rows, write out the table to an XML file.
                  if ( method_result.getValue() == true ) {
                     storeTable( *pIMDBTable );
                  }

                  // Construct and set the response.
                  response.setResponse(&method_result);
                  status = XmlRpcMethod::OK;
                  result = true;

                  
               }
            }
         }
      }
   }

   return result;
}

UtlBoolean ImdbRpcDeleteTableRecords::deleteTableRecord(UtlString& tableName, const UtlHashMap& tableRecordKeys)
{
    UtlBoolean   result;

    static UtlString uriKey           (URI);
    static UtlString extensionKey     (EXTENSION);
    static UtlString contactKey       (CONTACT);
    static UtlString realmKey         (REALM);
    static UtlString useridKey        (USERID);
    static UtlString passtokenKey     (PASSTOKEN);
    static UtlString pintokenKey      (PINTOKEN);
    static UtlString authtypeKey      (AUTHTYPE);
    static UtlString identityKey      (IDENTITY);
    static UtlString permissionKey    (PERMISSION);
    static UtlString userKey          (USER);
    static UtlString domainKey        (DOMAIN);
    static UtlString aliasKey         (ALIAS);

    result = FALSE;

    if ( tableName == CREDENTIAL ){
        CredentialDB::getInstance()->removeRows(
                     Url (*((UtlString*)tableRecordKeys.findValue(&uriKey))) );
        result = TRUE;
    }

    else if ( tableName == ALIAS ){
        result = AliasDB::getInstance()->removeRow(
                     Url (*((UtlString*)tableRecordKeys.findValue(&identityKey))) );
    }

    else if ( tableName == PERMISSION ){
        PermissionDB::getInstance()->removeRows(
                     Url (*((UtlString*)tableRecordKeys.findValue(&identityKey))) );
        result = TRUE;
    }

    else if ( tableName == EXTENSION ){
        result = ExtensionDB::getInstance()->removeRow(
                     Url (*((UtlString*)tableRecordKeys.findValue(&uriKey))) );
    }

    else if ( tableName == AUTHEXCEPTION ){
       result = AuthexceptionDB::getInstance()->removeRow(
                     (*(UtlString*)tableRecordKeys.findValue(&userKey)) );
    }

    return result;
}


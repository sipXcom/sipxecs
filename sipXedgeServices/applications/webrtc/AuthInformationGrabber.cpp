
#include <rutil/Data.hxx>
#include <rutil/MD5Stream.hxx>
#include <rutil/DataStream.hxx>
#include <resip/stack/Symbols.hxx>
#include <os/OsLogger.h>

#include <boost/filesystem.hpp>
#include <boost/algorithm/string.hpp>

#include "WSRouter.h"
#include "AuthInformationGrabber.h"
#include "csv_parser/csv_parser.hpp"

#define DEFAULT_CACHE_LIFETIME 3600 * 24
#define USER_CACHE_FILE "/edge/user-cache.csv"

AuthInformationGrabber::AuthInformationGrabber(WSRouter* pRouter, UserInfoCache* pUserInfoCache, jsonrpc::Client* pRpc, const char* userCacheFile) :
  repro::UserAuthGrabber(pRouter->repro()->getReproConfig().getDataStore()->mUserStore),
  _pRouter(pRouter),
  _pUserInfoCache(pUserInfoCache),
  _canDeleteCache(false),
  _pRpc(pRpc),
  _canDeleteRpc(false)
{
  if (!_pUserInfoCache)
  {
    _pUserInfoCache = new UserInfoCache(DEFAULT_CACHE_LIFETIME * 1000);
    _canDeleteCache = true;
  }
  
  //
  // Initialize the RPC connector.  
  // TODO:  Make this highly available
  //
  if (!_pRpc && _pRouter->getOption("rpc-url", _rpcUrl))
  {
    OS_LOG_INFO(FAC_SIP, "Setting up RPC service to " << _rpcUrl);
    _pRpc = new jsonrpc::Client(new jsonrpc::HttpClient(_rpcUrl));
    _canDeleteRpc = true;
  }

  if (userCacheFile)
    loadCacheFromFile(userCacheFile);
  else
    loadCacheFromFile(USER_CACHE_FILE);
}
  
AuthInformationGrabber::~AuthInformationGrabber()
{
  if (_canDeleteCache)
    delete _pUserInfoCache;
  
  if (_canDeleteRpc)  
    delete _pRpc;
}

bool AuthInformationGrabber::getSipPassword(const resip::Data& user, const resip::Data& realm, resip::Data& password)
{ 
  
  if (!_pRpc)
    return false;
  
  Json::Value params;
  params["user"] = user.c_str();
  params["realm"] = realm.c_str();
  
  try
  {
    Json::Value result;
    
    _pRpc->CallMethod("getSipPassword", params, result);
    
    if (!result.isMember("error") && result.isMember("password"))
    {
      password = result["password"].asCString();
    }
    else
    {
      OS_LOG_ERROR(FAC_SIP, "AuthInformationGrabber::getSipPassword User Not Found");
    }
  }
  catch (jsonrpc::JsonRpcException e)
  {
      OS_LOG_ERROR(FAC_SIP, "AuthInformationGrabber::getSipPassword FAILED with error: "  << e.what());
      return false;
  }
  
  return !password.empty();
}

bool AuthInformationGrabber::getUserAuthInfo(const resip::Data& user, const resip::Data& realm, resip::Data& a1Hash)
{
  resip::Data password;
  if (!getSipPassword(user, realm, password))
    return false;
  
  MD5Stream a1;
  a1 << user
     << Symbols::COLON
     << realm
     << Symbols::COLON
     << password;
  a1.flush();
  a1Hash = a1.getHex();
  
  AuthInfoRecord rec;
  rec.password = password.c_str();
  rec.a1 = a1Hash.c_str();
  
  setCachedAuthInfo(user, realm, rec);
  return true;
}

bool AuthInformationGrabber::getCachedAuthInfo(const resip::Data& user, const resip::Data& realm, AuthInfoRecord& rec)
{
  std::ostringstream identity;
  identity << user << "@" << realm;
   
  UserInfoPtr ptr = _pUserInfoCache->get(identity.str());
  if (ptr)
  {
    OS_LOG_DEBUG(FAC_SIP, _pUserInfoCache << " - AuthInformationGrabber::getCachedAuthInfo(" << identity.str() << ") - " <<  ptr->a1);
    rec = *ptr;
    return true;
  }
  
  OS_LOG_DEBUG(FAC_SIP, _pUserInfoCache << " - AuthInformationGrabber::getCachedAuthInfo(" << identity.str() << ") - No Entry");
  return false;
}

void AuthInformationGrabber::setCachedAuthInfo(const resip::Data& user, const resip::Data& realm, const AuthInfoRecord& rec)
{
  //
  // Update the cache
  //
  std::ostringstream identity;
  identity << user << "@" << realm;
  
  UserInfoPtr cacheData  = UserInfoPtr(new AuthInfoRecord(rec));
  _pUserInfoCache->add(identity.str(), cacheData);
  OS_LOG_DEBUG(FAC_SIP, "AuthInformationGrabber::setCachedAuthInfo(" << identity.str() << ", " << rec.a1 << ")");
}
      
bool AuthInformationGrabber::process(resip::ApplicationMessage* msg)
{
  repro::UserInfoMessage* uinf = dynamic_cast<UserInfoMessage*>(msg);    // auth for repro's DigestAuthenticator
  resip::UserAuthInfo* uainf = dynamic_cast<resip::UserAuthInfo*>(msg);  // auth for DUM's ServerAuthManager
  
  if(uinf)
  {
    getUserAuthInfo(uinf->user(), uinf->realm(), uinf->mRec.passwordHash);
    DebugLog(<<"AuthInformationGrabber Grabbed user info for " 
                   << uinf->user() <<"@"<<uinf->realm()
                   << " : " << uinf->A1());
    return true;
  }
  else if(uainf)
  {
    resip::Data a1Hash;
    if (getUserAuthInfo(uainf->getUser(), uainf->getRealm(), a1Hash))
      uainf->setA1(a1Hash);
    
    if(uainf->getA1().empty())
    {
       uainf->setMode(resip::UserAuthInfo::UserUnknown);
    }
    DebugLog(<<"AuthInformationGrabber Grabbed user info for " 
                   << uainf->getUser() <<"@"<<uainf->getRealm()
                   << " : " << uainf->getA1());
    return true;
  }
  else
  {
    WarningLog(<<"Did not recognize message type...");
  }
  return false;
}

repro::UserAuthGrabber* AuthInformationGrabber::clone() const
{
  return static_cast<repro::UserAuthGrabber*>(new AuthInformationGrabber(_pRouter, _pUserInfoCache, _pRpc));
}

bool AuthInformationGrabber::loadCacheFromFile( const std::string& cacheFile)
{
  if (!boost::filesystem::exists(cacheFile.c_str()))
    return false;

  const char field_terminator = ',';
  const char line_terminator  = '\n';
  const char enclosure_char   = '"';
  const char* user_name_field = "USER NAME";
  const char* sip_password_filed = "SIP PASSWORD";

  csv_parser csvParser;
  csvParser.set_skip_lines(0);
  csvParser.init(cacheFile.c_str());
  csvParser.set_enclosed_char(enclosure_char, ENCLOSURE_OPTIONAL);
  csvParser.set_field_term_char(field_terminator);
  csvParser.set_line_term_char(line_terminator);

  //
  // Detect the index for user name and password
  //
  if (!csvParser.has_more_rows())
    return false;

  csv_row header = csvParser.get_row();
  std::size_t headerSize = header.size();
  std::size_t userNameIndex = headerSize;
  std::size_t sipPasswordIndex = headerSize;


  for (std::size_t i = 0; i < headerSize; i++)
  {
    std::string h = header[i].c_str();
    boost::to_upper(h);
    if (userNameIndex == headerSize && h == user_name_field)
      userNameIndex = i;
    else if (sipPasswordIndex == headerSize && h == sip_password_filed)
      sipPasswordIndex = i;
  }

  if (userNameIndex == headerSize || sipPasswordIndex == headerSize)
    return false;

  while(csvParser.has_more_rows())
  {
    csv_row row = csvParser.get_row();

    if (row.size() > userNameIndex && row.size() > sipPasswordIndex)
    {
      resip::Data user = row[userNameIndex].c_str();
      resip::Data password = row[sipPasswordIndex].c_str();


      if (!user.empty() && !password.empty() && !_pRouter->getRealm().empty())
      {
        resip::Data realm = _pRouter->getRealm().c_str();

        resip::Data a1Hash;
        
        MD5Stream a1;
        a1 << user
           << Symbols::COLON
           << realm
           << Symbols::COLON
           << password;
        a1.flush();
        a1Hash = a1.getHex();

        AuthInfoRecord rec;
        rec.password = password.c_str();
        rec.a1 = a1Hash.c_str();

        OS_LOG_INFO(FAC_SIP, "Caching authentication info for " << user.data() << "@" << realm.data());

        setCachedAuthInfo(user, realm, rec);
      }
    }
  }

  return true;
}
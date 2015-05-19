 
#include "V8Engine.h"
#include <sipxproxy/SipRouter.h>
#include <os/OsLogger.h>
#include <os/OsConfigDb.h>

#include <OSS/JS/JSSIPMessage.h>

static const char PLUGIN_SCRIPT_CONFIG_PARAM[] = "SCRIPT";

static OSS::JS::JSSIPMessage gV8Engine("V8Engine");

extern "C" AuthPlugin* getAuthPlugin(const UtlString& pluginName)
{
  return new V8Engine(pluginName);
}

// Reads a file into a v8 string.
static std::string read_file(const std::string& name) 
{
  std::string data;
  FILE* file = fopen(name.c_str(), "rb");
  if (file == NULL)
  {
    return std::string();
  }

  fseek(file, 0, SEEK_END);
  int size = ftell(file);
  rewind(file);

  char* chars = new char[size + 1];
  chars[size] = '\0';
  for (int i = 0; i < size;) {
    int read = fread(&chars[i], 1, size - i, file);
    i += read;
  }
  fclose(file);
  data = std::string(chars, size);
  delete[] chars;
  return data;
}


static OSS::SIP::SIPMessage* unwrapRequest(const v8::Arguments& args)
{
  if (args.Length() < 1)
    return 0;
  v8::Handle<v8::Value> obj = args[0];
  if (!obj->IsObject())
    return 0;
  v8::Handle<v8::External> field = v8::Handle<v8::External>::Cast(obj->ToObject()->GetInternalField(0));
  void* ptr = field->Value();
  return static_cast<OSS::SIP::SIPMessage*>(ptr);
}

static std::string jsvalToString(const v8::Handle<v8::Value>& str)
{
  if (!str->IsString())
    return "";
  v8::String::Utf8Value value(str);
  return *value;
}

static v8::Handle<v8::Value> msgSetProperty(const v8::Arguments& args)
{
  if (args.Length() < 3)
    return v8::Boolean::New(false);

  v8::HandleScope scope;
  OSS::SIP::SIPMessage* pMsg = unwrapRequest(args);
  if (!pMsg)
    return v8::Boolean::New(false);


  std::string name = jsvalToString(args[1]);
  std::string value = jsvalToString(args[2]);

  if (name.empty() || value.empty())
    return v8::Boolean::New(false);

  pMsg->setProperty(name, value);

  return v8::Boolean::New(true);
}

static v8::Handle<v8::Value> msgGetProperty(const v8::Arguments& args)
{
  if (args.Length() < 2)
    return v8::Undefined();

  v8::HandleScope scope;
  OSS::SIP::SIPMessage* pMsg = unwrapRequest(args);
  if (!pMsg)
    return v8::Undefined();

  std::string name = jsvalToString(args[1]);

  if (name.empty())
    return v8::Undefined();

  std::string value;
  pMsg->getProperty(name, value);

  return v8::String::New(value.c_str());
}

static v8::Handle<v8::Value> msgSetTransactionProperty(const v8::Arguments& args)
{
  //
  // We don't support transaction properties
  //
  return v8::Boolean::New(false);
}

static v8::Handle<v8::Value> msgGetTransactionProperty(const v8::Arguments& args)
{
  //
  // We don't support transaction properties
  //
  return v8::Undefined();
}

//
// Logging
//
static v8::Handle<v8::Value> v8_logger(tagOsSysLogPriority prio, const v8::Arguments& args)
{
  if (args.Length() < 1)
    return v8::Undefined();

  v8::HandleScope scope;
  v8::Handle<v8::Value> arg = args[0];
  v8::String::Utf8Value value(arg);
  std::ostringstream msg;
  msg << "JS: " << *value;
  Os::Logger::instance().log(FAC_SIP, prio, msg.str().c_str());
  
  return v8::Undefined();
}

static v8::Handle<v8::Value> V8_OS_LOG_CRITICAL(const v8::Arguments& args)
{
  return v8_logger(PRI_CRIT, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_EMERGENCY(const v8::Arguments& args)
{
  return v8_logger(PRI_EMERG, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_ERROR(const v8::Arguments& args)
{
  return v8_logger(PRI_ERR, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_WARNING(const v8::Arguments& args)
{
  return v8_logger(PRI_WARNING, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_NOTICE(const v8::Arguments& args)
{
  return v8_logger(PRI_NOTICE, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_INFO(const v8::Arguments& args)
{
  return v8_logger(PRI_INFO, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_DEBUG(const v8::Arguments& args)
{
  return v8_logger(PRI_DEBUG, args);
}

static v8::Handle<v8::Value> V8_OS_LOG_ALERT(const v8::Arguments& args)
{
  return v8_logger(PRI_ALERT, args);
}

static void msgRegisterGlobals(OSS::OSS_HANDLE objectTemplate)
{
  v8::Handle<v8::ObjectTemplate>& global = *(static_cast<v8::Handle<v8::ObjectTemplate>*>(objectTemplate));
  global->Set(v8::String::New("msgSetProperty"), v8::FunctionTemplate ::New(msgSetProperty));
  global->Set(v8::String::New("msgGetProperty"), v8::FunctionTemplate ::New(msgGetProperty));
  global->Set(v8::String::New("msgSetTransactionProperty"), v8::FunctionTemplate ::New(msgSetTransactionProperty));
  global->Set(v8::String::New("msgGetTransactionProperty"), v8::FunctionTemplate ::New(msgGetTransactionProperty));
  
  //
  // Logging
  //
  global->Set(v8::String::New("OS_LOG_CRITICAL"), v8::FunctionTemplate ::New(V8_OS_LOG_CRITICAL));
  global->Set(v8::String::New("OS_LOG_EMERGENCY"), v8::FunctionTemplate ::New(V8_OS_LOG_EMERGENCY));
  global->Set(v8::String::New("OS_LOG_ERROR"), v8::FunctionTemplate ::New(V8_OS_LOG_ERROR));
  global->Set(v8::String::New("OS_LOG_WARNING"), v8::FunctionTemplate ::New(V8_OS_LOG_WARNING));
  global->Set(v8::String::New("OS_LOG_NOTICE"), v8::FunctionTemplate ::New(V8_OS_LOG_NOTICE));
  global->Set(v8::String::New("OS_LOG_INFO"), v8::FunctionTemplate ::New(V8_OS_LOG_INFO));
  global->Set(v8::String::New("OS_LOG_DEBUG"), v8::FunctionTemplate ::New(V8_OS_LOG_DEBUG));
  global->Set(v8::String::New("OS_LOG_ALERT"), v8::FunctionTemplate ::New(V8_OS_LOG_ALERT));
}


V8Engine::V8Engine(const UtlString& instanceName) :
  AuthPlugin(instanceName),
  _pSipRouter(0)
{
}
  
V8Engine::~V8Engine()
{
}

void V8Engine::readConfig(OsConfigDb& configDb)
{
  UtlString fileName;
  if (!configDb.get(PLUGIN_SCRIPT_CONFIG_PARAM, fileName))
  {
    OS_LOG_WARNING(FAC_SIP, "V8Engine::readConfig - No V8 Engine Script configured.  Scripting engine will be DISABLED");
    return;
  }
     
  std::string gMainScript = read_file(fileName.data());
  
  if (gMainScript.empty())
  {
    OS_LOG_WARNING(FAC_SIP, "V8Engine::readConfig - Unable to load script file. " <<  fileName.data() << ". Scripting engine will be DISABLED");
    return;
  }
  
  OS_LOG_NOTICE(FAC_SIP, "V8Engine::readConfig - Loaded " <<  fileName.data() << ". Scripting engine is enforcing");
  
  gV8Engine.initialize("handle_request", "", gMainScript, msgRegisterGlobals);
}

  
AuthPlugin::AuthResult V8Engine::authorizeAndModify(const UtlString& id,    /**< The authenticated identity of the
                                                            *   request originator, if any (the null
                                                            *   string if not).
                                                            *   This is in the form of a SIP uri
                                                            *   identity value as used in the
                                                            *   credentials database (user@domain)
                                                            *   without the scheme or any parameters.
                                                            */
                                   const Url&  requestUri, ///< parsed target Uri
                                   RouteState& routeState, ///< the state for this request.  
                                   const UtlString& method,///< the request method
                                   AuthResult  priorResult,///< results from earlier plugins.
                                   SipMessage& request,    ///< see AuthPlugin regarding modifying
                                   bool bSpiralingRequest, ///< request spiraling indication 
                                   UtlString&  reason      ///< rejection reason
                                   )
{
  if (!gV8Engine.isInitialized())
  {
    return AuthPlugin::CONTINUE;
  }
  
  std::string packet(request.getBytes());
  
  try
  {
    OSS::SIP::SIPMessage::Ptr pMsg(new OSS::SIP::SIPMessage(packet));
    
    pMsg->setProperty("operation", "authorizeAndModify");
    
    if (bSpiralingRequest)
    {
      pMsg->setProperty("spiraling", "true");
    }
    else
    {
      pMsg->setProperty("spiraling", "false");
    }
    
    if (!gV8Engine.processRequest(pMsg))
    {
      //
      // Treat all exceptions as NOOP but do log it
      //
      OS_LOG_ERROR(FAC_SIP, "V8Engine::authorizeAndModify - Error calling V8Engine::processRequest");
      return AuthPlugin::CONTINUE;
    }
    
    std::string action;
    pMsg->getProperty("route-action", action);
    OSS::string_to_lower(action);
    
    if (action == "continue")
    {
      std::string reparse;
      pMsg->getProperty("msg-reparse", reparse);
      OSS::string_to_lower(reparse);
      
      if (reparse[0] == 't' || reparse[0] == 'y')
      {
        request.parseMessage(pMsg->data().c_str(), pMsg->data().size());
      }
    
      return AuthPlugin::CONTINUE; 
    }
  }
  catch(const std::exception& e)
  {
    //
    // Treat all exceptions as NOOP but do log it
    //
    OS_LOG_ERROR(FAC_SIP, "V8Engine::authorizeAndModify - Exception: " << e.what());
    return AuthPlugin::CONTINUE;
  }
  catch(...)
  {
    OS_LOG_ERROR(FAC_SIP, "V8Engine::authorizeAndModify - Exception: UNKNOWN");
    return AuthPlugin::CONTINUE;
  }
  
  return AuthPlugin::DENY;
}

void V8Engine::modifyTrustedRequest(
                                 const Url&  requestUri,  ///< parsed target Uri
                                 SipMessage& request,     ///< see below regarding modifying this
                                 bool bSpiralingRequest  ///< true if request is still spiraling through pr
                                 )
{
  if (!gV8Engine.isInitialized())
  {
    return;
  }
  
  std::string packet(request.getBytes());
  
  try
  {
    OSS::SIP::SIPMessage::Ptr pMsg(new OSS::SIP::SIPMessage(packet));
    
    pMsg->setProperty("operation", "modifyTrustedRequest");
    
    if (bSpiralingRequest)
    {
      pMsg->setProperty("spiraling", "true");
    }
    else
    {
      pMsg->setProperty("spiraling", "false");
    }
    
    if (!gV8Engine.processRequest(pMsg))
    {
      //
      // Treat all exceptions as NOOP but do log it
      //
      OS_LOG_ERROR(FAC_SIP, "V8Engine::authorizeAndModify - Error calling V8Engine::processRequest");
      return;
    }
    
    std::string reparse;
    pMsg->getProperty("msg-reparse", reparse);
    OSS::string_to_lower(reparse);

    if (reparse[0] == 't' || reparse[0] == 'y')
    {
      request.parseMessage(pMsg->data().c_str(), pMsg->data().size());
    }
  }
  catch(const std::exception& e)
  {
    //
    // Treat all exceptions as NOOP but do log it
    //
    OS_LOG_ERROR(FAC_SIP, "V8Engine::modifyTrustedRequest - Exception: " << e.what());
  }
  catch(...)
  {
    OS_LOG_ERROR(FAC_SIP, "V8Engine::modifyTrustedRequest - Exception: UNKNOWN");
  }
  
  return;
}

void V8Engine::modifyFinalResponse(
  SipTransaction* pTransaction, 
  const SipMessage& request, 
  SipMessage& finalResponse)
{
  if (!gV8Engine.isInitialized())
  {
    return;
  }
  
  std::string packet(finalResponse.getBytes());
  
  try
  {
    OSS::SIP::SIPMessage::Ptr pMsg(new OSS::SIP::SIPMessage(packet));
    
    pMsg->setProperty("operation", "modifyFinalResponse");
       
    if (!gV8Engine.processRequest(pMsg))
    {
      //
      // Treat all exceptions as NOOP but do log it
      //
      OS_LOG_ERROR(FAC_SIP, "V8Engine::authorizeAndModify - Error calling V8Engine::processRequest");
      return;
    }
    
    std::string reparse;
    pMsg->getProperty("msg-reparse", reparse);
    OSS::string_to_lower(reparse);

    if (reparse[0] == 't' || reparse[0] == 'y')
    {
      finalResponse.parseMessage(pMsg->data().c_str(), pMsg->data().size());
    }
  }
  catch(const std::exception& e)
  {
    //
    // Treat all exceptions as NOOP but do log it
    //
    OS_LOG_ERROR(FAC_SIP, "V8Engine::modifyFinalResponse - Exception: " << e.what());
  }
  catch(...)
  {
    OS_LOG_ERROR(FAC_SIP, "V8Engine::modifyFinalResponse - Exception: UNKNOWN");
  }
  
  return;
}
   


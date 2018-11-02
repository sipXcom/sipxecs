local sipXReferror = session:getVariable("sip_h_X-sipX-Referror")
local sipXAuthIdentity = session:getVariable("sip_h_X-Sipx-Authidentity")
local byUser = session:getVariable("sip_referred_by_user")

freeswitch.consoleLog("info", "SETTING CALLBACK: sip_h_X-sipX-Referror:" .. sipXReferror .. " sip_h_X-Sipx-Authidentity:" .. sipXAuthIdentity .. " sip_referred_by_user:" .. byUser)

if sipXReferror then
  freeswitch.consoleLog("info", "SETTING CALLBACK FROM sip_h_X-sipX-Referror")
  session:execute("set", "transf_ext=${regex(${sip_h_X-sipX-Referror}|(.*)@|%1)}")
elseif sipXAuthIdentity then
  freeswitch.consoleLog("info", "SETTING CALLBACK FROM sip_h_X-Sipx-Authidentity")
  session:execute("set", "transf_ext=${regex(${sip_h_X-Sipx-Authidentity}|sip:(.*)@|%1)}")
elseif byUser then
  freeswitch.consoleLog("info", "SETTING CALLBACK FROM sip_referred_by_user")
  session:execute("set", "transf_ext=${sip_referred_by_user}")
end
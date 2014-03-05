/**
 *
 * Copyright (c) eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
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
 * 
 */



#include "WSRouter.h"
#include "sipXecsService/SipXApplication.h"


int main(int argc, char** argv)
{
  //
  // daemonize early on
  //
  SipXApplication::doDaemonize(argc, argv);

  SipXApplicationData appData =
  {
      "wsrouter",
      "",
      "",
      "",
      "",
      false, // do not check mongo connection
      true, // increase application file descriptor limits
      SipXApplicationData::ConfigFileFormatIni, // format type for configuration file
      OsMsgQShared::QUEUE_LIMITED, //limited queue
  };

  WSRouter router(argc, argv, "wsrouter");
  SipXApplication::instance().init(argc, argv, appData, &router);

  return router.main();
}



#include <iostream>
#include <string.h>
#include <stdlib.h>

#include "digitmaps/AuthRulesUrlMapping.h"
#include "digitmaps/EmergencyRulesUrlMapping.h"

#include "os/OsLogger.h"
#include "net/Url.h"

#include "utl/UtlSListIterator.h"
#include "utl/UtlHashMapIterator.h"

int main(int argc, char *argv[])
{
    UtlString fileName(argv[1]);
    UtlString callerLocation(argv[2]);

    UtlString uri(argv[2]);
    Url requestUri(uri);
    requestUri.toString(uri);
    // TODO check input parameters

    std::cout << "AUTH RULES CHECKER" << std::endl;
    std::cout << "using rules file: " << fileName.data() << ", request URI: " << uri << std::endl;

    AuthRulesUrlMapping map;
    EmergencyRulesUrlMapping emergency;

    if (map.loadMappings(fileName))
    {
        std::cout << "loading file " << fileName << " complete" << std::endl;

        ResultSet records;
        map.getPermissionRequired(requestUri, records);

        if (!records.isEmpty())
        {
            UtlSListIterator requiredRecs(records);
            UtlHashMap* record;
            UtlString permissionKey("permission");

            while((record = dynamic_cast<UtlHashMap*>(requiredRecs())))
            {
                UtlHashMapIterator it(*record);

                while (it() != NULL)
                {
                    UtlString *key = dynamic_cast <UtlString *> (it.key());
                    UtlString *value = dynamic_cast <UtlString *> (it.value());

                    std::cout << "record(key, value):  " << (key ? *key : "___") << ", " << (value ? *value : "___") << std::endl;
                }
            }
        }
        else
        {
            std::cout << "result set is empty" << std::endl;
        }
    }

    if (emergency.loadMappings(fileName))
    {
        UtlString nameStr;
        UtlString descriptionStr;

        if (emergency.getMatchedRule(requestUri, nameStr, descriptionStr))
        {
            std::cout << "matched emergency rule result: " << nameStr << ", " << descriptionStr << std::endl;
        }
        else
        {
            std::cout << "no matched emergency rules found" << std::endl;
        }
    }
    else
    {
        std::cout << "error loading file " << fileName << std::endl;
    }
    return 0;
}

#include <iostream>
#include <string.h>
#include <stdlib.h>

#include "digitmaps/FallbackRulesUrlMapping.h"

#include "os/OsLogger.h"
#include "net/Url.h"
#include "utl/UtlHashMapIterator.h"

int main(int argc, char *argv[])
{
    UtlString fileName(argv[1]);
    UtlString callerLocation(argv[2]);

    UtlString uri(argv[3]);
    Url requestUri(uri);
    requestUri.toString(uri);

    // TODO check input parameters

    std::cout << "FALLBACK RULES CHECKER" << std::endl;
    std::cout << "using rules file: " << fileName.data() << ", caller location: " << callerLocation << ", request URI: " << uri << std::endl;

    FallbackRulesUrlMapping map;

    bool loaded = map.loadMappings(fileName);

    if (loaded)
    {
        std::cout << "loading file " << fileName << " complete" << std::endl;

        ResultSet records;
        UtlString callTag = "UNK";

        map.getContactList(requestUri, callerLocation, records, callTag);

        int count = records.getSize();

        if (count != 0)
        {
            for (int i = 0; i < count; i++)
            {
                UtlHashMap record;
                records.getIndex(i, record);

                UtlHashMapIterator it(record);

                while (it() != NULL)
                {
                    UtlString key;
                    UtlString value;

                    while (it() != NULL)
                    {
                        UtlString *key = dynamic_cast <UtlString *> (it.key());
                        UtlString *value = dynamic_cast <UtlString *> (it.value());

                        std::cout << "record[" << i << "](key, value):  " << (key ? *key : "___") << ", " << (value ? *value : "___") << std::endl;
                    }
                }
            }
        }
        else
        {
            std::cout << "result set is empty" << std::endl;
        }
    }
    else
    {
        std::cout << "error loading file " << fileName << std::endl;
    }

    return 0;
}

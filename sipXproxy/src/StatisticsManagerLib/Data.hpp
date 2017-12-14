#pragma once

#include <string>
#include <sstream>
#include <vector>

namespace statistics
{
      /// \class Data
      /// \brief statistics data exchange type
      struct Data
      {
            explicit Data() {}

            template<typename T>
                  Data(const std::string &n, const T &v) : name(n)
            {
                  std::stringstream s;
                  s << v;
                  value = s.str();
            }

            std::string name;
            std::string value;
      };

      std::ostream & operator<< (std::ostream &out, Data &d);

} // namespace statistics

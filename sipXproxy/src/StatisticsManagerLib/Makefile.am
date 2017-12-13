include $(top_srcdir)/config/utility.am

## Process this file with automake to produce Makefile.in

STATISTICSMANAGER_COMMON_CXX_FLAGS =

## Libtool version info - update only immediately before a release
# Current - increment if any interfaces are added, removed, or changed
version_Current=1
# Revision - set to 0 if Current was incremented
#            increment if any implementation has changed
version_Revision=0
# Age - set to 0 of any interfaces have been removed
#       increment if any interfaces have been added
version_Age=0

StatisticsManager_LTLIBRARIES = libStatisticsManager.la
StatisticsManagerdir = $(SIPX_LIBDIR)

libStatisticsManager_la_LDFLAGS = \
    -shared \
    -version-info ${version_Current}:${version_Revision}:${version_Age}

libStatisticsManager_la_SOURCES =  \
	StatisticsManager.cpp \
	StatisticsManager.hpp \
	SipStatistics.hpp \
	SipStatistics.cpp \
	Fifo.hpp

libStatisticsManager_la_CXXFLAGS = \
	$(STATISTICSMANAGER_COMMON_CXX_FLAGS)

libStatisticsManager_la_CFLAGS =

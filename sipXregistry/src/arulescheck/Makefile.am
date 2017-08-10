include $(top_srcdir)/config/utility.am

AM_FLAGS = -I$(top_srcdir)/include -I$(top_builddir)/config -I../../include

bin_PROGRAMS = \
	arulescheck

arulescheck_LDADD = \
	@SIPXTACK_LIBS@ \
	@SIPXPORT_LIBS@ \
	@SIPXCOMMSERVER_LIBS@ \
    -lmongoclient

arulescheck_CPPFLAGS = \
	$(AM_CFLAGS)

arulescheck_SOURCES = \
	arulescheck.cpp


include $(top_srcdir)/config/utility.am

AM_FLAGS = -I$(top_srcdir)/include -I$(top_builddir)/config -I../../include

bin_PROGRAMS = \
	fbrulescheck

fbrulescheck_LDADD = \
	@SIPXTACK_LIBS@ \
	@SIPXPORT_LIBS@ \
	@SIPXCOMMSERVER_LIBS@ \
    -lmongoclient

fbrulescheck_CPPFLAGS = \
	$(AM_CFLAGS)

fbrulescheck_SOURCES = \
	fbrulescheck.cpp


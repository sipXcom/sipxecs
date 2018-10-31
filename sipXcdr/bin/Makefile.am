include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(initd_SCRIPTS:=.in) \
	$(bin_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
	sipxcdr

bindir = @SIPX_BINDIR@
bin_SCRIPTS = \
	sipxcdr-archive

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxcdr.service

$(bin_SCRIPTS) $(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,ODBC_LIBDIR sipxcallresolver_VER)

$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(initd_SCRIPTS) $(bin_SCRIPTS) $(systemd_SCRIPTS)

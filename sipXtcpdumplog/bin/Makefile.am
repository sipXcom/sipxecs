include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = $(initd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
	sipxtcpdumplog

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxtcpdumplog.service

$(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,tcpdumplog_PKGS)

$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

DISTCLEANFILES = $(initd_SCRIPTS) $(systemd_SCRIPTS)

include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = $(initd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
	sipxprovision

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxprovision.service

$(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,provision_PKGS)

$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

DISTCLEANFILES = $(initd_SCRIPTS) $(systemd_SCRIPTS)

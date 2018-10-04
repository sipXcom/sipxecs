include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = \
	$(initd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
	sipxpage

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxpage.service

$(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,page_PKGS)

$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(initd_SCRIPTS) $(systemd_SCRIPTS)

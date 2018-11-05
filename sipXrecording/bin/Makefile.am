include $(top_srcdir)/config/utility.am
include $(top_srcdir)/common.am

EXTRA_DIST = $(initd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
	sipxrecording

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxrecording.service

$(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,recording_PKGS)

$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

DISTCLEANFILES = $(initd_SCRIPTS) $(systemd_SCRIPTS)

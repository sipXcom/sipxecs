include $(top_srcdir)/config/utility.am

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
    sipxregistrar

EXTRA_DIST = \
	$(initd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)

$(initd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)
$(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)
	
systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxregistrar.service

CLEANFILES = $(initd_SCRIPTS) $(systemd_SCRIPTS)

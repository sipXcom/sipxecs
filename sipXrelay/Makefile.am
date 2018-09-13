include config/utility.am
include config/project.am
include common.am

SUBDIRS = \
	src \
	.

@PACKAGE@_EXTRA_DIST = \
	CONTRIBUTORS \
	$(initd_SCRIPTS:=.in) \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in) \
	$(systemd_SCRIPTS:=.in)
	
confdir = @SIPX_CONFDIR@/sipxrelay
conf_DATA = \
	sipxrelay.sec.erb \
	log4j.properties

initddir = @SIPX_SERVICEDIR@
initd_SCRIPTS = \
	sipxrelay

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxrelay.conf

systemddir = /usr/lib/systemd/system
systemd_SCRIPTS = \
	sipxrelay.service

$(conf_DATA) $(initd_SCRIPTS) $(tmpfilesd_SCRIPTS) $(systemd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@,relay_PKGS)

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipxrelay.cf

DISTCLEANFILES = $(initd_SCRIPTS) $(conf_DATA) $(tmpfilesd_SCRIPTS) $(systemd_SCRIPTS)

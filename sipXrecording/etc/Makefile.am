include $(top_srcdir)/config/utility.am

EXTRA_DIST = \
	$(tmpfilesd_SCRIPTS:=.in)

cfinputsdir = @SIPX_CFINPUTS@/plugin.d
dist_cfinputs_DATA = \
	sipxrecording.cf

cfgdir = @SIPX_CONFDIR@/sipxrecording
dist_cfg_DATA = \
	sipxrecording.xml \
	sipxrecording.properties \
	sipxrecording-apache.conf

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipxrecording.conf

$(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(tmpfilesd_SCRIPTS)
